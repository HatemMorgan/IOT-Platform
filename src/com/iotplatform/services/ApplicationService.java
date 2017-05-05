package com.iotplatform.services;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.DynamicOntologyDao;
import com.iotplatform.daos.InsertionDao;
import com.iotplatform.daos.ValidationDao;
import com.iotplatform.exceptions.CannotCreateApplicationModelException;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.models.SuccessfullInsertionModel;
import com.iotplatform.ontology.mapers.OntologyMapper;
import com.iotplatform.utilities.InsertionPropertyValue;
import com.iotplatform.validations.InsertRequestValidation;

import oracle.spatial.rdf.client.jena.Oracle;

@Service("applicationService")
public class ApplicationService {

	private ApplicationDao applicationDao;
	private InsertRequestValidation requestFieldsValidation;
	private InsertionDao insertionDao;

	@Autowired
	public ApplicationService(ApplicationDao applicationDao, InsertRequestValidation requestFieldsValidation,
			InsertionDao insertionDao) {
		this.applicationDao = applicationDao;
		this.requestFieldsValidation = requestFieldsValidation;
		this.insertionDao = insertionDao;
	}

	/*
	 * insertApplication method is responsible to call requestValidationService
	 * class to make sure that the request is valid then call the applicationDAO
	 * to make the insertion
	 */
	public LinkedHashMap<String, Object> insertApplication(LinkedHashMap<String, Object> htblPropValue) {
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

			String applicationModelName = applicationDao.getHtblApplicationNameModelName()
					.get(applicationName.toLowerCase().replaceAll(" ", ""));

			/*
			 * Check if the request is valid or not
			 */

			Hashtable<String, ArrayList<ArrayList<InsertionPropertyValue>>> htblClassPropertyValue = requestFieldsValidation
					.validateRequestFields(htblPropValue,
							OntologyMapper.getOntologyMapper().getHtblMainOntologyClassesMappers().get("application"),
							applicationModelName);

			insertionDao.insertData(applicationModelName,
					OntologyMapper.getHtblMainOntologyClassesMappers().get("application").getName(),
					htblClassPropertyValue);

			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			SuccessfullInsertionModel successModel = new SuccessfullInsertionModel("Application", timeTaken);
			return successModel.getResponseJson();

		} catch (ErrorObjException ex) {
			ex.printStackTrace();
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return ex.getExceptionHashTable(timeTaken);

		}

	}

	public LinkedHashMap<String, Object> dropApplication() {
		applicationDao.dropApplicationModel("Test Application");

		SuccessfullInsertionModel successModel = new SuccessfullInsertionModel("Application", 0);
		return successModel.getResponseJson();
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

		ValidationDao validationDao = new ValidationDao(oracle);

		DynamicOntologyDao dynamicOntologyDao = new DynamicOntologyDao(oracle);

		InsertRequestValidation requestFieldsValidation = new InsertRequestValidation(validationDao,
				dynamicOntologyDao);
		InsertionDao insertionDao = new InsertionDao(oracle);

		ApplicationDao applicationDao = new ApplicationDao(oracle);
		ApplicationService applicationService = new ApplicationService(applicationDao, requestFieldsValidation,
				insertionDao);

		LinkedHashMap<String, Object> htblPropValue = new LinkedHashMap<>();
		htblPropValue.put("name", "Test Application");
		htblPropValue.put("description", "Test App Description");

		// applicationDao.dropApplicationModel("Test Application");

		LinkedHashMap<String, Object> res = applicationService.insertApplication(htblPropValue);
		LinkedHashMap<String, Object>[] json = (LinkedHashMap<String, Object>[]) res.get("errors");
		System.out.println(json[0].toString());
		System.out.println(res.toString());

		System.out.println(applicationDao.getHtblApplicationNameModelName().toString());
		System.out.println(applicationService.dropApplication());
		System.out.println(applicationDao.getHtblApplicationNameModelName().toString());

	}
}
