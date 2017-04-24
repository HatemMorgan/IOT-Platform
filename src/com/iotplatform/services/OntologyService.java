package com.iotplatform.services;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.OntologyDao;
import com.iotplatform.exceptions.NoApplicationModelException;
import com.iotplatform.models.SuccessfullSelectAllJsonModel;

@Service("ontologyService")
public class OntologyService {

	private OntologyDao ontologyDao;
	private ApplicationDao applicationDao;

	@Autowired
	public OntologyService(OntologyDao ontologyDao, ApplicationDao applicationDao) {
		this.ontologyDao = ontologyDao;
		this.applicationDao = applicationDao;
	}

	public LinkedHashMap<String, Object> getApplicationOntology(String applicationName) {

		long startTime = System.currentTimeMillis();
		boolean exist = applicationDao.checkIfApplicationModelExsist(applicationName);

		/*
		 * check if the model exist or not .
		 */

		if (!exist) {
			NoApplicationModelException exception = new NoApplicationModelException(applicationName, "Ontology");
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return new SuccessfullSelectAllJsonModel(exception.getExceptionHashTable(timeTaken)).getJson();
		}

		/*
		 * get application modelName
		 */
		String applicationModelName = applicationDao.getHtblApplicationNameModelName().get(applicationName);
		return ontologyDao.loadApplicationOntology(applicationModelName);

	}

}
