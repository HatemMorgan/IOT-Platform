package com.iotplatform.services;

import java.util.Hashtable;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.DynamicOntologyDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.InvalidClassNameException;
import com.iotplatform.exceptions.InvalidUpdateRequestBodyException;
import com.iotplatform.exceptions.NoApplicationModelException;
import com.iotplatform.models.SuccessfullSelectAllJsonModel;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.mapers.DynamicOntologyMapper;
import com.iotplatform.ontology.mapers.OntologyMapper;
import com.iotplatform.utilities.UpdateRequestValidationResultUtility;
import com.iotplatform.validations.InsertRequestValidation;
import com.iotplatform.validations.UpdateRequestValidation;

@Service("updatingService")
public class UpdatingService {

	private DynamicOntologyDao dynamicOntologyDao;
	private ApplicationDao applicationDao;
	private InsertRequestValidation insertRequestValidation;
	private UpdateRequestValidation updateRequestValidation;

	@Autowired
	public UpdatingService(DynamicOntologyDao dynamicOntologyDao, ApplicationDao applicationDao,
			InsertRequestValidation insertRequestValidation, UpdateRequestValidation updateRequestValidation) {
		this.dynamicOntologyDao = dynamicOntologyDao;
		this.applicationDao = applicationDao;
		this.insertRequestValidation = insertRequestValidation;
		this.updateRequestValidation = updateRequestValidation;
	}

	public LinkedHashMap<String, Object> update(String applicationName, String className,
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

			/*
			 * check that the request body has update part
			 */
			if (htblRequestBody.containsKey("update") && htblRequestBody.get("update") instanceof LinkedHashMap<?, ?>) {
				LinkedHashMap<String, Object> htblUpdateRequestBody = (LinkedHashMap<String, Object>) htblRequestBody
						.get("update");

				/*
				 * check that the update part of the request body is valid
				 */
				UpdateRequestValidationResultUtility res = updateRequestValidation
						.validateUpdateRequest(applicationModelName, htblUpdateRequestBody, subjectClass);
				
			
			} else {

				throw new InvalidUpdateRequestBodyException("Invalid Update Request Body."
						+ " The Request body must contains an update field with an object"
						+ " value to hold the need updated fields and values. ");

			}

		} catch (ErrorObjException ex) {
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return ex.getExceptionHashTable(timeTaken);
		}

		return null;
	}

}
