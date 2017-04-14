package com.iotplatform.services;

import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.DynamicConceptsDao;
import com.iotplatform.daos.MainDao;
import com.iotplatform.daos.ValidationDao;
import com.iotplatform.exceptions.CannotCreateApplicationModelException;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.models.SuccessfullInsertionModel;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.dynamicConcepts.DynamicConceptsUtility;
import com.iotplatform.ontology.mapers.OntologyMapper;
import com.iotplatform.query.results.SelectionQueryResults;
import com.iotplatform.utilities.PropertyValue;
import com.iotplatform.validations.InsertRequestValidations;

import oracle.spatial.rdf.client.jena.Oracle;

@Service("applicationService")
public class ApplicationService {

	private ApplicationDao applicationDao;
	private InsertRequestValidations requestFieldsValidation;
	private MainDao mainDao;

	@Autowired
	public ApplicationService(ApplicationDao applicationDao, InsertRequestValidations requestFieldsValidation,
			MainDao mainDao) {
		this.applicationDao = applicationDao;
		this.requestFieldsValidation = requestFieldsValidation;
		this.mainDao = mainDao;
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
		try {
			if (htblPropValue.containsKey("name")) {
				Object value = htblPropValue.get("name");

				if (value instanceof String) {
					boolean exist = applicationDao.checkIfApplicationModelExsist(value.toString());
					if (!exist) {
						applicationName = value.toString();

						/*
						 * create application model
						 */
						applicationDao.createNewApplicationModel(applicationName);
					} else {
						CannotCreateApplicationModelException err = new CannotCreateApplicationModelException(
								"There is an application exist with this name . application name has to be unique",
								"Application");
						double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
						return err.getExceptionHashTable(timeTaken);
					}
				}
			}

			/*
			 * Check if the request is valid or not
			 */

			Hashtable<Class, ArrayList<ArrayList<PropertyValue>>> htblClassPropertyValue = requestFieldsValidation
					.validateRequestFields(applicationName, htblPropValue,
							OntologyMapper.getHtblMainOntologyClassesMappers().get("application"));

			mainDao.insertData(applicationDao.getHtblApplicationNameModelName().get(applicationName),
					OntologyMapper.getHtblMainOntologyClassesMappers().get("application").getName(),
					htblClassPropertyValue);

			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			SuccessfullInsertionModel successModel = new SuccessfullInsertionModel("Application", timeTaken);
			return successModel.getResponseJson();

		} catch (ErrorObjException ex) {
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return ex.getExceptionHashTable(timeTaken);

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

		InsertRequestValidations requestFieldsValidation = new InsertRequestValidations(validationDao,
				new DynamicConceptsUtility(dynamicConceptDao));
		MainDao mainDao = new MainDao(oracle, new SelectionQueryResults(new DynamicConceptsUtility(dynamicConceptDao)));

		ApplicationDao applicationDao = new ApplicationDao(oracle);
		ApplicationService applicationService = new ApplicationService(applicationDao, requestFieldsValidation,
				mainDao);

		Hashtable<String, Object> htblPropValue = new Hashtable<>();
		htblPropValue.put("name", "Test Applications");
		htblPropValue.put("description", "Test App Description");

		// applicationDao.dropApplicationModel("Test Application");

		Hashtable<String, Object> res = applicationService.insertApplication(htblPropValue);
		// Hashtable<String, Object>[] json = (Hashtable<String, Object>[])
		// res.get("errors");
		// System.out.println(json[0].toString());
		System.out.println(res.toString());

	}
}
