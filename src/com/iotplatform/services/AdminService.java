package com.iotplatform.services;

import java.util.Hashtable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.AdminDao;
import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.NoApplicationModelException;
import com.iotplatform.models.SuccessfullInsertionModel;
import com.iotplatform.models.SuccessfullSelectAllJsonModel;
import com.iotplatform.ontology.classes.Admin;
import com.iotplatform.validations.RequestValidation;

@Service("adminService")
public class AdminService {

	private RequestValidation requestValidation;
	private ApplicationDao applicationDao;
	private AdminDao adminDao;
	private Admin adminClass;

	@Autowired
	public AdminService(RequestValidation requestValidation, ApplicationDao applicationDao, AdminDao adminDao,
			Admin adminClass) {
		this.requestValidation = requestValidation;
		this.applicationDao = applicationDao;
		this.adminDao = adminDao;
		this.adminClass = adminClass;
	}

	/*
	 * insertAdmin method is a service method that is responsible to take
	 * property values key pairs and call request validation to validate the
	 * request content then if it pass the validations call the admin data
	 * access object to insert the new admin
	 */

	public Hashtable<String, Object> insertAdmin(Hashtable<String, Object> htblPropValue, String applicationNameCode) {

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

		/*
		 * Check if the request is valid or not
		 */

		try {

			Hashtable<String, Object> htblPrefixedPropertyValue = requestValidation.isRequestValid(applicationNameCode,
					adminClass, htblPropValue);

			String applicationModelName = applicationDao.getHtblApplicationNameModelName().get(applicationNameCode);

			adminDao.insertAdmin(htblPrefixedPropertyValue, applicationModelName);
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			SuccessfullInsertionModel successModel = new SuccessfullInsertionModel("Application", timeTaken);
			return successModel.getResponseJson();

		} catch (ErrorObjException ex) {
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return ex.getExceptionHashTable(timeTaken);

		}
	}

	/*
	 * getAdmins method check if the application model is correct then it calls
	 * adminDao to get all admins  of this application
	 */
	public SuccessfullSelectAllJsonModel getAdmins(String applicationNameCode) {

		long startTime = System.currentTimeMillis();
		boolean exist = applicationDao.checkIfApplicationModelExsist(applicationNameCode);

		/*
		 * check if the model exist or not .
		 */

		if (!exist) {
			NoApplicationModelException exception = new NoApplicationModelException(applicationNameCode, "Admin");
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return new SuccessfullSelectAllJsonModel(exception.getExceptionHashTable(timeTaken));
		}

		try {

			List<Hashtable<String, Object>> htblPropValue = adminDao
					.getAdmins(applicationDao.getHtblApplicationNameModelName().get(applicationNameCode));

			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return new SuccessfullSelectAllJsonModel(htblPropValue, timeTaken);

		} catch (ErrorObjException e) {
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return new SuccessfullSelectAllJsonModel(e.getExceptionHashTable(timeTaken));

		}
	}

}
