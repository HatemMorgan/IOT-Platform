package com.iotplatform.services;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.DynamicConceptsDao;
import com.iotplatform.daos.MainDao;
import com.iotplatform.daos.NormalUserDao;
import com.iotplatform.daos.ValidationDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.NoApplicationModelException;
import com.iotplatform.models.SuccessfullInsertionModel;
import com.iotplatform.models.SuccessfullSelectAllJsonModel;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.dynamicConcepts.DynamicConceptsUtility;
import com.iotplatform.ontology.mapers.OntologyMapper;
import com.iotplatform.query.results.SelectionQueryResults;
import com.iotplatform.utilities.PropertyValue;
import com.iotplatform.validations.InsertRequestValidation;

import oracle.spatial.rdf.client.jena.Oracle;

@Service("normalUserService")
public class NormalUserService {

	private InsertRequestValidation requestFieldsValidation;
	private ApplicationDao applicationDao;
	private NormalUserDao normalUserDao;
	private MainDao mainDao;

	@Autowired
	public NormalUserService(InsertRequestValidation requestFieldsValidation, ApplicationDao applicationDao,
			NormalUserDao normalUserDao, MainDao mainDao) {
		this.requestFieldsValidation = requestFieldsValidation;
		this.applicationDao = applicationDao;
		this.normalUserDao = normalUserDao;
		this.mainDao = mainDao;
	}

	/*
	 * insertNormalUser method is a service method that is responsible to take
	 * property values key pairs and call request validation to validate the
	 * request content then if it pass the validations call the normal user data
	 * access object to insert the new normal user
	 */

	public Hashtable<String, Object> insertNormalUser(Hashtable<String, Object> htblFieldValue,
			String applicationNameCode) {

		long startTime = System.currentTimeMillis();

		/*
		 * check if the model exist or not .
		 */

		boolean exist = applicationDao.checkIfApplicationModelExsist(applicationNameCode);
		if (!exist) {
			NoApplicationModelException exception = new NoApplicationModelException(applicationNameCode, "Normal User");
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
							OntologyMapper.getHtblMainOntologyClassesMappers().get("normaluser"));

			/*
			 * get application modelName
			 */
			String applicationModelName = applicationDao.getHtblApplicationNameModelName().get(applicationNameCode);

			mainDao.insertData(applicationModelName,
					OntologyMapper.getHtblMainOntologyClassesMappers().get("normaluser").getName(),
					htblClassPropertyValue);

			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			SuccessfullInsertionModel successModel = new SuccessfullInsertionModel("Normal User", timeTaken);
			return successModel.getResponseJson();

		} catch (ErrorObjException ex) {
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return ex.getExceptionHashTable(timeTaken);

		}
	}

	/*
	 * getNormalUsers method check if the application model is correct then it
	 * calls normalUserDao to get all normalUsers of this application
	 */
	public Hashtable<String, Object> getNormalUsers(String applicationNameCode) {

		long startTime = System.currentTimeMillis();
		boolean exist = applicationDao.checkIfApplicationModelExsist(applicationNameCode);

		/*
		 * check if the model exist or not .
		 */

		if (!exist) {
			NoApplicationModelException exception = new NoApplicationModelException(applicationNameCode, "Normal User");
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return new SuccessfullSelectAllJsonModel(exception.getExceptionHashTable(timeTaken)).getJson();
		}

		try {

			List<Hashtable<String, Object>> htblPropValue = normalUserDao
					.getNormalUsers(applicationDao.getHtblApplicationNameModelName().get(applicationNameCode));

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

		DynamicConceptsDao dynamicConceptDao = new DynamicConceptsDao(dataSource);

		ValidationDao validationDao = new ValidationDao(oracle);

		NormalUserDao normalUserDao = new NormalUserDao(oracle,
				new SelectionQueryResults(new DynamicConceptsUtility(dynamicConceptDao)));

		Hashtable<String, Object> htblPropValue = new Hashtable<>();
		htblPropValue.put("age", 20);
		htblPropValue.put("firstName", "Mohamed");
		htblPropValue.put("middleName", "Ebrahim");
		htblPropValue.put("familyName", "Gomaa");
		htblPropValue.put("birthday", "27/2/1995");
		htblPropValue.put("gender", "Male");
		htblPropValue.put("title", "Engineer");
		htblPropValue.put("userName", "MohamedIbrahim");

		ArrayList<Object> emailList = new ArrayList<>();
		emailList.add("hima@gmail.com");
		emailList.add("mohamed.ibrahim@student.guc.edu.eg");

		htblPropValue.put("mbox", emailList);

		htblPropValue.put("usesApplication", "TESTAPPLICATION");
		htblPropValue.put("knows", "HatemMorgan");
		htblPropValue.put("hates", "HatemMorgan");

		InsertRequestValidation requestFieldsValidation = new InsertRequestValidation(validationDao,
				new DynamicConceptsUtility(dynamicConceptDao));

		MainDao mainDao = new MainDao(oracle, new SelectionQueryResults(new DynamicConceptsUtility(dynamicConceptDao)));

		NormalUserService normalUserService = new NormalUserService(requestFieldsValidation, new ApplicationDao(oracle),
				normalUserDao, mainDao);

		Hashtable<String, Object> normalusers = normalUserService.getNormalUsers("TESTAPPLICATION");
		System.out.println(normalusers);

		// Hashtable<String, Object> res =
		// normalUserService.insertNormalUser(htblPropValue, "TESTAPPLICATION");

		// Hashtable<String, Object>[] json = (Hashtable<String,
		// Object>[])res.get("errors");
		// System.out.println(json[0].toString());

		// System.out.println(res.toString());
	}

}
