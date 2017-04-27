package com.iotplatform.services;

import java.util.Hashtable;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.DynamicOntologyDao;
import com.iotplatform.daos.OntologyDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.NoApplicationModelException;
import com.iotplatform.models.SuccessfullInsertionModel;
import com.iotplatform.models.SuccessfullSelectAllJsonModel;
import com.iotplatform.validations.DynamicOntologyRequestValidation;

@Service("ontologyService")
public class OntologyService {

	private OntologyDao ontologyDao;
	private ApplicationDao applicationDao;
	private DynamicOntologyDao dynamicOntologyDao;

	@Autowired
	public OntologyService(OntologyDao ontologyDao, ApplicationDao applicationDao,
			DynamicOntologyDao dynamicOntologyDao) {
		this.ontologyDao = ontologyDao;
		this.applicationDao = applicationDao;
		this.dynamicOntologyDao = dynamicOntologyDao;
	}

	public LinkedHashMap<String, Object> getApplicationOntology(String applicationName) {

		long startTime = System.currentTimeMillis();
		boolean exist = applicationDao.checkIfApplicationModelExsist(applicationName);

		/*
		 * check if the model exist or not .
		 */

		if (!exist) {
			NoApplicationModelException exception = new NoApplicationModelException(applicationName, "Ontology");
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return new SuccessfullSelectAllJsonModel(exception.getExceptionHashTable(timeTaken)).getJson();
		}

		/*
		 * get application modelName
		 */
		String applicationModelName = applicationDao.getHtblApplicationNameModelName().get(applicationName);
		return ontologyDao.loadApplicationOntology(applicationModelName);

	}

	public LinkedHashMap<String, Object> insertNewClass(String applicationName,
			Hashtable<String, Object> htblRequestBody) {

		long startTime = System.currentTimeMillis();

		try {
			boolean exist = applicationDao.checkIfApplicationModelExsist(applicationName);

			/*
			 * check if the model exist or not .
			 */

			if (!exist) {
				NoApplicationModelException exception = new NoApplicationModelException(applicationName, "Ontology");
				double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
				return new SuccessfullSelectAllJsonModel(exception.getExceptionHashTable(timeTaken)).getJson();
			}

			/*
			 * get application modelName
			 */
			String applicationModelName = applicationDao.getHtblApplicationNameModelName().get(applicationName);

			/*
			 * check that request is valid
			 */
			DynamicOntologyRequestValidation.validateNewClassOntologyRequest(htblRequestBody);

			/*
			 * insert new class
			 */
			dynamicOntologyDao.addNewClassToOntology(htblRequestBody.get("name").toString(), applicationModelName);
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			SuccessfullInsertionModel successModel = new SuccessfullInsertionModel("Ontology", timeTaken);
			return successModel.getResponseJson();

		} catch (ErrorObjException ex) {
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return ex.getExceptionHashTable(timeTaken);
		}

	}

	public LinkedHashMap<String, Object> insertNewObjectProperty(String applicationName,
			Hashtable<String, Object> htblRequestBody) {

		long startTime = System.currentTimeMillis();

		try {
			boolean exist = applicationDao.checkIfApplicationModelExsist(applicationName);

			/*
			 * check if the model exist or not .
			 */

			if (!exist) {
				NoApplicationModelException exception = new NoApplicationModelException(applicationName, "Ontology");
				double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
				return new SuccessfullSelectAllJsonModel(exception.getExceptionHashTable(timeTaken)).getJson();
			}

			/*
			 * get application modelName
			 */
			String applicationModelName = applicationDao.getHtblApplicationNameModelName().get(applicationName);

			/*
			 * check that request is valid
			 */
			DynamicOntologyRequestValidation.validateNewObjectPropertyOntologyRequest(htblRequestBody);

			/*
			 * insert new class
			 */

			dynamicOntologyDao.addNewObjectPropertyToOntology(htblRequestBody.get("propertyName").toString(),
					htblRequestBody.get("domainPrefixName").toString(),
					htblRequestBody.get("rangePrefixName").toString(),
					Boolean.parseBoolean(htblRequestBody.get("hasMultipleValues").toString()),
					Boolean.parseBoolean(htblRequestBody.get("isUnique").toString()), applicationModelName);

			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			SuccessfullInsertionModel successModel = new SuccessfullInsertionModel("Ontology", timeTaken);
			return successModel.getResponseJson();

		} catch (ErrorObjException ex) {
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return ex.getExceptionHashTable(timeTaken);
		}

	}

	public LinkedHashMap<String, Object> insertNewDataTypeProperty(String applicationName,
			Hashtable<String, Object> htblRequestBody) {

		long startTime = System.currentTimeMillis();

		try {
			boolean exist = applicationDao.checkIfApplicationModelExsist(applicationName);

			/*
			 * check if the model exist or not .
			 */

			if (!exist) {
				NoApplicationModelException exception = new NoApplicationModelException(applicationName, "Ontology");
				double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
				return new SuccessfullSelectAllJsonModel(exception.getExceptionHashTable(timeTaken)).getJson();
			}

			/*
			 * get application modelName
			 */
			String applicationModelName = applicationDao.getHtblApplicationNameModelName().get(applicationName);

			/*
			 * check that request is valid
			 */
			DynamicOntologyRequestValidation.validateNewDataTypPropertyOntologyRequest(htblRequestBody);

			/*
			 * insert new DataType property
			 */
			dynamicOntologyDao.addNewDatatypePropertyToOntology(htblRequestBody.get("propertyName").toString(),
					htblRequestBody.get("domainPrefixName").toString(), htblRequestBody.get("dataType").toString(),
					Boolean.parseBoolean(htblRequestBody.get("hasMultipleValues").toString()),
					Boolean.parseBoolean(htblRequestBody.get("isUnique").toString()), applicationModelName);

			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			SuccessfullInsertionModel successModel = new SuccessfullInsertionModel("Ontology", timeTaken);
			return successModel.getResponseJson();

		} catch (ErrorObjException ex) {
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return ex.getExceptionHashTable(timeTaken);
		}

	}

}
