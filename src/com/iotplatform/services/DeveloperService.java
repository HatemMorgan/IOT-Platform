package com.iotplatform.services;

import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.DeveloperDao;
import com.iotplatform.exceptions.CannotCreateApplicationModelException;
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

	public Hashtable<String, Object> insertDeveloper(Hashtable<String, Object> htblPropValue,
			String applicationCodeName) {

		boolean exist = applicationDao.checkIfApplicationModelExsist(applicationCodeName);
		
		return null;
	}

}
