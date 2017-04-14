package com.iotplatform.services;

import java.util.Hashtable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.SelectAllQueryDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.InvalidClassNameException;
import com.iotplatform.exceptions.NoApplicationModelException;
import com.iotplatform.models.SuccessfullSelectAllJsonModel;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.mapers.OntologyMapper;

/*
 * InsertionService is used to serve SelectAllQueryAPIController to get data
 * 
 * 
 * 1- It calls the SelectAllQueryDao which is responsible to query database and return results
 * 2- It takes the results and passed it to SelectAllQueryAPIController
 * 
 */

@Service("selectAllQueryService")
public class SelectAllQueryService {

	private ApplicationDao applicationDao;
	private SelectAllQueryDao selectAllQueryDao;

	@Autowired
	public SelectAllQueryService(ApplicationDao applicationDao, SelectAllQueryDao selectAllQueryDao) {
		this.applicationDao = applicationDao;
		this.selectAllQueryDao = selectAllQueryDao;
	}

	/*
	 * selectAll method is used :
	 * 
	 * 1- validate that applicationName and className passed by the request are
	 * valid 2- call selectAllQueryDao to perform a selectAll query on the data
	 * of the passed className in the applicationModel of the passed
	 * applicationNameCode
	 */
	public Hashtable<String, Object> selectAll(String applicationNameCode, String className) {

		long startTime = System.currentTimeMillis();
		boolean exist = applicationDao.checkIfApplicationModelExsist(applicationNameCode);

		className = className.toLowerCase().replaceAll(" ", "");
		/*
		 * check if the className has a valid class Mapping
		 */
		if (OntologyMapper.getHtblMainOntologyClassesMappers().containsKey(className)) {
			Class subjectClass = OntologyMapper.getHtblMainOntologyClassesMappers().get(className);

			/*
			 * check if the model exist or not .
			 */

			if (!exist) {
				NoApplicationModelException exception = new NoApplicationModelException(applicationNameCode,
						subjectClass.getName());
				double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
				return new SuccessfullSelectAllJsonModel(exception.getExceptionHashTable(timeTaken)).getJson();
			}

			try {

				/*
				 * get application modelName
				 */
				String applicationModelName = applicationDao.getHtblApplicationNameModelName().get(applicationNameCode);

				List<Hashtable<String, Object>> htblPropValue = selectAllQueryDao.selectAll(applicationModelName,
						subjectClass);

				double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
				return new SuccessfullSelectAllJsonModel(htblPropValue, timeTaken).getJson();

			} catch (ErrorObjException e) {
				double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
				return new SuccessfullSelectAllJsonModel(e.getExceptionHashTable(timeTaken)).getJson();

			}

		} else {
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			InvalidClassNameException invalidClassNameException = new InvalidClassNameException(className);
			return invalidClassNameException.getExceptionHashTable(timeTaken);
		}

	}

}
