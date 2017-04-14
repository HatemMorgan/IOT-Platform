package com.iotplatform.services;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.MainDao;
import com.iotplatform.daos.SelectQueryDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.InvalidClassNameException;
import com.iotplatform.exceptions.NoApplicationModelException;
import com.iotplatform.models.SuccessfullSelectAllJsonModel;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.mapers.OntologyMapper;
import com.iotplatform.utilities.QueryField;
import com.iotplatform.validations.SelectQueryRequestValidation;

@Service("selectQueryService")
public class SelectQueryService {

	private SelectQueryRequestValidation selectQueryRequestValidation;
	private ApplicationDao applicationDao;
	private SelectQueryDao selectQueryDao;

	
	@Autowired
	public SelectQueryService(SelectQueryRequestValidation selectQueryRequestValidation, ApplicationDao applicationDao,
			SelectQueryDao selectQueryDao) {
		this.selectQueryRequestValidation = selectQueryRequestValidation;
		this.applicationDao = applicationDao;
		this.selectQueryDao = selectQueryDao;
	}



	public Hashtable<String, Object> QueryData(String applicationNameCode, String className,
			Hashtable<String, Object> htblFieldValue) {

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
				 * validate query request
				 */
				LinkedHashMap<String, LinkedHashMap<String, ArrayList<QueryField>>> htblClassNameProperty = selectQueryRequestValidation
						.validateRequest(applicationNameCode, htblFieldValue, subjectClass);

				/*
				 * get application modelName
				 */
				String applicationModelName = applicationDao.getHtblApplicationNameModelName().get(applicationNameCode);

				List<Hashtable<String, Object>> resultsList = selectQueryDao.queryData(htblClassNameProperty,
						applicationModelName);

				double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
				return new SuccessfullSelectAllJsonModel(resultsList, timeTaken).getJson();

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
