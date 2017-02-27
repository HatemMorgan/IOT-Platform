package com.iotplatform.services;

import java.util.Hashtable;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.DeveloperDao;
import com.iotplatform.daos.DynamicConceptDao;
import com.iotplatform.daos.ValidationDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.NoApplicationModelException;
import com.iotplatform.models.SuccessfullInsertionModel;
import com.iotplatform.models.SuccessfullSelectAllJsonModel;
import com.iotplatform.ontology.XSDDataTypes;
import com.iotplatform.ontology.classes.Application;
import com.iotplatform.ontology.classes.Developer;
import com.iotplatform.utilities.QueryResultUtility;
import com.iotplatform.validations.RequestValidation;

import oracle.spatial.rdf.client.jena.Oracle;

@Service("developerService")
public class DeveloperService {

	private DeveloperDao developerDao;
	private RequestValidation requestValidation;
	private Developer developerClass;
	private ApplicationDao applicationDao;

	@Autowired
	public DeveloperService(DeveloperDao developerDao, RequestValidation requestValidation, Developer developerClass,
			ApplicationDao applicationDao) {
		this.developerDao = developerDao;
		this.requestValidation = requestValidation;
		this.developerClass = developerClass;
		this.applicationDao = applicationDao;
	}

	/*
	 * insertDeveloper method is a service method that is responsible to take
	 * property values key pairs and call request validation to validate the
	 * request content then if it pass the validations call the developer data
	 * access object to insert the new developer
	 */

	public Hashtable<String, Object> insertDeveloper(Hashtable<String, Object> htblPropValue,
			String applicationNameCode) {

		long startTime = System.currentTimeMillis();

		/*
		 * check if the model exist or not .
		 */

		boolean exist = applicationDao.checkIfApplicationModelExsist(applicationNameCode);
		if (!exist) {
			NoApplicationModelException exception = new NoApplicationModelException(applicationNameCode, "Developer");
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return exception.getExceptionHashTable(timeTaken);
		}

		/*
		 * Check if the request is valid or not
		 */

		try {

			Hashtable<String, Object> htblPrefixedPropertyValue = requestValidation.isRequestValid(applicationNameCode,
					developerClass, htblPropValue);

			String applicationModelName = applicationDao.getHtblApplicationNameModelName().get(applicationNameCode);
			developerDao.insertDeveloper(htblPrefixedPropertyValue, applicationModelName);
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			SuccessfullInsertionModel successModel = new SuccessfullInsertionModel("Application", timeTaken);
			return successModel.getResponseJson();

		} catch (ErrorObjException ex) {
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return ex.getExceptionHashTable(timeTaken);

		}
	}

	/*
	 * getDevelopers method check if the application model is correct then it calls
	 * developerDao to get all developers  of this application
	 */
	
	public SuccessfullSelectAllJsonModel getDevelopers(String applicationNameCode) {

		long startTime = System.currentTimeMillis();
		boolean exist = applicationDao.checkIfApplicationModelExsist(applicationNameCode);

		/*
		 * check if the model exist or not .
		 */

		if (!exist) {
			NoApplicationModelException exception = new NoApplicationModelException(applicationNameCode, "Developer");
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return new SuccessfullSelectAllJsonModel(exception.getExceptionHashTable(timeTaken));
		}

		try {

			List<Hashtable<String, Object>> htblPropValue = developerDao
					.getDevelopers(applicationDao.getHtblApplicationNameModelName().get(applicationNameCode));
			
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return new SuccessfullSelectAllJsonModel(htblPropValue, timeTaken);

		} catch (ErrorObjException e) {
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return new SuccessfullSelectAllJsonModel(e.getExceptionHashTable(timeTaken));

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
		

		Developer developerClass = new Developer();
		DeveloperDao developerDao = new DeveloperDao(oracle, developerClass,new QueryResultUtility(new RequestValidation(validationDao, dynamicConceptDao)));
		RequestValidation requestValidation = new RequestValidation(new ValidationDao(oracle),
				new DynamicConceptDao(dataSource));

		ApplicationDao applicationDao = new ApplicationDao(oracle, new Application());

		DeveloperService developerService = new DeveloperService(developerDao, requestValidation, developerClass,
				applicationDao);
		
		Hashtable<String, Object> htblPropValue = new Hashtable<>();
		htblPropValue.put("age", 20);
		htblPropValue.put("firstName", "Omar");
		htblPropValue.put("middleName", "Hassan" );
		htblPropValue.put("familyName", "Tag" );
		htblPropValue.put("birthday", "27/2/1995" );
		htblPropValue.put("gender", "Male" );
		htblPropValue.put("id", "1" );
		htblPropValue.put("title", "Engineer" );
		htblPropValue.put("userName", "OmarTag" );
		htblPropValue.put("mbox", "omartagguv@gmail.com" );
		htblPropValue.put("developedApplication", "TESTAPPLICATION");
		htblPropValue.put("knows", "HatemMorgan");
		htblPropValue.put("hates", "HatemMorgan");

		
//		Hashtable<String, Object> res =  developerService.insertDeveloper(htblPropValue, "test Application");
		
		Hashtable<String, Object> res = developerService.getDevelopers("test Application").getJson();
		System.out.println(res.get("results"));
		
//		Hashtable<String, Object>[] json = (Hashtable<String, Object>[])res.get("errors");
//		System.out.println(json[0].toString());
		
//		System.out.println(res.toString());

		
		
	}
}
