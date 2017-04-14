package com.iotplatform.services;

import java.util.ArrayList;
import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.MainDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.InvalidClassNameException;
import com.iotplatform.models.SuccessfullInsertionModel;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.mapers.OntologyMapper;
import com.iotplatform.utilities.PropertyValue;
import com.iotplatform.validations.InsertRequestValidations;
import com.iotplatform.validations.QueryRequestValidations;

/*
 * InsertionService is used to serve InsertionAPIController to insert new data 
 * 
 * 1- It calls InsertRequestValidation class to validate the request and parse the request body
 * 2- It calls the InsertionDao which is responsible to query database and return results
 * 3- It takes the results and passed it to InsertionAPIController
 */

@Service("insertionService")
public class InsertionService {

	private InsertRequestValidations insertRequestValidations;
	private ApplicationDao applicationDao;
	private MainDao mainDao;

	@Autowired
	public InsertionService(InsertRequestValidations insertRequestValidations, ApplicationDao applicationDao,
			MainDao mainDao, QueryRequestValidations getQueryRequestValidations) {
		this.insertRequestValidations = insertRequestValidations;
		this.applicationDao = applicationDao;
		this.mainDao = mainDao;

	}

	/*
	 * insertNewFieldValueList method is a service method that is responsible to
	 * take property values key pairs and call request validation to validate
	 * the request content then if it pass the validations call the admin data
	 * access object to insert the new admin
	 */
	public Hashtable<String, Object> insertNewFieldValueList(Hashtable<String, Object> htblFieldValue,
			String applicationNameCode, String className) {

		long startTime = System.currentTimeMillis();
		className = className.toLowerCase().replaceAll(" ", "");

		/*
		 * check if the className has a valid class Mapping
		 */
		if (OntologyMapper.getHtblMainOntologyClassesMappers().containsKey(className)) {

			try {

				Class subjectClass = OntologyMapper.getHtblMainOntologyClassesMappers().get(className);

				/*
				 * Check if the request is valid or not
				 */
				Hashtable<Class, ArrayList<ArrayList<PropertyValue>>> htblClassPropertyValue = insertRequestValidations
						.validateRequestFields(applicationNameCode, htblFieldValue, subjectClass);

				/*
				 * get application modelName
				 */
				String applicationModelName = applicationDao.getHtblApplicationNameModelName().get(applicationNameCode);

				mainDao.insertData(applicationModelName, subjectClass.getName(), htblClassPropertyValue);

				double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
				SuccessfullInsertionModel successModel = new SuccessfullInsertionModel(subjectClass.getName(),
						timeTaken);
				return successModel.getResponseJson();

			} catch (ErrorObjException ex) {
				double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
				return ex.getExceptionHashTable(timeTaken);

			}

		} else

		{
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			InvalidClassNameException invalidClassNameException = new InvalidClassNameException(className);
			return invalidClassNameException.getExceptionHashTable(timeTaken);
		}

	}

}
