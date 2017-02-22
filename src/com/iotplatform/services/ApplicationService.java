package com.iotplatform.services;

import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.ValidationDao;
import com.iotplatform.exceptions.CannotCreateApplicationModelException;
import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.exceptions.InvalidPropertyValuesException;
import com.iotplatform.exceptions.InvalidRequestFieldsException;
import com.iotplatform.models.SuccessfullInsertionModel;
import com.iotplatform.ontology.classes.Application;
import com.iotplatform.validations.RequestValidationService;

import oracle.spatial.rdf.client.jena.Oracle;

@Service("applicationService")
public class ApplicationService {

	private ApplicationDao applicationDao;
	private RequestValidationService requestValidationService;
	private Application applicationClass;

	@Autowired
	public ApplicationService(ApplicationDao applicationDao, RequestValidationService requestValidationService,
			Application applicationClass) {
		this.applicationDao = applicationDao;
		this.requestValidationService = requestValidationService;
		this.applicationClass = applicationClass;
	}

	/*
	 * insertApplication method is responsible to call requestValidationService
	 * class to make sure that the request is valid then call the applicationDAO
	 * to make the insertion
	 */

	public Hashtable<String, Object> insertApplication(Hashtable<String, Object> htblPropValue) {
		long startTime = System.currentTimeMillis();
		String applicationName = "";
		
		/*
		 * check if the model exist or not . it pass the application name after
		 * checking that the passed request has property name and its value is a
		 * String
		 */

		if (htblPropValue.containsKey("name")) {
			Object value = htblPropValue.get("name");

			if (value instanceof String) {
				boolean exist = applicationDao.checkIfApplicationModelExsist(value.toString());
				if (!exist) {
					applicationName = value.toString();
				} else {
					CannotCreateApplicationModelException err = new CannotCreateApplicationModelException(
							"There is an application exist with this name . application name has to be unique",
							"Application");
					return err.getExceptionHashTable();
				}
			}
		}

		/*
		 * Check if the fields are valid
		 */

		boolean isValid = requestValidationService.isFieldsValid(applicationClass, htblPropValue);

		/*
		 * if all the fields passed by the request valid check properties'
		 * values (object value or data type value) are valid
		 */
		if (isValid) {
			isValid = requestValidationService.isPropertiesValid(applicationName, applicationClass, htblPropValue);
		} else {
			InvalidRequestFieldsException err = new InvalidRequestFieldsException("Application");
			return err.getExceptionHashTable();
		}

		/*
		 * if the request fields are valid and objects of the properties valid
		 * then we are able to insert data to the application's model
		 */
		if (isValid) {

			try {
				Hashtable<String, Object> htblPrefixedPropertyValue = requestValidationService.getPrefixedProperties(htblPropValue, applicationClass);
				applicationDao.insertApplication(htblPrefixedPropertyValue, applicationName);
				double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
				SuccessfullInsertionModel successModel = new SuccessfullInsertionModel("Application",timeTaken);
				return successModel.getResponseJson();
			} catch (DatabaseException ex) {
				return ex.getExceptionHashTable();
			}

		} else {
			InvalidPropertyValuesException err = new InvalidPropertyValuesException("Application");
			return err.getExceptionHashTable();
		}

	}

	public static void main(String[] args) {
		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		String szUser = "rdfusr";
		String szPasswd = "rdfusr";

		Application application = new Application();
		Oracle oracle = new Oracle(szJdbcURL, szUser, szPasswd);

		ApplicationDao applicationDao = new ApplicationDao(oracle, application);
		ApplicationService applicationService = new ApplicationService(applicationDao,
				new RequestValidationService(new ValidationDao(oracle)), application);

		Hashtable<String, Object> htblPropValue = new Hashtable<>();
		htblPropValue.put("name", "Test Application");
		htblPropValue.put("description", "Test App Description");
		
		applicationDao.dropApplicationModel("Test Application");
		
//		long startTime = System.currentTimeMillis();
//		System.out.println("Started at : " + startTime / 1000);
//		Hashtable<String,Object> res =  applicationService.insertApplication(htblPropValue);
//		System.out.println(
//				"test inserting: elapsed time (sec): " + ((System.currentTimeMillis() - startTime) / 1000));
//		System.out.println(res.toString());

	}
}
