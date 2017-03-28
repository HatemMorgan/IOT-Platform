package com.iotplatform.services;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.DeveloperDao;
import com.iotplatform.daos.DynamicConceptDao;
import com.iotplatform.daos.MainDao;
import com.iotplatform.daos.ValidationDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.NoApplicationModelException;
import com.iotplatform.models.SuccessfullInsertionModel;
import com.iotplatform.models.SuccessfullSelectAllJsonModel;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.classes.Developer;
import com.iotplatform.utilities.DynamicPropertiesUtility;
import com.iotplatform.utilities.PropertyValue;
import com.iotplatform.utilities.SelectionUtility;
import com.iotplatform.validations.PostRequestValidations;

import oracle.spatial.rdf.client.jena.Oracle;

@Service("developerService")
public class DeveloperService {

	private DeveloperDao developerDao;
	private PostRequestValidations requestFieldsValidation;
	private MainDao mainDao;
	private ApplicationDao applicationDao;

	@Autowired
	public DeveloperService(DeveloperDao developerDao, PostRequestValidations requestFieldsValidation, MainDao mainDao,
			ApplicationDao applicationDao) {
		this.developerDao = developerDao;
		this.requestFieldsValidation = requestFieldsValidation;
		this.mainDao = mainDao;
		this.applicationDao = applicationDao;
	}

	/*
	 * insertDeveloper method is a service method that is responsible to take
	 * property values key pairs and call request validation to validate the
	 * request content then if it pass the validations call the developer data
	 * access object to insert the new developer
	 */

	public Hashtable<String, Object> insertDeveloper(Hashtable<String, Object> htblFieldValue,
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

			/*
			 * Check if the request is valid or not
			 */
			Hashtable<Class, ArrayList<ArrayList<PropertyValue>>> htblClassPropertyValue = requestFieldsValidation
					.validateRequestFields(applicationNameCode, htblFieldValue, Developer.getDeveloperInstance());

			String applicationModelName = applicationDao.getHtblApplicationNameModelName().get(applicationNameCode);

			mainDao.insertData(applicationModelName, Developer.getDeveloperInstance().getName(),
					htblClassPropertyValue);

			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			SuccessfullInsertionModel successModel = new SuccessfullInsertionModel("Developer", timeTaken);
			return successModel.getResponseJson();

		} catch (ErrorObjException ex) {
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return ex.getExceptionHashTable(timeTaken);

		}
	}

	/*
	 * getDevelopers method check if the application model is correct then it
	 * calls developerDao to get all developers of this application
	 */

	public Hashtable<String, Object> getDevelopers(String applicationNameCode) {

		long startTime = System.currentTimeMillis();
		boolean exist = applicationDao.checkIfApplicationModelExsist(applicationNameCode);

		/*
		 * check if the model exist or not .
		 */

		if (!exist) {
			NoApplicationModelException exception = new NoApplicationModelException(applicationNameCode, "Developer");
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return new SuccessfullSelectAllJsonModel(exception.getExceptionHashTable(timeTaken)).getJson();
		}

		try {

			List<Hashtable<String, Object>> htblPropValue = developerDao
					.getDevelopers(applicationDao.getHtblApplicationNameModelName().get(applicationNameCode));

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

		DeveloperDao developerDao = new DeveloperDao(oracle,
				new SelectionUtility(new DynamicPropertiesUtility(dynamicConceptDao)));

		PostRequestValidations requestFieldsValidation = new PostRequestValidations(validationDao,
				new DynamicPropertiesUtility(dynamicConceptDao));

		MainDao mainDao = new MainDao(oracle, new SelectionUtility(new DynamicPropertiesUtility(dynamicConceptDao)));

		DeveloperService developerService = new DeveloperService(developerDao, requestFieldsValidation, mainDao,
				new ApplicationDao(oracle));

		// Hashtable<String, Object> htblPropValue = new Hashtable<>();
		// htblPropValue.put("age", 20);
		// htblPropValue.put("firstName", "Hatem");
		// htblPropValue.put("middleName", "ELsayed");
		// htblPropValue.put("familyName", "Morgan");
		// htblPropValue.put("birthday", "27/7/1995");
		// htblPropValue.put("gender", "Male");
		// htblPropValue.put("title", "Engineer");
		// htblPropValue.put("userName", "HatemMorgan");
		//
		// ArrayList<Object> emailList = new ArrayList<>();
		// emailList.add("hatemmorgan17@gmail.com");
		// emailList.add("hatem.el-sayed@student.guc.edu.eg");
		//
		// htblPropValue.put("mbox", emailList);
		//
		// htblPropValue.put("developedApplication", "TESTAPPLICATION");
		// htblPropValue.put("knows", "HatemMorgan");
		// htblPropValue.put("hates", "HatemMorgan");
		// htblPropValue.put("job", "Engineeer");

		Hashtable<String, Object> htblPropValue = new Hashtable<>();
		htblPropValue.put("age", 21);
		htblPropValue.put("firstName", "Haytham");
		htblPropValue.put("middleName", "Ismail");
		htblPropValue.put("familyName", "Khalf");
		htblPropValue.put("birthday", "27/7/1975");
		htblPropValue.put("gender", "Male");
		htblPropValue.put("title", "Professor");
		htblPropValue.put("userName", "HaythamIsmail");

		ArrayList<Object> emailList = new ArrayList<>();
		emailList.add("haytham.ismail@gmail.com");
		emailList.add("haytham.ismail@student.guc.edu.eg");

		htblPropValue.put("mbox", emailList);

		htblPropValue.put("developedApplication", "TESTAPPLICATION");
		htblPropValue.put("knows", "HatemMorgan");
		htblPropValue.put("hates", "HatemMorgan");
		htblPropValue.put("job", "Engineeer");

		Hashtable<String, Object> res = developerService.getDevelopers("testApplication");
		System.out.println(res.get("results"));

		// System.out.println("===================================");
		// System.out.println(developerClass.getProperties().toString());
		// System.out.println("===================================");

		// Hashtable<String, Object> resInsertion =
		// developerService.insertDeveloper(htblPropValue, "test Application");

		// Hashtable<String, Object>[] json = (Hashtable<String, Object>[])
		// resInsertion.get("errors");
		// System.out.println(json[0].toString());

		// System.out.println(resInsertion.toString());

	}
}
