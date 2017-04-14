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
import com.iotplatform.daos.MainDao;
import com.iotplatform.daos.ValidationDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.InvalidClassNameException;
import com.iotplatform.exceptions.NoApplicationModelException;
import com.iotplatform.models.SuccessfullInsertionModel;
import com.iotplatform.models.SuccessfullSelectAllJsonModel;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.dynamicConcepts.DynamicConceptsUtility;
import com.iotplatform.ontology.mapers.OntologyMapper;
import com.iotplatform.query.results.SelectionQueryResults;
import com.iotplatform.utilities.PropertyValue;
import com.iotplatform.utilities.QueryField;
import com.iotplatform.validations.QueryRequestValidations;
import com.iotplatform.validations.InsertRequestValidations;

import oracle.spatial.rdf.client.jena.Oracle;

/*
 * DynamicInsertionService is to service the insertion API in DynamicAPIController
 * 
 */

@Service("dynamicInsertionService")
public class DynamicInsertionService {

	private InsertRequestValidations requestFieldsValidation;
	private ApplicationDao applicationDao;
	private MainDao mainDao;
	// private Hashtable<String, Class> htblAllStaticClasses;
	private QueryRequestValidations getQueryRequestValidations;

	@Autowired
	public DynamicInsertionService(InsertRequestValidations requestFieldsValidation, ApplicationDao applicationDao,
			MainDao mainDao, QueryRequestValidations getQueryRequestValidations) {
		this.requestFieldsValidation = requestFieldsValidation;
		this.applicationDao = applicationDao;
		this.mainDao = mainDao;
		this.getQueryRequestValidations = getQueryRequestValidations;

		// init();
	}

	/*
	 * insertNewFieldValueList method is a service method that is responsible to
	 * take property values key pairs and call request validation to validate
	 * the request content then if it pass the validations call the admin data
	 * access object to insert the new admin
	 */
	public Hashtable<String, Object> insertNewFieldValueList(Hashtable<String, Object> htblFieldValue,
			String applicationNameCode, String className) {

		long startTime = System.currentTimeMillis();
		className = className.toLowerCase().replaceAll(" ", "");

		/*
		 * check if the className has a valid class Mapping
		 */
		if (OntologyMapper.getHtblMainOntologyClassesMappers().containsKey(className)) {

			try {

				Class subjectClass = OntologyMapper.getHtblMainOntologyClassesMappers().get(className);

				/*
				 * Check if the request is valid or not
				 */
				Hashtable<Class, ArrayList<ArrayList<PropertyValue>>> htblClassPropertyValue = requestFieldsValidation
						.validateRequestFields(applicationNameCode, htblFieldValue, subjectClass);

				/*
				 * get application modelName
				 */
				String applicationModelName = applicationDao.getHtblApplicationNameModelName().get(applicationNameCode);

				mainDao.insertData(applicationModelName, subjectClass.getName(), htblClassPropertyValue);

				double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
				SuccessfullInsertionModel successModel = new SuccessfullInsertionModel(subjectClass.getName(),
						timeTaken);
				return successModel.getResponseJson();

			} catch (ErrorObjException ex) {
				double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
				return ex.getExceptionHashTable(timeTaken);

			}

		} else

		{
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			InvalidClassNameException invalidClassNameException = new InvalidClassNameException(className);
			return invalidClassNameException.getExceptionHashTable(timeTaken);
		}

	}

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

				List<Hashtable<String, Object>> htblPropValue = mainDao.selectAll(applicationModelName, subjectClass);

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
				LinkedHashMap<String, LinkedHashMap<String, ArrayList<QueryField>>> htblClassNameProperty = getQueryRequestValidations
						.validateRequest(applicationNameCode, htblFieldValue, subjectClass);

				/*
				 * get application modelName
				 */
				String applicationModelName = applicationDao.getHtblApplicationNameModelName().get(applicationNameCode);

				List<Hashtable<String, Object>> resultsList = mainDao.queryData(htblClassNameProperty,
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

	// private void init() {
	// htblAllStaticClasses = new Hashtable<>();
	// htblAllStaticClasses.put("application",
	// Application.getApplicationInstance());
	// htblAllStaticClasses.put("person", Person.getPersonInstance());
	// htblAllStaticClasses.put("admin", Admin.getAdminInstance());
	// htblAllStaticClasses.put("developer", Developer.getDeveloperInstance());
	// htblAllStaticClasses.put("normalUser",
	// NormalUser.getNormalUserInstance());
	// htblAllStaticClasses.put("actuatingdevice",
	// ActuatingDevice.getActuatingDeviceInstance());
	// htblAllStaticClasses.put("agent", Agent.getAgentInstance());
	// htblAllStaticClasses.put("amount", Amount.getAmountInstance());
	// htblAllStaticClasses.put("attribute", Attribute.getAttributeInstance());
	// htblAllStaticClasses.put("communicatingdevice",
	// CommunicatingDevice.getCommunicatingDeviceInstance());
	// htblAllStaticClasses.put("condition", Condition.getConditionInstance());
	// htblAllStaticClasses.put("coverage", Coverage.getCoverageInstance());
	// htblAllStaticClasses.put("deployment",
	// Deployment.getDeploymentInstance());
	// htblAllStaticClasses.put("deploymentrelatedprocess",
	// DeploymentRelatedProcess.getDeploymentRelatedProcessInstance());
	// htblAllStaticClasses.put("device", Device.getDeviceInstance());
	// htblAllStaticClasses.put("devicemodule",
	// DeviceModule.getDeviceModuleInstance());
	// htblAllStaticClasses.put("featureofinterest",
	// FeatureOfInterest.getFeatureOfInterestInstance());
	// htblAllStaticClasses.put("group", Group.getGroupInstance());
	// htblAllStaticClasses.put("input", Input.getInputInstance());
	// htblAllStaticClasses.put("iotsystem", IOTSystem.getIOTSystemInstance());
	// htblAllStaticClasses.put("measurementcapability",
	// MeasurementCapability.getMeasurementCapabilityInstance());
	// htblAllStaticClasses.put("measurementproperty",
	// MeasurementProperty.getMeasurementPropertyInstance());
	// htblAllStaticClasses.put("metadata", Metadata.getMetadataInstance());
	// htblAllStaticClasses.put("object", ObjectClass.getObjectClassInstance());
	// htblAllStaticClasses.put("observation",
	// Observation.getObservationInstance());
	// htblAllStaticClasses.put("observationvalue",
	// ObservationValue.getObservationValueInstance());
	// htblAllStaticClasses.put("operatingproperty",
	// OperatingProperty.getOperatingPropertyInstance());
	// htblAllStaticClasses.put("operatingrange",
	// OperatingRange.getOperatingRangeInstance());
	// htblAllStaticClasses.put("organization",
	// Organization.getOrganizationInstance());
	// htblAllStaticClasses.put("output", Output.getOutputInstance());
	// htblAllStaticClasses.put("platform", Platform.getPlatformInstance());
	// htblAllStaticClasses.put("point", Point.getPointInstacne());
	// htblAllStaticClasses.put("process", Process.getProcessInstance());
	// htblAllStaticClasses.put("property",
	// com.iotplatform.ontology.classes.Property.getPropertyInstance());
	// htblAllStaticClasses.put("quantitykind",
	// QuantityKind.getQuantityKindInstance());
	// htblAllStaticClasses.put("sensing", Sensing.getSensingInstance());
	// htblAllStaticClasses.put("sensingDevice",
	// SensingDevice.getSensingDeviceInstance());
	// htblAllStaticClasses.put("sensor", Sensor.getSensorInstance());
	// htblAllStaticClasses.put("sensordatasheet",
	// SensorDataSheet.getSensorDataSheetInstance());
	// htblAllStaticClasses.put("sensoroutput",
	// SensorOutput.getSensorOutputInstance());
	// htblAllStaticClasses.put("service",
	// com.iotplatform.ontology.classes.Service.getServiceInstance());
	// htblAllStaticClasses.put("stimulus", Stimulus.getStimulusInstance());
	// htblAllStaticClasses.put("survivalproperty",
	// SurvivalProperty.getSurvivalPropertyInstance());
	// htblAllStaticClasses.put("survivalrange",
	// SurvivalRange.getSurvivalRangeInstance());
	// htblAllStaticClasses.put("system", SystemClass.getSystemInstance());
	// htblAllStaticClasses.put("tagdevice", TagDevice.getTagDeviceInstance());
	// htblAllStaticClasses.put("unit", Unit.getUnitInstance());
	// }

	public static void main(String[] args) {
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

		DynamicConceptsDao dynamicConceptDao = new DynamicConceptsDao(dataSource);

		ValidationDao validationDao = new ValidationDao(oracle);

		InsertRequestValidations requestFieldsValidation = new InsertRequestValidations(validationDao,
				new DynamicConceptsUtility(dynamicConceptDao));

		MainDao mainDao = new MainDao(oracle, new SelectionQueryResults(new DynamicConceptsUtility(dynamicConceptDao)));

		QueryRequestValidations getQueryRequestValidations = new QueryRequestValidations(
				new DynamicConceptsUtility(dynamicConceptDao));

		DynamicInsertionService dynamicInsertionService = new DynamicInsertionService(requestFieldsValidation,
				new ApplicationDao(oracle), mainDao, getQueryRequestValidations);

		Hashtable<String, Object> htblFieldValue = new Hashtable<>();

		ArrayList<Object> fieldList = new ArrayList<>();
		htblFieldValue.put("fields", fieldList);

		fieldList.add("id");
		fieldList.add("hasTransmissionPower");
		fieldList.add("hasType");

		LinkedHashMap<String, Object> coverageMap = new LinkedHashMap<>();
		coverageMap.put("fieldName", "hasCoverage");
		ArrayList<Object> coverageFieldList = new ArrayList<>();
		coverageMap.put("fields", coverageFieldList);
		coverageFieldList.add("id");

		LinkedHashMap<String, Object> pointMap = new LinkedHashMap<>();
		pointMap.put("fieldName", "location");
		ArrayList<Object> pointFieldList = new ArrayList<>();
		pointMap.put("fields", pointFieldList);
		pointFieldList.add("id");
		pointFieldList.add("lat");
		pointFieldList.add("long");

		coverageFieldList.add(pointMap);

		fieldList.add(coverageMap);

		LinkedHashMap<String, Object> survivalRangeMap = new LinkedHashMap<>();
		survivalRangeMap.put("fieldName", "hasSurvivalRange");
		ArrayList<Object> survivalRangeFieldList = new ArrayList<>();
		survivalRangeMap.put("fields", survivalRangeFieldList);

		LinkedHashMap<String, Object> conditionMap = new LinkedHashMap<>();
		conditionMap.put("fieldName", "inCondition");
		ArrayList<Object> conditionFieldList = new ArrayList<>();
		conditionMap.put("fields", conditionFieldList);
		conditionFieldList.add("id");
		conditionFieldList.add("description");

		survivalRangeFieldList.add(conditionMap);

		LinkedHashMap<String, Object> survivalPropertyMap = new LinkedHashMap<>();
		survivalPropertyMap.put("fieldName", "hasSurvivalProperty");
		survivalPropertyMap.put("classType", "BatteryLifetime");
		ArrayList<Object> survivalPropertyList = new ArrayList<>();
		survivalPropertyMap.put("fields", survivalPropertyList);
		survivalPropertyList.add("id");

		LinkedHashMap<String, Object> amountMap = new LinkedHashMap<>();
		amountMap.put("fieldName", "hasValue");
		ArrayList<Object> amountFieldList = new ArrayList<>();
		amountMap.put("fields", amountFieldList);
		amountFieldList.add("id");
		amountFieldList.add("hasDataValue");

		survivalPropertyList.add(amountMap);

		survivalRangeFieldList.add(survivalPropertyMap);

		fieldList.add(survivalRangeMap);

		System.out.println(htblFieldValue);

		System.out
				.println(dynamicInsertionService.QueryData("TESTAPPLICATION", "communicating Device", htblFieldValue));

		// System.out.println(dynamicInsertionService.selectAll("TESTAPPLICATION",
		// "communicating Device"));
	}
}
