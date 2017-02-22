package com.iotplatform.services;

import java.util.Hashtable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.DynamicConceptDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.models.DynamicConceptModel;
import com.iotplatform.models.SuccessfullInsertionModel;

@Service("dynamicConceptService")
public class DynamicConceptService {

	DynamicConceptDao dynamicConceptDao;

	@Autowired
	public DynamicConceptService(DynamicConceptDao dynamicConceptDao) {
		this.dynamicConceptDao = dynamicConceptDao;
	}

	/*
	 * insertNewConcept service method is used to call dynamicConceptDao and it
	 * return a json object
	 */
	public Hashtable<String, Object> insertNewConcept(String applicationName, DynamicConceptModel newConcept) {
		try {

			/*
			 * check if the applicationName is not the same as newConcept
			 * application name domain
			 */

			if (!applicationName.equals(newConcept.getApplication_name())) {
				ErrorObjException err = new ErrorObjException(HttpStatus.BAD_REQUEST.name(),
						HttpStatus.BAD_REQUEST.value(), "Wrong Application name ", "Ontology");
				return err.getExceptionHashTable();
			}

			dynamicConceptDao.insertNewConcept(newConcept);
			SuccessfullInsertionModel successModel = new SuccessfullInsertionModel("New Ontology Concept");
			return successModel.getResponseJson();
		} catch (ErrorObjException e) {
			return e.getExceptionHashTable();
		}
	}

	public Hashtable<String, Object> getApplicationDynamicConcepts(String applicationName) {
		
		try{
			List<DynamicConceptModel> concepts = dynamicConceptDao.getConceptsOfApplication(applicationName);
			Hashtable<String, Object> json = new Hashtable<>();
			json.put("dynamicAddedConcepts",concepts);
			return json;
		}catch (ErrorObjException e) {
			return e.getExceptionHashTable();
		}
		
	}

}
