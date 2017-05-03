package com.iotplatform.services;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.DynamicOntologyDao;
import com.iotplatform.daos.InsertionDao;
import com.iotplatform.daos.UpdateDao;
import com.iotplatform.daos.ValidationDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.InvalidClassNameException;
import com.iotplatform.exceptions.InvalidUpdateRequestBodyException;
import com.iotplatform.exceptions.NoApplicationModelException;
import com.iotplatform.models.SuccessfullInsertionModel;
import com.iotplatform.models.SuccessfullSelectAllJsonModel;
import com.iotplatform.models.SuccessfullUpdateJSONModel;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.mapers.DynamicOntologyMapper;
import com.iotplatform.ontology.mapers.OntologyMapper;
import com.iotplatform.utilities.InsertionPropertyValue;
import com.iotplatform.utilities.UpdateRequestValidationResultUtility;
import com.iotplatform.validations.InsertRequestValidation;
import com.iotplatform.validations.UpdateRequestValidation;

import oracle.spatial.rdf.client.jena.Oracle;

@Service("updatingService")
public class UpdateService {

	private DynamicOntologyDao dynamicOntologyDao;
	private ApplicationDao applicationDao;
	private InsertRequestValidation insertRequestValidation;
	private UpdateRequestValidation updateRequestValidation;
	private UpdateDao updateDao;
	private InsertionDao InsertionDao;

	@Autowired
	public UpdateService(DynamicOntologyDao dynamicOntologyDao, ApplicationDao applicationDao,
			InsertRequestValidation insertRequestValidation, UpdateRequestValidation updateRequestValidation,
			UpdateDao updateDao, InsertionDao insertionDao) {
		this.dynamicOntologyDao = dynamicOntologyDao;
		this.applicationDao = applicationDao;
		this.insertRequestValidation = insertRequestValidation;
		this.updateRequestValidation = updateRequestValidation;
		this.updateDao = updateDao;
		this.InsertionDao = insertionDao;
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
				 * make sure that the update part is not an empty object
				 */
				if (htblUpdateRequestBody.isEmpty()) {
					throw new InvalidUpdateRequestBodyException(
							"Invalid Update request body." + " The object Value of update field must not be empty.");
				}

				/*
				 * check that the update part of the request body is valid
				 */
				UpdateRequestValidationResultUtility updateRequestValidationResult = updateRequestValidation
						.validateUpdateRequest(applicationModelName, htblUpdateRequestBody, subjectClass);

				System.out.println(updateRequestValidationResult);

				/*
				 * check if the request body has insert part with update part
				 */
				if (htblRequestBody.containsKey("insert")
						&& htblRequestBody.get("insert") instanceof LinkedHashMap<?, ?>) {

					LinkedHashMap<String, Object> htblInsertRequestBody = (LinkedHashMap<String, Object>) htblRequestBody
							.get("insert");

					/*
					 * make sure that the insert part is not an empty object
					 */
					if (htblInsertRequestBody.isEmpty()) {
						throw new InvalidUpdateRequestBodyException("Invalid Update request body."
								+ " The object Value of insert field must not be empty.");
					}

					/*
					 * check that the insert part is valid
					 */
					Hashtable<String, ArrayList<ArrayList<InsertionPropertyValue>>> insertValidationRes = insertRequestValidation
							.validateRequestFields(htblInsertRequestBody, subjectClass, applicationModelName,
									updateRequestValidationResult.getHtblUniquePropValueList(),
									updateRequestValidationResult.getClassValueList());

					System.out.println(insertValidationRes);

					updateDao.updateData(applicationModelName, subjectClass, individualUniqueIdentifier,
							updateRequestValidationResult, insertValidationRes);

					double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
					SuccessfullUpdateJSONModel successModel = new SuccessfullUpdateJSONModel(subjectClass.getName(),
							timeTaken);
					return successModel.getResponseJson();

				} else {

					/*
					 * check that the insert part is valid
					 */
					insertRequestValidation.validateRequestFields(new LinkedHashMap<>(), subjectClass,
							applicationModelName, updateRequestValidationResult.getHtblUniquePropValueList(),
							updateRequestValidationResult.getClassValueList());

					updateDao.updateData(applicationModelName, subjectClass, individualUniqueIdentifier,
							updateRequestValidationResult, null);

					double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
					SuccessfullUpdateJSONModel successModel = new SuccessfullUpdateJSONModel(subjectClass.getName(),
							timeTaken);
					return successModel.getResponseJson();
				}

			} else {

				/*
				 * check if the request body has insert part only without update
				 * part
				 */
				if (htblRequestBody.containsKey("insert")
						&& htblRequestBody.get("insert") instanceof LinkedHashMap<?, ?>) {

					LinkedHashMap<String, Object> htblInsertRequestBody = (LinkedHashMap<String, Object>) htblRequestBody
							.get("insert");

					/*
					 * make sure that the insert part is not an empty object
					 */
					if (htblInsertRequestBody.isEmpty()) {
						throw new InvalidUpdateRequestBodyException("Invalid Update request body."
								+ " The object Value of insert field must not be empty.");
					}

					/*
					 * check that the insert part is valid
					 */
					Hashtable<String, ArrayList<ArrayList<InsertionPropertyValue>>> insertValidationRes = insertRequestValidation
							.validateRequestFields(htblInsertRequestBody, subjectClass, applicationModelName,
									new LinkedHashMap<>(), new ArrayList<>());

					System.out.println(insertValidationRes);

					updateDao.updateData(applicationModelName, subjectClass, individualUniqueIdentifier, null,
							insertValidationRes);

					double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
					SuccessfullInsertionModel successModel = new SuccessfullInsertionModel(subjectClass.getName(),
							timeTaken);
					return successModel.getResponseJson();

				} else {
					throw new InvalidUpdateRequestBodyException("Invalid Update Request Body."
							+ " The Request body must contains an update or insert fields with an object"
							+ " values to hold the need updated or new fields and values."
							+ " ex: {\"update\":{}, \"insert\":{} ");
				}
			}

		} catch (ErrorObjException ex) {
			System.out.println(ex.getExceptionMessage());
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return ex.getExceptionHashTable(timeTaken);
		}

	}

	public static void main(String[] args) {

		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		String szUser = "rdfusr";
		String szPasswd = "rdfusr";

		Oracle oracle = new Oracle(szJdbcURL, szUser, szPasswd);

		System.out.println("Database connected");

		DynamicOntologyDao dynamicOntologyDao = new DynamicOntologyDao(oracle);

		UpdateRequestValidation updateRequestValidation = new UpdateRequestValidation(dynamicOntologyDao);

		LinkedHashMap<String, Object> htblRequestBody = new LinkedHashMap<>();

		LinkedHashMap<String, Object> htbUpdatePart = new LinkedHashMap<>();
		// htblRequestBody.put("update", htbUpdatePart);

		htbUpdatePart.put("age", 28);

		htbUpdatePart.put("userName", "KimoElzeeny");

		LinkedHashMap<String, Object> htbMbox = new LinkedHashMap<>();
		htbMbox.put("newValue", "karim.mohamed@gmail.com");
		// htbMbox.put("newValue", "hatem.el-sayed@student.guc.edu.eg");
		htbMbox.put("oldValue", "kimoElzoz@gmail.com");

		htbUpdatePart.put("mbox", htbMbox);

		LinkedHashMap<String, Object> htblloves = new LinkedHashMap<>();
		htblloves.put("newValue", "OmarTag");
		htblloves.put("oldValue", "HatemElsayed");

		htbUpdatePart.put("loves", htblloves);
		htbUpdatePart.put("title", "MR");

		LinkedHashMap<String, Object> htbInsertPart = new LinkedHashMap<>();
		htblRequestBody.put("insert", htbInsertPart);

		htbInsertPart.put("knows", "KhaledElzeeny");
		// htbInsertPart.put("userName", "HatemMorgan");
		// htbInsertPart.put("topic_interest", "Sales");
		// htbInsertPart.put("gender", "Male");
		// htbInsertPart.put("job", "Sales Man");
		
		 LinkedHashMap<String, Object> htblKnows = new LinkedHashMap<>();
		 htblKnows.put("type", "Developer");
		 htblKnows.put("userName", "EL3ankboots");
		 htblKnows.put("firstName", "Gamal");
		 htblKnows.put("middleName", "Mostafa");
		 htblKnows.put("age", 22);
//		 htblKnows.put("hates", "HatemElsayed");
		 htblKnows.put("job", "Accounter");
		
		 htbInsertPart.put("knows", htblKnows);

		System.out.println(htblRequestBody);

		ApplicationDao applicationDao = new ApplicationDao(oracle);

		InsertRequestValidation insertRequestValidation = new InsertRequestValidation(new ValidationDao(oracle),
				dynamicOntologyDao);

		UpdateDao updateDao = new UpdateDao(oracle);

		InsertionDao insertionDao = new InsertionDao(oracle);

		UpdateService updateService = new UpdateService(dynamicOntologyDao, applicationDao, insertRequestValidation,
				updateRequestValidation, updateDao, insertionDao);

		updateService.update("test application", "developer", "KarimElzeeny", htblRequestBody);

	}

}
