package com.iotplatform.services;

import java.util.Hashtable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.DynamicConceptDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.InvalidDynamicConceptException;
import com.iotplatform.exceptions.NoApplicationModelException;
import com.iotplatform.models.DynamicConceptModel;
import com.iotplatform.models.SuccessfullInsertionModel;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.PropertyType;
import com.iotplatform.ontology.XSDDatatype;

@Service("dynamicConceptService")
public class DynamicConceptService {

	DynamicConceptDao dynamicConceptDao;
	ApplicationDao applicationDao;

	@Autowired
	public DynamicConceptService(DynamicConceptDao dynamicConceptDao, ApplicationDao applicationDao) {
		this.dynamicConceptDao = dynamicConceptDao;
		this.applicationDao = applicationDao;
	}

	/*
	 * insertNewConcept service method is used to call dynamicConceptDao and it
	 * return a json object
	 */
	public Hashtable<String, Object> insertNewConcept(String applicationNameCode, DynamicConceptModel newConcept) {
		long startTime = System.currentTimeMillis();
		try {

			/*
			 * check if the model exist or not .
			 */

			boolean exist = applicationDao.checkIfApplicationModelExsist(applicationNameCode);
			if (!exist) {
				NoApplicationModelException exception = new NoApplicationModelException(applicationNameCode,
						"Ontology");
				double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
				return exception.getExceptionHashTable(timeTaken);
			}

			/*
			 * check if the applicationName is not the same as newConcept
			 * application name domain
			 */

			if (!applicationNameCode.replaceAll(" ", "").toLowerCase()
					.equals(newConcept.getApplication_name().replaceAll(" ", "").toLowerCase())) {

				ErrorObjException err = new ErrorObjException(HttpStatus.BAD_REQUEST.name(),
						HttpStatus.BAD_REQUEST.value(), "Wrong Application name ", "Ontology");
				double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
				return err.getExceptionHashTable(timeTaken);
			}

			/*
			 * check if the passed new concept has a valid fields
			 * 
			 * if newConcept is invalid the isNewConceptValid method will throw
			 * an InvalidDynamicConceptException
			 */

			if (isNewConceptValid(newConcept)) {
				dynamicConceptDao.insertNewConcept(newConcept);
				SuccessfullInsertionModel successModel = new SuccessfullInsertionModel("New Ontology Concept");
				return successModel.getResponseJson();
			}

		} catch (ErrorObjException e) {
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return e.getExceptionHashTable(timeTaken);
		}

		return null;
	}

	public Hashtable<String, Object> getApplicationDynamicConcepts(String applicationNameCode) {
		long startTime = System.currentTimeMillis();
		try {

			/*
			 * check if the model exist or not .
			 */

			boolean exist = applicationDao.checkIfApplicationModelExsist(applicationNameCode);
			if (!exist) {
				NoApplicationModelException exception = new NoApplicationModelException(applicationNameCode,
						"Developer");
				double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
				return exception.getExceptionHashTable(timeTaken);
			}

			List<DynamicConceptModel> concepts = dynamicConceptDao.getConceptsOfApplication(applicationNameCode);
			Hashtable<String, Object> json = new Hashtable<>();
			json.put("dynamicAddedConcepts", concepts);
			return json;
		} catch (ErrorObjException e) {
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return e.getExceptionHashTable(timeTaken);
		}

	}

	/*
	 * isValidUri method is used to check if the classURI, propertyURI and
	 * ObjectTypeURI have valid URI (started with one of the prefixes URIs
	 * defined in the ontology)
	 */
	private boolean isNewConceptValid(DynamicConceptModel newConcept) {

		boolean validClassURI = false;
		boolean validPropertyURI = false;
		boolean validObjectTypeURI = false;

		for (Prefix prefix : Prefix.values()) {

			if (!validClassURI) {
				if (newConcept.getClass_uri().contains(prefix.getUri())
						&& newConcept.getClass_prefix_uri().equals(prefix.getUri())
						&& newConcept.getClass_prefix_alias().equals(prefix.getPrefix()))
					validClassURI = true;
			}

			if (!validPropertyURI) {
				if (newConcept.getProperty_uri().contains(prefix.getUri())
						&& newConcept.getProperty_prefix_uri().equals(prefix.getUri())
						&& newConcept.getProperty_prefix_alias().equals(prefix.getPrefix()))
					validPropertyURI = true;
			}

			if (!validObjectTypeURI) {
				if (newConcept.getProperty_object_type_uri().contains(prefix.getUri()))
					validObjectTypeURI = true;
			}

		}

		if (!(newConcept.getIsUnique() == 0 || newConcept.getIsUnique() == 1)) {
			throw new InvalidDynamicConceptException(
					"Invalid isUnique field value. isUnique field must be 0(false) or 1(true)");
		}

		if (!(newConcept.getHasMultipleValues() == 0 || newConcept.getHasMultipleValues() == 1)) {
			throw new InvalidDynamicConceptException(
					"Invalid hasMultipleValues field value. hasMultipleValues field must be 0(false) or 1(true)");
		}

		if (!validClassURI) {
			throw new InvalidDynamicConceptException(
					"Invalid Class fields values. ClassURI, classPrefixURI and classPrefixAlias values must"
							+ " be a valid URI with one of the ontology prefixes");
		}

		if (!validPropertyURI) {
			throw new InvalidDynamicConceptException(
					"Invalid Class fields values. ClassURI, classPrefixURI and classPrefixAlias values must"
							+ " be a valid URI with one of the ontology prefixes");
		}

		if (!validObjectTypeURI) {
			throw new InvalidDynamicConceptException("Invalid Class fields values. propertyObjectTypeURI values must"
					+ " be a valid URI with one of the ontology prefixes");
		}

		if (!(newConcept.getProperty_type().equals(PropertyType.DatatypeProperty.toString())
				|| newConcept.getProperty_type().equals(PropertyType.ObjectProperty.toString()))) {
			throw new InvalidDynamicConceptException("Invalid propertyObjectTypeURI value. propertyObjectTypeURI field"
					+ " must have a value either \"DatatypeProperty\" or \"ObjectProperty\" ");
		}

		/*
		 * check if DataTypeProperty as a valid datatype
		 */
		if (newConcept.getProperty_type().equals(PropertyType.DatatypeProperty.toString()) && validObjectTypeURI) {
			boolean check = false;
			for (XSDDatatype xsdDataType : XSDDatatype.values()) {
				if (newConcept.getProperty_object_type_uri().equals(xsdDataType.getXsdTypeURI())) {
					check = true;
					break;
				}
			}

			if (!check) {
				throw new InvalidDynamicConceptException(
						"Invalid DataType for new added DataProperty. Check documentation for more information");
			}
		}

		return true;
	}

}
