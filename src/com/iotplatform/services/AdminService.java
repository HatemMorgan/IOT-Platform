package com.iotplatform.services;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.AdminDao;
import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.DynamicConceptsDao;
import com.iotplatform.daos.MainDao;
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
import com.iotplatform.validations.PostRequestValidations;

import oracle.spatial.rdf.client.jena.Oracle;

@Service("adminService")
public class AdminService {

	private PostRequestValidations requestFieldsValidation;
	private ApplicationDao applicationDao;
	private AdminDao adminDao;
	private MainDao mainDao;

	@Autowired
	public AdminService(PostRequestValidations requestFieldsValidation, ApplicationDao applicationDao,
			AdminDao adminDao, MainDao mainDao) {
		this.requestFieldsValidation = requestFieldsValidation;
		this.applicationDao = applicationDao;
		this.adminDao = adminDao;
		this.mainDao = mainDao;
	}

	/*
	 * insertAdmin method is a service method that is responsible to take
	 * property values key pairs and call request validation to validate the
	 * request content then if it pass the validations call the admin data
	 * access object to insert the new admin
	 */

	public Hashtable<String, Object> insertAdmin(Hashtable<String, Object> htblFieldValue, String applicationNameCode) {

		long startTime = System.currentTimeMillis();

		/*
		 * check if the model exist or not .
		 */

		boolean exist = applicationDao.checkIfApplicationModelExsist(applicationNameCode);
		if (!exist) {
			NoApplicationModelException exception = new NoApplicationModelException(applicationNameCode, "Admin");
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return exception.getExceptionHashTable(timeTaken);
		}

		try {

			/*
			 * Check if the request is valid or not
			 */
			Hashtable<Class, ArrayList<ArrayList<PropertyValue>>> htblClassPropertyValue = requestFieldsValidation
					.validateRequestFields(applicationNameCode, htblFieldValue,
							OntologyMapper.getHtblMainOntologyClassesMappers().get("admin"));

			/*
			 * get application modelName
			 */
			String applicationModelName = applicationDao.getHtblApplicationNameModelName().get(applicationNameCode);

			mainDao.insertData(applicationModelName,
					OntologyMapper.getHtblMainOntologyClassesMappers().get("admin").getName(), htblClassPropertyValue);

			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			SuccessfullInsertionModel successModel = new SuccessfullInsertionModel("Admin", timeTaken);
			return successModel.getResponseJson();

		} catch (ErrorObjException ex) {
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return ex.getExceptionHashTable(timeTaken);

		}
	}

	/*
	 * getAdmins method check if the application model is correct then it calls
	 * adminDao to get all admins of this application
	 */
	public Hashtable<String, Object> getAdmins(String applicationNameCode) {

		long startTime = System.currentTimeMillis();
		boolean exist = applicationDao.checkIfApplicationModelExsist(applicationNameCode);

		/*
		 * check if the model exist or not .
		 */

		if (!exist) {
			NoApplicationModelException exception = new NoApplicationModelException(applicationNameCode, "Admin");
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return new SuccessfullSelectAllJsonModel(exception.getExceptionHashTable(timeTaken)).getJson();
		}

		try {

			List<Hashtable<String, Object>> htblPropValue = adminDao
					.getAdmins(applicationDao.getHtblApplicationNameModelName().get(applicationNameCode));

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

		AdminDao adminDao = new AdminDao(oracle, new SelectionQueryResults(new DynamicConceptsUtility(dynamicConceptDao)));

		Hashtable<String, Object> htblFieldValue = new Hashtable<>();
		LinkedHashMap<String, Object> hatemmorgan = new LinkedHashMap<>();

		hatemmorgan.put("type", "Developer");
		hatemmorgan.put("age", 20);
		hatemmorgan.put("firstName", "Hatem");
		hatemmorgan.put("middleName", "ELsayed");
		hatemmorgan.put("familyName", "Morgan");
		hatemmorgan.put("birthday", "27/7/1995");
		hatemmorgan.put("gender", "Male");
		hatemmorgan.put("title", "Engineer");
		hatemmorgan.put("userName", "HatemMorgans");

		ArrayList<Object> hatemmorganEmailList = new ArrayList<>();
		hatemmorganEmailList.add("hatemmorgan17s@gmail.com");
		hatemmorganEmailList.add("hatem.el-sayeds@student.guc.edu.eg");

		hatemmorgan.put("mbox", hatemmorganEmailList);
		hatemmorgan.put("knows", "karammorgan");
		hatemmorgan.put("job", "Computer Engineeer");

		LinkedHashMap<String, Object> ahmedmorgnan = new LinkedHashMap<>();
		ahmedmorgnan.put("type", "Developer");
		ahmedmorgnan.put("age", 16);
		ahmedmorgnan.put("firstName", "Ahmed");
		ahmedmorgnan.put("middleName", "ELsayed");
		ahmedmorgnan.put("familyName", "Morgan");
		ahmedmorgnan.put("birthday", "25/9/2000");
		ahmedmorgnan.put("gender", "Male");
		ahmedmorgnan.put("title", "Student");
		ahmedmorgnan.put("userName", "AhmedMorganl");

		ArrayList<Object> ahmedorganEmailList = new ArrayList<>();
		ahmedorganEmailList.add("ahmedmorganl@gmail.com");

		ahmedmorgnan.put("mbox", ahmedorganEmailList);
		ahmedmorgnan.put("job", "High School Student");
		ahmedmorgnan.put("love", hatemmorgan);

		// Haytham Ismail
		htblFieldValue.put("age", 50);
		htblFieldValue.put("firstName", "Haytham");
		htblFieldValue.put("middleName", "Ismail");
		htblFieldValue.put("familyName", "Khalf");
		htblFieldValue.put("birthday", "27/7/1975");
		htblFieldValue.put("gender", "Male");
		htblFieldValue.put("title", "Professor");
		htblFieldValue.put("userName", "HaythamIsmails");

		ArrayList<Object> emailList = new ArrayList<>();
		emailList.add("haytham.ismails@gmail.com");
		emailList.add("haytham.ismails@student.guc.edu.eg");

		htblFieldValue.put("mbox", emailList);

		htblFieldValue.put("adminOf", "TESTAPPLICATION");
		// htblFieldValue.put("knows", ahmedmorgnan);
		htblFieldValue.put("hates", ahmedmorgnan);
		// ArrayList<LinkedHashMap<String, Object>> loveList = new
		// ArrayList<>();
		// loveList.add(hatemmorgan2);
		// loveList.add(hatemmorgan);
		// htblFieldValue.put("love", loveList);
		// htblFieldValue.put("job", "Engineeer");
		PostRequestValidations requestFieldsValidation = new PostRequestValidations(validationDao,
				new DynamicConceptsUtility(dynamicConceptDao));

		MainDao mainDao = new MainDao(oracle, new SelectionQueryResults(new DynamicConceptsUtility(dynamicConceptDao)));

		AdminService adminService = new AdminService(requestFieldsValidation, new ApplicationDao(oracle), adminDao,
				mainDao);

		Hashtable<String, Object> res = adminService.getAdmins("TESTAPPLICATION");

		// Hashtable<String, Object> res =
		// adminService.insertAdmin(htblFieldValue, "TESTAPPLICATION");

		// Hashtable<String, Object>[] json = (Hashtable<String, Object>[])
		// res.get("errors");
		// System.out.println(json[0].toString());

		 System.out.println(res.toString());
	}
}
