package com.iotplatform.services;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.DynamicOntologyDao;
import com.iotplatform.daos.SelectQueryDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.InvalidClassNameException;
import com.iotplatform.exceptions.NoApplicationModelException;
import com.iotplatform.models.SuccessfullSelectAllJsonModel;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.mapers.DynamicOntologyMapper;
import com.iotplatform.ontology.mapers.OntologyMapper;
import com.iotplatform.utilities.QueryFieldUtility;
import com.iotplatform.utilities.QueryRequestValidationResultUtility;
import com.iotplatform.validations.SelectQueryRequestValidation;

import oracle.spatial.rdf.client.jena.Oracle;

/*
 * SelectQueryService is used to serve SelectQueryAPIController to insert new data 
 * 
 * 1- It calls SelectQueryRequestValidation class to validate the request and parse the request body
 * 2- It calls the SelectQueryDao which is responsible to query database and return results
 * 3- It takes the results and passed it to SelectQueryAPIController
 */

@Service("selectQueryService")
public class SelectQueryService {

	private SelectQueryRequestValidation selectQueryRequestValidation;
	private ApplicationDao applicationDao;
	private SelectQueryDao selectQueryDao;
	private DynamicOntologyDao dynamicOntologyDao;

	@Autowired
	public SelectQueryService(SelectQueryRequestValidation selectQueryRequestValidation, ApplicationDao applicationDao,
			SelectQueryDao selectQueryDao, DynamicOntologyDao dynamicOntologyDao) {
		this.selectQueryRequestValidation = selectQueryRequestValidation;
		this.applicationDao = applicationDao;
		this.selectQueryDao = selectQueryDao;
		this.dynamicOntologyDao = dynamicOntologyDao;
	}

	public LinkedHashMap<String, Object> QueryData(String applicationNameCode, String className,
			LinkedHashMap<String, Object> htblFieldValue) {

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
					 * The class is not from MainOntology so check it in
					 * DynamicOntology cache of this application
					 */

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
			 * validate query request
			 */
			QueryRequestValidationResultUtility validationResult = selectQueryRequestValidation
					.validateRequest(applicationNameCode, htblFieldValue, subjectClass, applicationModelName);
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<QueryFieldUtility>>> htblClassNameProperty = validationResult
					.getHtblClassNameProperty();

			List<LinkedHashMap<String, Object>> resultsList = selectQueryDao.queryData(htblClassNameProperty,
					applicationModelName, validationResult.getHtblOptions());

			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return new SuccessfullSelectAllJsonModel(resultsList, timeTaken).getJson();

		} catch (ErrorObjException e) {
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return new SuccessfullSelectAllJsonModel(e.getExceptionHashTable(timeTaken)).getJson();

		}

	}

	public static void main(String[] args) {
		LinkedHashMap<String, Object> htblQueryFields = new LinkedHashMap<>();

		LinkedHashMap<String, Object> htblOptions = new LinkedHashMap<>();
		htblOptions.put("autoGetObjValType", true);
		htblQueryFields.put("options", htblOptions);

		ArrayList<Object> fieldsList = new ArrayList<>();
		htblQueryFields.put("fields", fieldsList);

		fieldsList.add("id");
		fieldsList.add("hasTransmissionPower");
		fieldsList.add("hasType");
		fieldsList.add("exposedBy");

		LinkedHashMap<String, Object> coverageFieldMap = new LinkedHashMap<>();
		fieldsList.add(coverageFieldMap);

		coverageFieldMap.put("fieldName", "hasCoverage");
		ArrayList<Object> coverageFieldsList = new ArrayList<>();
		coverageFieldMap.put("fields", coverageFieldsList);

		coverageFieldsList.add("id");

		LinkedHashMap<String, Object> locationFieldMap = new LinkedHashMap<>();
		coverageFieldsList.add(locationFieldMap);

		locationFieldMap.put("fieldName", "location");
		ArrayList<Object> locationFieldsList = new ArrayList<>();
		locationFieldMap.put("fields", locationFieldsList);

		locationFieldsList.add("id");
		locationFieldsList.add("long");
		locationFieldsList.add("lat");

		LinkedHashMap<String, Object> metaDataFieldMap = new LinkedHashMap<>();
		fieldsList.add(metaDataFieldMap);

		metaDataFieldMap.put("fieldName", "hasMetadata");
		ArrayList<Object> metadataFieldsList = new ArrayList<>();
		metaDataFieldMap.put("fields", metadataFieldsList);

		metadataFieldsList.add("id");
		metadataFieldsList.add("metadataType");
		metadataFieldsList.add("metadataValue");

		LinkedHashMap<String, Object> survivalRangeFieldMap = new LinkedHashMap<>();
		fieldsList.add(survivalRangeFieldMap);

		survivalRangeFieldMap.put("fieldName", "hasSurvivalRange");
		ArrayList<Object> survivalRangeFieldsList = new ArrayList<>();
		survivalRangeFieldMap.put("fields", survivalRangeFieldsList);

		LinkedHashMap<String, Object> conditionFieldMap = new LinkedHashMap<>();
		survivalRangeFieldsList.add(conditionFieldMap);

		conditionFieldMap.put("fieldName", "inCondition");
		ArrayList<Object> conditionFieldsList = new ArrayList<>();
		conditionFieldMap.put("fields", conditionFieldsList);

		conditionFieldsList.add("id");
		conditionFieldsList.add("description");

		LinkedHashMap<String, Object> survivalPropertyFieldMap = new LinkedHashMap<>();
		survivalRangeFieldsList.add(survivalPropertyFieldMap);
		// survivalRangeFieldsList.add("hasSurvivalProperty");

		survivalPropertyFieldMap.put("fieldName", "hasSurvivalProperty");
		ArrayList<Object> survivalPropertyFieldsList = new ArrayList<>();
		survivalPropertyFieldMap.put("values", survivalPropertyFieldsList);

		LinkedHashMap<String, Object> batteryLifetimeFieldMap = new LinkedHashMap<>();
		survivalPropertyFieldsList.add(batteryLifetimeFieldMap);

		batteryLifetimeFieldMap.put("classType", "BatteryLifetime");
		ArrayList<Object> batteryLifetimeFieldsList = new ArrayList<>();
		batteryLifetimeFieldMap.put("fields", batteryLifetimeFieldsList);

		batteryLifetimeFieldsList.add("id");

		LinkedHashMap<String, Object> amountFieldMap = new LinkedHashMap<>();
		batteryLifetimeFieldsList.add(amountFieldMap);

		amountFieldMap.put("fieldName", "hasValue");
		ArrayList<Object> amountFieldsList = new ArrayList<>();
		amountFieldMap.put("fields", amountFieldsList);

		amountFieldsList.add("hasDataValue");

		LinkedHashMap<String, Object> systemLifetimeFieldMap = new LinkedHashMap<>();
		// survivalPropertyFieldsList.add(systemLifetimeFieldMap);

		systemLifetimeFieldMap.put("classType", "SystemLifetime");
		systemLifetimeFieldMap.put("fields", batteryLifetimeFieldsList);

		System.out.println(htblQueryFields);

		System.out.println(OntologyMapper.getOntologyMapper().getHtblMainOntologyClassesUriMappers()
				.get("http://purl.oclc.org/NET/ssnx/ssn#SurvivalProperty").getClassTypesList());

		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		String szUser = "rdfusr";
		String szPasswd = "rdfusr";
		String szJdbcDriver = "oracle.jdbc.driver.OracleDriver";

		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(szJdbcDriver);
		dataSource.setUrl(szJdbcURL);
		dataSource.setUsername(szUser);
		dataSource.setPassword(szPasswd);

		Oracle oracle = new Oracle(szJdbcURL, szUser, szPasswd);

		SelectQueryRequestValidation selectQueryRequestValidation = new SelectQueryRequestValidation(
				(new DynamicOntologyDao(oracle)));

		ApplicationDao applicationDao = new ApplicationDao(oracle);

		SelectQueryDao selectQueryDao = new SelectQueryDao(oracle);

		DynamicOntologyDao dynamicOntologyDao = new DynamicOntologyDao(oracle);

		SelectQueryService selectQueryService = new SelectQueryService(selectQueryRequestValidation, applicationDao,
				selectQueryDao, dynamicOntologyDao);

		LinkedHashMap<String, Object> res = selectQueryService.QueryData("test application", "communicating device",
				htblQueryFields);

		System.out.println(res);

		// Hashtable<String, Object>[] err = (Hashtable<String, Object>[])
		// res.get("errors");
		// System.out.println(err[0]);

	}
}
