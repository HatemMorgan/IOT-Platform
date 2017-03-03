package com.iotplatform.services;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.AdminDao;
import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.DynamicConceptDao;
import com.iotplatform.daos.OrganizationDao;
import com.iotplatform.daos.ValidationDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.NoApplicationModelException;
import com.iotplatform.models.SuccessfullInsertionModel;
import com.iotplatform.models.SuccessfullSelectAllJsonModel;
import com.iotplatform.ontology.classes.Admin;
import com.iotplatform.ontology.classes.Application;
import com.iotplatform.ontology.classes.Organization;
import com.iotplatform.utilities.PropertyValue;
import com.iotplatform.utilities.SelectionUtility;
import com.iotplatform.validations.RequestValidation;

import oracle.spatial.rdf.client.jena.Oracle;

@Service("organizationService")
public class OrganizationService {

	private RequestValidation requestValidation;
	private ApplicationDao applicationDao;
	private OrganizationDao organizationDao;
	private Organization organizationClass;

	@Autowired
	public OrganizationService(RequestValidation requestValidation, ApplicationDao applicationDao,
			OrganizationDao organizationDao, Organization organizationClass) {
		this.requestValidation = requestValidation;
		this.applicationDao = applicationDao;
		this.organizationDao = organizationDao;
		this.organizationClass = organizationClass;
	}

	/*
	 * insertOrganization method is a service method that is responsible to take
	 * property values key pairs and call request validation to validate the
	 * request content then if it pass the validations call the organization
	 * data access object to insert the new organization
	 */

	public Hashtable<String, Object> insertOrganization(Hashtable<String, Object> htblPropValue,
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

			ArrayList<PropertyValue> prefixedPropertyValue = requestValidation.isRequestValid(applicationNameCode,
					organizationClass, htblPropValue);

			String applicationModelName = applicationDao.getHtblApplicationNameModelName().get(applicationNameCode);

			String organizationName = htblPropValue.get("name").toString();

			organizationDao.insertOrganization(prefixedPropertyValue, applicationModelName, organizationName);

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

	public static void main(String[] args) {
		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		String szUser = "rdfusr";
		String szPasswd = "rdfusr";
		String szJdbcDriver = "oracle.jdbc.driver.OracleDriver";

		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(szJdbcDriver);
		dataSource.setUrl(szJdbcURL);
		dataSource.setUsername(szUser);
		dataSource.setPassword(szPasswd);

		Oracle oracle = new Oracle(szJdbcURL, szUser, szPasswd);

		DynamicConceptDao dynamicConceptDao = new DynamicConceptDao(dataSource);

		ValidationDao validationDao = new ValidationDao(oracle);

		Organization organizationClass = new Organization();

		RequestValidation requestValidation = new RequestValidation(validationDao, dynamicConceptDao);

		OrganizationDao organizationDao = new OrganizationDao(oracle, new SelectionUtility(requestValidation),
				organizationClass);

		// Hashtable<String, Object> htblPropValue = new Hashtable<>();
		// htblPropValue.put("name", "Google");
		// htblPropValue.put("description",
		// "Google is a software Engineering Company that helps peolpe with new
		// technologies.");

		Hashtable<String, Object> htblPropValue = new Hashtable<>();
		htblPropValue.put("name", "BMW");
		htblPropValue.put("description", "BMW is mechanical company that manufacture automative cars");

		OrganizationService organizationService = new OrganizationService(requestValidation,
				new ApplicationDao(oracle, new Application()), organizationDao, organizationClass);

//		Hashtable<String, Object> res = organizationService.getOrganizations("test application");

		 Hashtable<String, Object> res =
		 organizationService.insertOrganization(htblPropValue,
		 "TESTAPPLICATION");

		// Hashtable<String, Object>[] json = (Hashtable<String,
		// Object>[])res.get("errors");
		// System.out.println(json[0].toString());

		System.out.println(res.toString());
	}

}
