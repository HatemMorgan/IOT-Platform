package com.iotplatform.ontology.dynamicConcepts;

import java.util.Hashtable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.DynamicConceptsDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.NoApplicationModelException;
import com.iotplatform.models.DynamicConceptModel;
import com.iotplatform.models.SuccessfullInsertionModel;

/*
 * DynamicConceptService is used to take input from API Controller  validate it and then call DynamicConceptDao 
 * to perform any CRUD operations 
 */

@Service("dynamicConceptService")
public class DynamicConceptsService {

	DynamicConceptsDao dynamicConceptDao;
	ApplicationDao applicationDao;

	@Autowired
	public DynamicConceptsService(DynamicConceptsDao dynamicConceptDao, ApplicationDao applicationDao) {
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

			if (DynamicConceptsValidation.isNewConceptValid(newConcept)) {
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

}
