package com.iotplatform.services;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.DynamicOntologyDao;
import com.iotplatform.daos.InsertionDao;
import com.iotplatform.daos.ValidationDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.InvalidClassNameException;
import com.iotplatform.exceptions.NoApplicationModelException;
import com.iotplatform.models.SuccessfullInsertionModel;
import com.iotplatform.models.SuccessfullSelectAllJsonModel;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.mapers.DynamicOntologyMapper;
import com.iotplatform.ontology.mapers.OntologyMapper;
import com.iotplatform.utilities.InsertionPropertyValue;
import com.iotplatform.validations.InsertRequestValidation;

import oracle.spatial.rdf.client.jena.Oracle;

/*
 * InsertionService is used to serve InsertionAPIController to insert new data 
 * 
 * 1- It calls InsertRequestValidation class to validate the request and parse the request body
 * 2- It calls the InsertionDao which is responsible to insert data into database 
 * 3- It takes the results and passed it to InsertionAPIController
 */

@Service("insertionService")
public class InsertionService {

	private InsertRequestValidation insertRequestValidations;
	private ApplicationDao applicationDao;
	private InsertionDao insertionDao;
	private DynamicOntologyDao dynamicOntologyDao;

	@Autowired
	public InsertionService(InsertRequestValidation insertRequestValidations, ApplicationDao applicationDao,
			InsertionDao insertionDao, DynamicOntologyDao dynamicOntologyDao) {
		this.insertRequestValidations = insertRequestValidations;
		this.applicationDao = applicationDao;
		this.insertionDao = insertionDao;
		this.dynamicOntologyDao = dynamicOntologyDao;

	}

	/*
	 * insertNewFieldValueList method is a service method that is responsible to
	 * take property values key pairs and call request validation to validate
	 * the request content then if it pass the validations call the admin data
	 * access object to insert the new admin
	 */
	public LinkedHashMap<String, Object> insertNewFieldValueList(LinkedHashMap<String, Object> htblFieldValue,
			String applicationNameCode, String className) {

		long startTime = System.currentTimeMillis();
		className = className.toLowerCase().replaceAll(" ", "");

		try {
			boolean exist = applicationDao.checkIfApplicationModelExsist(applicationNameCode);

			/*
			 * check if the model exist or not .
			 */
			if (!exist) {
				NoApplicationModelException exception = new NoApplicationModelException(applicationNameCode, className);
				double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
				return new SuccessfullSelectAllJsonModel(exception.getExceptionHashTable(timeTaken)).getJson();
			}

			/*
			 * get application modelName
			 */
			String applicationModelName = applicationDao.getHtblApplicationNameModelName()
					.get(applicationNameCode.toLowerCase().replaceAll(" ", ""));

			/*
			 * check if the className has a valid class Mapping
			 */
			Class subjectClass = null;
			if (OntologyMapper.getOntologyMapper().getHtblMainOntologyClassesMappers().containsKey(className)) {
				subjectClass = OntologyMapper.getHtblMainOntologyClassesMappers().get(className);
			} else {

				/*
				 * The class is not from MainOntology so check it in
				 * DynamicOntology cache of this application
				 */
				if ((DynamicOntologyMapper.getHtblappDynamicOntologyClasses().containsKey(applicationModelName)
						&& DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
								.containsKey(className))) {
					subjectClass = DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
							.get(className);
				} else {

					/*
					 * It doesnot exist so It might not cached before so I will
					 * load and cache it if its a valid class
					 */
					ArrayList<String> classNameListToBeloaded = new ArrayList<>();

					dynamicOntologyDao.loadAndCacheDynamicClassesofApplicationDomain(applicationModelName,
							classNameListToBeloaded);

					if (DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
							.containsKey(className)) {
						subjectClass = DynamicOntologyMapper.getHtblappDynamicOntologyClasses()
								.get(applicationModelName).get(className);
					} else {

						/*
						 * Not a valid class so return an error to the user
						 */
						double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
						InvalidClassNameException invalidClassNameException = new InvalidClassNameException(className);
						return invalidClassNameException.getExceptionHashTable(timeTaken);
					}

				}

			}

			/*
			 * Check if the request is valid or not
			 */
			Hashtable<String, ArrayList<ArrayList<InsertionPropertyValue>>> htblClassPropertyValue = insertRequestValidations
					.validateRequestFields(applicationNameCode, htblFieldValue, subjectClass, applicationModelName);
			insertionDao.insertData(applicationModelName, subjectClass.getName(), htblClassPropertyValue);

			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			SuccessfullInsertionModel successModel = new SuccessfullInsertionModel(subjectClass.getName(), timeTaken);
			return successModel.getResponseJson();

		} catch (ErrorObjException ex) {
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return ex.getExceptionHashTable(timeTaken);
		}

	}

	public static void main(String[] args) {

		LinkedHashMap<String, Object> htblFieldValue = new LinkedHashMap<>();

		LinkedHashMap<String, Object> hatemmorgan = new LinkedHashMap<>();

		hatemmorgan.put("type", "NormalUser");
		hatemmorgan.put("age", 20);
		hatemmorgan.put("firstName", "Hatem");
		hatemmorgan.put("middleName", "ELsayed");
		hatemmorgan.put("familyName", "Morgan");
		hatemmorgan.put("birthday", "27/7/1995");
		hatemmorgan.put("gender", "Male");
		hatemmorgan.put("title", "Engineer");
		hatemmorgan.put("userName", "HatemMorganss");

		ArrayList<Object> hatemmorganEmailList = new ArrayList<>();
		hatemmorganEmailList.add("hatemmorgan17ss@gmail.com");
		hatemmorganEmailList.add("hatem.el-sayedss@student.guc.edu.eg");

		hatemmorgan.put("mbox", hatemmorganEmailList);
		// hatemmorgan.put("knows", "karammorgan");
		// hatemmorgan.put("job", "Computer Engineeer");

		LinkedHashMap<String, Object> ahmedmorgnan = new LinkedHashMap<>();

		ahmedmorgnan.put("type", "Developer");
		ahmedmorgnan.put("age", 16);
		ahmedmorgnan.put("firstName", "Ahmed");
		ahmedmorgnan.put("middleName", "ELsayed");
		ahmedmorgnan.put("familyName", "Morgan");
		ahmedmorgnan.put("birthday", "25/9/2000");
		ahmedmorgnan.put("gender", "Male");
		ahmedmorgnan.put("title", "Student");
		ahmedmorgnan.put("userName", "AhmedMorganls");

		ArrayList<Object> ahmedorganEmailList = new ArrayList<>();
		ahmedorganEmailList.add("ahmedmorganlss@gmail.com");

		ahmedmorgnan.put("mbox", ahmedorganEmailList);
		ahmedmorgnan.put("job", "High School Student");
		ahmedmorgnan.put("loves", hatemmorgan);

		// Haytham Ismail
		htblFieldValue.put("age", 50);
		htblFieldValue.put("firstName", "Haytham");
		htblFieldValue.put("middleName", "Ismail");
		htblFieldValue.put("familyName", "Khalf");
		htblFieldValue.put("birthday", "27/7/1975");
		htblFieldValue.put("gender", "Male");
		htblFieldValue.put("title", "Professor");
		htblFieldValue.put("userName", "HaythamIsmailss");

		ArrayList<Object> emailList = new ArrayList<>();
		emailList.add("haytham.ismailss@gmail.com");
		emailList.add("haytham.ismailss@student.guc.edu.eg");

		htblFieldValue.put("mbox", emailList);

		htblFieldValue.put("adminOf", "TESTAPPLICATION");
		htblFieldValue.put("hates", ahmedmorgnan);

		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		String szUser = "rdfusr";
		String szPasswd = "rdfusr";

		Oracle oracle = new Oracle(szJdbcURL, szUser, szPasswd);
		ValidationDao validationDao = new ValidationDao(oracle);

		DynamicOntologyDao dynamicOntologyDao = new DynamicOntologyDao(oracle);

		System.out.println("Connected to Database");

		InsertRequestValidation insertRequestValidation = new InsertRequestValidation(validationDao,
				dynamicOntologyDao);

		ApplicationDao applicationDao = new ApplicationDao(oracle);

		InsertionDao insertionDao = new InsertionDao(oracle);

		InsertionService insertionService = new InsertionService(insertRequestValidation, applicationDao, insertionDao,
				dynamicOntologyDao);

		insertionService.insertNewFieldValueList(htblFieldValue, "TESTAPPLICATION", "Admin");

	}

}
