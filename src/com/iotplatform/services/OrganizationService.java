package com.iotplatform.services;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.MainDao;
import com.iotplatform.daos.OrganizationDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.NoApplicationModelException;
import com.iotplatform.models.SuccessfullInsertionModel;
import com.iotplatform.models.SuccessfullSelectAllJsonModel;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.mapers.OntologyMapper;
import com.iotplatform.utilities.PropertyValue;
import com.iotplatform.validations.InsertRequestValidation;

@Service("organizationService")
public class OrganizationService {

	private InsertRequestValidation requestFieldsValidation;
	private ApplicationDao applicationDao;
	private OrganizationDao organizationDao;
	private MainDao mainDao;

	@Autowired
	public OrganizationService(InsertRequestValidation requestFieldsValidation, ApplicationDao applicationDao,
			OrganizationDao organizationDao, MainDao mainDao) {
		this.requestFieldsValidation = requestFieldsValidation;
		this.applicationDao = applicationDao;
		this.organizationDao = organizationDao;
		this.mainDao = mainDao;
	}

	/*
	 * insertOrganization method is a service method that is responsible to take
	 * property values key pairs and call request validation to validate the
	 * request content then if it pass the validations call the organization
	 * data access object to insert the new organization
	 */

	public Hashtable<String, Object> insertOrganization(Hashtable<String, Object> htblFieldValue,
			String applicationNameCode) {

		long startTime = System.currentTimeMillis();

		/*
		 * check if the model exist or not .
		 */

		boolean exist = applicationDao.checkIfApplicationModelExsist(applicationNameCode);
		if (!exist) {
			NoApplicationModelException exception = new NoApplicationModelException(applicationNameCode,
					"Organization");
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return exception.getExceptionHashTable(timeTaken);
		}

		/*
		 * Check if the request is valid or not
		 */

		try {

			/*
			 * Check if the request is valid or not
			 */
			Hashtable<Class, ArrayList<ArrayList<PropertyValue>>> htblClassPropertyValue = requestFieldsValidation
					.validateRequestFields(applicationNameCode, htblFieldValue,
							OntologyMapper.getHtblMainOntologyClassesMappers().get("organization"));

			/*
			 * get application modelName
			 */
			String applicationModelName = applicationDao.getHtblApplicationNameModelName().get(applicationNameCode);

			mainDao.insertData(applicationModelName,
					OntologyMapper.getHtblMainOntologyClassesMappers().get("organization").getName(),
					htblClassPropertyValue);

			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			SuccessfullInsertionModel successModel = new SuccessfullInsertionModel("Organization", timeTaken);
			return successModel.getResponseJson();

		} catch (ErrorObjException ex) {
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return ex.getExceptionHashTable(timeTaken);

		}
	}

	/*
	 * getOrganizations method check if the application model is correct then it
	 * calls organizationDao to get all organizations of this application
	 */
	public Hashtable<String, Object> getOrganizations(String applicationNameCode) {

		long startTime = System.currentTimeMillis();
		boolean exist = applicationDao.checkIfApplicationModelExsist(applicationNameCode);

		/*
		 * check if the model exist or not .
		 */

		if (!exist) {
			NoApplicationModelException exception = new NoApplicationModelException(applicationNameCode,
					"Organization");
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return new SuccessfullSelectAllJsonModel(exception.getExceptionHashTable(timeTaken)).getJson();
		}

		try {

			List<Hashtable<String, Object>> htblPropValue = organizationDao
					.getOrganizations(applicationDao.getHtblApplicationNameModelName().get(applicationNameCode));

			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return new SuccessfullSelectAllJsonModel(htblPropValue, timeTaken).getJson();

		} catch (ErrorObjException e) {
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return new SuccessfullSelectAllJsonModel(e.getExceptionHashTable(timeTaken)).getJson();

		}
	}

	// public static void main(String[] args) {
	// String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
	// String szUser = "rdfusr";
	// String szPasswd = "rdfusr";
	// String szJdbcDriver = "oracle.jdbc.driver.OracleDriver";
	//
	// BasicDataSource dataSource = new BasicDataSource();
	// dataSource.setDriverClassName(szJdbcDriver);
	// dataSource.setUrl(szJdbcURL);
	// dataSource.setUsername(szUser);
	// dataSource.setPassword(szPasswd);
	//
	// Oracle oracle = new Oracle(szJdbcURL, szUser, szPasswd);
	//
	// DynamicConceptDao dynamicConceptDao = new DynamicConceptDao(dataSource);
	//
	// ValidationDao validationDao = new ValidationDao(oracle);
	//
	// Organization organizationClass = new Organization();
	//
	// SingleClassRequestValidation requestValidation = new
	// SingleClassRequestValidation(validationDao,
	// dynamicConceptDao);
	//
	// OrganizationDao organizationDao = new OrganizationDao(oracle, new
	// SelectionUtility(requestValidation),
	// organizationClass);
	//
	// // Hashtable<String, Object> htblPropValue = new Hashtable<>();
	// // htblPropValue.put("name", "Google");
	// // htblPropValue.put("description",
	// // "Google is a software Engineering Company that helps peolpe with new
	// // technologies.");
	//
	// Hashtable<String, Object> htblPropValue = new Hashtable<>();
	// htblPropValue.put("name", "BMW");
	// htblPropValue.put("description", "BMW is mechanical company that
	// manufacture automative cars");
	//
	// OrganizationService organizationService = new
	// OrganizationService(requestValidation,
	// new ApplicationDao(oracle, new Application()), organizationDao,
	// organizationClass);
	//
	// // Hashtable<String, Object> res =
	// // organizationService.getOrganizations("test application");
	//
	// Hashtable<String, Object> res =
	// organizationService.insertOrganization(htblPropValue, "TESTAPPLICATION");
	//
	// // Hashtable<String, Object>[] json = (Hashtable<String,
	// // Object>[])res.get("errors");
	// // System.out.println(json[0].toString());
	//
	// System.out.println(res.toString());
	// }

}
