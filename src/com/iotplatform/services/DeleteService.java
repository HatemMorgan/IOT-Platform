package com.iotplatform.services;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.DeleteDao;
import com.iotplatform.daos.DynamicOntologyDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.InvalidClassNameException;
import com.iotplatform.exceptions.NoApplicationModelException;
import com.iotplatform.models.SuccessfullDeleteJSONModel;
import com.iotplatform.models.SuccessfullInsertionModel;
import com.iotplatform.models.SuccessfullSelectAllJsonModel;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.mapers.DynamicOntologyMapper;
import com.iotplatform.ontology.mapers.OntologyMapper;
import com.iotplatform.utilities.DeletePropertyValueUtility;
import com.iotplatform.validations.DeleteRequestValidation;

import oracle.spatial.rdf.client.jena.Oracle;

@Service("deleteService")
public class DeleteService {

	private DeleteRequestValidation deleteRequestValidation;
	private DeleteDao deleteDao;
	private ApplicationDao applicationDao;
	private DynamicOntologyDao dynamicOntologyDao;

	@Autowired
	public DeleteService(DeleteRequestValidation deleteRequestValidation, DeleteDao deleteDao,
			ApplicationDao applicationDao) {

		this.deleteRequestValidation = deleteRequestValidation;
		this.deleteDao = deleteDao;
		this.applicationDao = applicationDao;
	}

	public LinkedHashMap<String, Object> deleteData(String className, String applicationName,
			String individualUniqueIdentifier, LinkedHashMap<String, Object> htblRequestBody) {

		long startTime = System.currentTimeMillis();
		className = className.toLowerCase().replaceAll(" ", "");

		try {
			boolean exist = applicationDao.checkIfApplicationModelExsist(applicationName);

			/*
			 * check if the model exist or not .
			 */
			if (!exist) {
				NoApplicationModelException exception = new NoApplicationModelException(applicationName, className);
				double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
				return new SuccessfullSelectAllJsonModel(exception.getExceptionHashTable(timeTaken)).getJson();
			}

			/*
			 * get application modelName
			 */
			String applicationModelName = applicationDao.getHtblApplicationNameModelName()
					.get(applicationName.toLowerCase().replaceAll(" ", ""));

			/*
			 * check if the className has a valid class Mapping
			 */
			Class subjectClass = null;
			if ((DynamicOntologyMapper.getHtblappDynamicOntologyClasses().containsKey(applicationModelName)
					&& DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
							.containsKey(className))) {
				subjectClass = DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
						.get(className);

			} else {

				/*
				 * The class is not in dynamicOntology cache of requested
				 * application so check it in main ontology
				 */

				if (OntologyMapper.getOntologyMapper().getHtblMainOntologyClassesMappers().containsKey(className)) {
					subjectClass = OntologyMapper.getHtblMainOntologyClassesMappers().get(className);
				} else {

					/*
					 * It doesnot exist so It might not cached before so I will
					 * load and cache it if its a valid class
					 */
					Hashtable<String, String> htbClassNameToBeloaded = new Hashtable<>();
					htbClassNameToBeloaded.put(className, className);

					dynamicOntologyDao.loadAndCacheDynamicClassesofApplicationDomain(applicationModelName,
							htbClassNameToBeloaded);

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

			if (htblRequestBody.isEmpty()) {
				deleteDao.deleteIndividual(applicationModelName, individualUniqueIdentifier, subjectClass);
			} else {

				ArrayList<DeletePropertyValueUtility> deletePropValueList = deleteRequestValidation
						.validateDeleteRequest(applicationModelName, htblRequestBody, subjectClass);

				deleteDao.deletePatternOfIndividual(applicationModelName, individualUniqueIdentifier, subjectClass,
						deletePropValueList);
			}

			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			SuccessfullDeleteJSONModel successModel = new SuccessfullDeleteJSONModel(subjectClass.getName(), timeTaken);
			return successModel.getResponseJson();

		} catch (ErrorObjException ex) {
			System.out.println(ex.getExceptionMessage());
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return ex.getExceptionHashTable(timeTaken);
		}

	}

	public static void main(String[] args) {

		LinkedHashMap<String, Object> htbRequestBody = new LinkedHashMap<>();
		ArrayList<Object> deleteList = new ArrayList<>();
//		htbRequestBody.put("delete", deleteList);

		deleteList.add("firstName");
		deleteList.add("title");
		deleteList.add("job");
		// deleteList.add("userName");

		LinkedHashMap<String, Object> htblMbox = new LinkedHashMap<>();
		htblMbox.put("fieldName", "mbox");
		htblMbox.put("value", "karim.mohamed@gmail.com");
		deleteList.add(htblMbox);

		LinkedHashMap<String, Object> htblHates = new LinkedHashMap<>();
		htblHates.put("fieldName", "hates");
		htblHates.put("value", "OmarTag");
		deleteList.add(htblHates);

		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		String szUser = "rdfusr";
		String szPasswd = "rdfusr";

		Oracle oracle = new Oracle(szJdbcURL, szUser, szPasswd);

		DynamicOntologyDao dynamicOntologyDao = new DynamicOntologyDao(oracle);

		DeleteRequestValidation deleteRequestValidation = new DeleteRequestValidation(dynamicOntologyDao);

		ApplicationDao applicationDao = new ApplicationDao(oracle);

		DeleteDao deleteDao = new DeleteDao(oracle);

		DeleteService deleteService = new DeleteService(deleteRequestValidation, deleteDao, applicationDao);

		System.out.println(deleteService.deleteData("Developer", "test application", "EL3ankboots", htbRequestBody));

	}
}
