package com.iotplatform.services;

import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.DeveloperDao;
import com.iotplatform.exceptions.CannotCreateApplicationModelException;
import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.NoApplicationModelException;
import com.iotplatform.models.SuccessfullInsertionModel;
import com.iotplatform.ontology.classes.Developer;
import com.iotplatform.validations.RequestValidation;

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
			String applicationCodeName) {

		long startTime = System.currentTimeMillis();
		boolean exist = applicationDao.checkIfApplicationModelExsist(applicationCodeName);

		/*
		 * check if the model exist or not .
		 */

		if (!exist) {
			NoApplicationModelException exception = new NoApplicationModelException(applicationCodeName, "Developer");
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return exception.getExceptionHashTable(timeTaken);
		}

		/*
		 * Check if the request is valid or not
		 */
		
		try {

			Hashtable<String, Object> htblPrefixedPropertyValue = requestValidation.isRequestValid(applicationCodeName,
					developerClass, htblPropValue);
			
				String applicationModelName = applicationDao.getHtblApplicationNameModelName().get(applicationCodeName);
				developerDao.InsertDeveloper(htblPropValue, applicationModelName);
				double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
				SuccessfullInsertionModel successModel = new SuccessfullInsertionModel("Application", timeTaken);
				return successModel.getResponseJson();

			

		} catch (ErrorObjException ex) {
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return ex.getExceptionHashTable(timeTaken);

		}
	}

}
