package com.iotplatform.services;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.DynamicConceptsDao;
import com.iotplatform.daos.SelectQueryDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.InvalidClassNameException;
import com.iotplatform.exceptions.NoApplicationModelException;
import com.iotplatform.models.SuccessfullSelectAllJsonModel;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.dynamicConcepts.DynamicConceptsUtility;
import com.iotplatform.ontology.mapers.OntologyMapper;
import com.iotplatform.utilities.QueryField;
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
		if (OntologyMapper.getOntologyMapper().getHtblMainOntologyClassesMappers().containsKey(className)) {
			Class subjectClass = OntologyMapper.getOntologyMapper().getHtblMainOntologyClassesMappers().get(className);

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

	public static void main(String[] args) {
		Hashtable<String, Object> htblQueryFields = new Hashtable<>();
		ArrayList<Object> fieldsList = new ArrayList<>();
		htblQueryFields.put("fields", fieldsList);

		fieldsList.add("id");
		fieldsList.add("hasTransmissionPower");
		fieldsList.add("hasType");

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

		survivalPropertyFieldMap.put("fieldName", "hasSurvivalProperty");
		ArrayList<Object> survivalPropertyFieldsList = new ArrayList<>();
		survivalPropertyFieldMap.put("values",  new ArrayList<>());

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
		survivalPropertyFieldsList.add(systemLifetimeFieldMap);

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
				new DynamicConceptsUtility(new DynamicConceptsDao(dataSource)));
		
		ApplicationDao applicationDao = new ApplicationDao(oracle);
		
		SelectQueryDao selectQueryDao = new SelectQueryDao(oracle);
		
		SelectQueryService selectQueryService = new SelectQueryService(selectQueryRequestValidation, applicationDao, selectQueryDao);
		
		Hashtable<String, Object> res =  selectQueryService.QueryData("test application","communicating device",htblQueryFields);
			
		System.out.println(res);
		
//		Hashtable<String, Object>[] err = (Hashtable<String, Object>[])  res.get("errors");
//		System.out.println(err[0]);
		
		
		
	}
}
