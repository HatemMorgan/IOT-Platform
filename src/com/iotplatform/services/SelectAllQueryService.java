package com.iotplatform.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.DynamicOntologyDao;
import com.iotplatform.daos.SelectAllQueryDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.InvalidClassNameException;
import com.iotplatform.exceptions.NoApplicationModelException;
import com.iotplatform.models.SuccessfullSelectAllJsonModel;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.mapers.DynamicOntologyMapper;
import com.iotplatform.ontology.mapers.OntologyMapper;
import com.iotplatform.query.results.SelectionQueryResults;

import oracle.spatial.rdf.client.jena.Oracle;

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
	private DynamicOntologyDao dynamicOntologyDao;

	@Autowired
	public SelectAllQueryService(ApplicationDao applicationDao, SelectAllQueryDao selectAllQueryDao,
			DynamicOntologyDao dynamicOntologyDao) {
		this.applicationDao = applicationDao;
		this.selectAllQueryDao = selectAllQueryDao;
		this.dynamicOntologyDao = dynamicOntologyDao;
	}

	/*
	 * selectAll method is used :
	 * 
	 * 1- validate that applicationName and className passed by the request are
	 * valid 2- call selectAllQueryDao to perform a selectAll query on the data
	 * of the passed className in the applicationModel of the passed
	 * applicationNameCode
	 */
	public LinkedHashMap<String, Object> selectAll(String applicationNameCode, String className) {

		long startTime = System.currentTimeMillis();

		try {

			boolean exist = applicationDao.checkIfApplicationModelExsist(applicationNameCode);

			className = className.toLowerCase().replaceAll(" ", "");

			/*
			 * check if the model exist or not .
			 */

			if (!exist) {
				NoApplicationModelException exception = new NoApplicationModelException(applicationNameCode, className);
				double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
				return new SuccessfullSelectAllJsonModel(exception.getExceptionHashTable(timeTaken)).getJson();
			}

			/*
			 * get application modelName
			 */
			String applicationModelName = applicationDao.getHtblApplicationNameModelName()
					.get(applicationNameCode.toLowerCase().replaceAll(" ", ""));

			/*
			 * check if the className has a valid class Mapping
			 */
			Class subjectClass = null;
			if (OntologyMapper.getOntologyMapper().getHtblMainOntologyClassesMappers().containsKey(className)) {
				subjectClass = OntologyMapper.getHtblMainOntologyClassesMappers().get(className);
			} else {

				/*
				 * The class is not from MainOntology so check it in
				 * DynamicOntology cache of this application
				 */
				if ((DynamicOntologyMapper.getHtblappDynamicOntologyClasses().containsKey(applicationModelName)
						&& DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
								.containsKey(className))) {
					subjectClass = DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
							.get(className);
				} else {

					/*
					 * It doesnot exist so It might not cached before so I will
					 * load and cache it if its a valid class
					 */
					ArrayList<String> classNameListToBeloaded = new ArrayList<>();

					dynamicOntologyDao.loadAndCacheDynamicClassesofApplicationDomain(applicationModelName,
							classNameListToBeloaded);

					if (DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
							.containsKey(className)) {
						subjectClass = DynamicOntologyMapper.getHtblappDynamicOntologyClasses()
								.get(applicationModelName).get(className);
					} else {

						/*
						 * Not a valid class so return an error to the user
						 */
						double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
						InvalidClassNameException invalidClassNameException = new InvalidClassNameException(className);
						return invalidClassNameException.getExceptionHashTable(timeTaken);
					}

				}

			}

			List<LinkedHashMap<String, Object>> htblPropValue = selectAllQueryDao.selectAll(applicationModelName,
					subjectClass);

			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return new SuccessfullSelectAllJsonModel(htblPropValue, timeTaken).getJson();

		} catch (ErrorObjException e) {
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return new SuccessfullSelectAllJsonModel(e.getExceptionHashTable(timeTaken)).getJson();

		}

	}

	public static void main(String[] args) {
		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		String szUser = "rdfusr";
		String szPasswd = "rdfusr";

		Oracle oracle = new Oracle(szJdbcURL, szUser, szPasswd);

		ApplicationDao applicationDao = new ApplicationDao(oracle);

		DynamicOntologyDao dynamicOntologyDao = new DynamicOntologyDao(oracle);

		SelectAllQueryDao selectAllQueryDao = new SelectAllQueryDao(oracle,
				new SelectionQueryResults(dynamicOntologyDao));

		SelectAllQueryService selectAllQueryService = new SelectAllQueryService(applicationDao, selectAllQueryDao,
				dynamicOntologyDao);

		System.out.println(selectAllQueryService.selectAll("test application", "admin"));

	}

}
