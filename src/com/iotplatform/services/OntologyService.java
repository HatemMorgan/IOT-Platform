package com.iotplatform.services;

import java.util.Hashtable;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.DynamicOntologyDao;
import com.iotplatform.daos.OntologyDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.InvalidDynamicOntologyException;
import com.iotplatform.exceptions.NoApplicationModelException;
import com.iotplatform.models.SuccessfullInsertionModel;
import com.iotplatform.models.SuccessfullSelectAllJsonModel;
import com.iotplatform.ontology.similarty.check.OntExtension;
import com.iotplatform.validations.DynamicOntologyRequestValidation;

import oracle.spatial.rdf.client.jena.Oracle;

@Service("ontologyService")
public class OntologyService {

	private OntologyDao ontologyDao;
	private ApplicationDao applicationDao;
	private DynamicOntologyDao dynamicOntologyDao;
	private DynamicOntologyRequestValidation dynamicOntologyRequestValidation;

	@Autowired
	public OntologyService(OntologyDao ontologyDao, ApplicationDao applicationDao,
			DynamicOntologyDao dynamicOntologyDao, DynamicOntologyRequestValidation dynamicOntologyRequestValidation) {
		this.ontologyDao = ontologyDao;
		this.applicationDao = applicationDao;
		this.dynamicOntologyDao = dynamicOntologyDao;
		this.dynamicOntologyRequestValidation = dynamicOntologyRequestValidation;
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
		String applicationModelName = applicationDao.getHtblApplicationNameModelName()
				.get(applicationName.toLowerCase().replaceAll(" ", ""));

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
			String applicationModelName = applicationDao.getHtblApplicationNameModelName()
					.get(applicationName.toLowerCase().replaceAll(" ", ""));

			if (htblRequestBody == null || htblRequestBody.size() == 0) {
				throw new InvalidDynamicOntologyException("Invlid Request Body. ");
			}

			/*
			 * check that request is valid
			 */
			dynamicOntologyRequestValidation.validateNewClassOntologyRequest(htblRequestBody, applicationModelName);

			/*
			 * insert new class
			 */
			dynamicOntologyDao.addNewClassToOntology(htblRequestBody.get("name").toString(), applicationModelName);
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			SuccessfullInsertionModel successModel = new SuccessfullInsertionModel("Ontology", timeTaken);
			return successModel.getResponseJson();

		} catch (ErrorObjException ex) {
			System.out.println(ex.getExceptionMessage());

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
			String applicationModelName = applicationDao.getHtblApplicationNameModelName()
					.get(applicationName.toLowerCase().replaceAll(" ", ""));

			if (htblRequestBody == null || htblRequestBody.size() == 0) {
				throw new InvalidDynamicOntologyException("Invlid Request Body. ");
			}

			/*
			 * check that request is valid
			 */
			dynamicOntologyRequestValidation.validateNewObjectPropertyOntologyRequest(htblRequestBody,
					applicationModelName);

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
			System.out.println(ex.getExceptionMessage());
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
			String applicationModelName = applicationDao.getHtblApplicationNameModelName()
					.get(applicationName.toLowerCase().replaceAll(" ", ""));

			if (htblRequestBody == null || htblRequestBody.size() == 0) {
				throw new InvalidDynamicOntologyException("Invlid Request Body. ");
			}

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
			System.out.println(ex.getExceptionMessage());
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return ex.getExceptionHashTable(timeTaken);
		}

	}

	public static void main(String[] args) {
		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		String szUser = "rdfusr";
		String szPasswd = "rdfusr";

		Oracle oracle = new Oracle(szJdbcURL, szUser, szPasswd);

		OntologyDao ontologyDao = new OntologyDao(new DynamicOntologyDao(oracle));
		ApplicationDao applicationDao = new ApplicationDao(oracle);
		DynamicOntologyDao dynamicOntologyDao = new DynamicOntologyDao(oracle);

		DynamicOntologyRequestValidation dynamicOntologyRequestValidation = new DynamicOntologyRequestValidation(
				new OntExtension(), ontologyDao);

		OntologyService ontologyService = new OntologyService(ontologyDao, applicationDao, dynamicOntologyDao,
				dynamicOntologyRequestValidation);

		// LinkedHashMap<String, Object> htblResults =
		// ontologyService.getApplicationOntology("test application");
		// System.out.println(htblResults);
		// ontologyService.getApplicationOntology("test application");
		// System.out.println(htblResults);

		 Hashtable<String, Object> htbNewClassRequest = new Hashtable<>();
		
		 htbNewClassRequest.put("name", "");
		
		 System.out.println(ontologyService.insertNewClass("test application",
		 htbNewClassRequest));

		// Hashtable<String, Object> htblNewObjectPropertyRequest = new
		// Hashtable<>();
		//
		// htblNewObjectPropertyRequest.put("propertyName", "hates");
		// htblNewObjectPropertyRequest.put("domainPrefixName", "foaf:Person");
		// htblNewObjectPropertyRequest.put("rangePrefixName", "foaf:Person");
		// htblNewObjectPropertyRequest.put("hasMultipleValues", true);
		// htblNewObjectPropertyRequest.put("isUnique", false);
		//
		// System.out.println(ontologyService.insertNewObjectProperty("test
		// application", htblNewObjectPropertyRequest));

		Hashtable<String, Object> htblNewDataTypePropertyRequest = new Hashtable<>();

		htblNewDataTypePropertyRequest.put("propertyName", "job");
		htblNewDataTypePropertyRequest.put("domainPrefixName", "foaf:Person");
		htblNewDataTypePropertyRequest.put("dataType", "string");
		htblNewDataTypePropertyRequest.put("hasMultipleValues",true);
		htblNewDataTypePropertyRequest.put("isUnique", false);

		System.out
				.println(ontologyService.insertNewDataTypeProperty("test application", htblNewDataTypePropertyRequest));

	}

}
