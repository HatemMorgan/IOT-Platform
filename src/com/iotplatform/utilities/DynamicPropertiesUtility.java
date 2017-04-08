package com.iotplatform.utilities;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.daos.DynamicConceptDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.models.DynamicConceptModel;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.DynamicConceptColumns;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.PropertyType;
import com.iotplatform.ontology.XSDDataTypes;
import com.iotplatform.ontology.classes.ActuatingDevice;
import com.iotplatform.ontology.classes.Admin;
import com.iotplatform.ontology.classes.Agent;
import com.iotplatform.ontology.classes.Amount;
import com.iotplatform.ontology.classes.Application;
import com.iotplatform.ontology.classes.Attribute;
import com.iotplatform.ontology.classes.CommunicatingDevice;
import com.iotplatform.ontology.classes.Condition;
import com.iotplatform.ontology.classes.Coverage;
import com.iotplatform.ontology.classes.Deployment;
import com.iotplatform.ontology.classes.DeploymentRelatedProcess;
import com.iotplatform.ontology.classes.Developer;
import com.iotplatform.ontology.classes.Device;
import com.iotplatform.ontology.classes.DeviceModule;
import com.iotplatform.ontology.classes.FeatureOfInterest;
import com.iotplatform.ontology.classes.Group;
import com.iotplatform.ontology.classes.IOTSystem;
import com.iotplatform.ontology.classes.Input;
import com.iotplatform.ontology.classes.MeasurementCapability;
import com.iotplatform.ontology.classes.MeasurementProperty;
import com.iotplatform.ontology.classes.Metadata;
import com.iotplatform.ontology.classes.NormalUser;
import com.iotplatform.ontology.classes.ObjectClass;
import com.iotplatform.ontology.classes.Observation;
import com.iotplatform.ontology.classes.ObservationValue;
import com.iotplatform.ontology.classes.OperatingProperty;
import com.iotplatform.ontology.classes.OperatingRange;
import com.iotplatform.ontology.classes.Organization;
import com.iotplatform.ontology.classes.Output;
import com.iotplatform.ontology.classes.Person;
import com.iotplatform.ontology.classes.Platform;
import com.iotplatform.ontology.classes.Point;
import com.iotplatform.ontology.classes.Process;
import com.iotplatform.ontology.classes.QuantityKind;
import com.iotplatform.ontology.classes.Sensing;
import com.iotplatform.ontology.classes.SensingDevice;
import com.iotplatform.ontology.classes.Sensor;
import com.iotplatform.ontology.classes.SensorDataSheet;
import com.iotplatform.ontology.classes.SensorOutput;
import com.iotplatform.ontology.classes.Service;
import com.iotplatform.ontology.classes.Stimulus;
import com.iotplatform.ontology.classes.SurvivalProperty;
import com.iotplatform.ontology.classes.SurvivalRange;
import com.iotplatform.ontology.classes.SystemClass;
import com.iotplatform.ontology.classes.TagDevice;
import com.iotplatform.ontology.classes.Unit;

/*
 * DynamicPropertiesUtility is used to load dynamic properties and perform caching of loaded dynamic properties
 */

@Component
public class DynamicPropertiesUtility {

	public static Hashtable<String, Class> htblAllStaticClasses;

	/*
	 * dynamicConceptDao class is used to get all dynamic properties or dynamic
	 * classes added to the ontology
	 */
	private DynamicConceptDao dynamicConceptDao;

	@Autowired
	public DynamicPropertiesUtility(DynamicConceptDao dynamicConceptDao) {
		this.dynamicConceptDao = dynamicConceptDao;

		init();
	}

	/*
	 * it loads dynamic properties of the given classsList in the passed
	 * application domain and also caches the dynamic properties to improve
	 * performance
	 * 
	 * htblPrevNotMappedFieldsClasses is a hashtable that holds the previous
	 * classes that their dynamicProperties where loaded before in the
	 * validation of this request (this improve performance by preventing
	 * loading the dynamic properties of the same class more than time when
	 * validating a request that has multiple of nested objects in its body)
	 * 
	 * This way of comparison between htblPrevNotMappedFieldsClasses and
	 * htblNotMappedFieldsClasses can improve performance by removing some
	 * conditions or it can avoid penalty of going to database if the both
	 * hashtables holds the same classes
	 */
	public Hashtable<String, DynamicConceptModel> getDynamicProperties(String applicationName,
			Hashtable<String, Class> htblNotMappedFieldsClasses,
			Hashtable<String, Class> htblPrevNotMappedFieldsClasses) {

		/*
		 * to get the dynamic properties only one time
		 */
		ArrayList<SqlCondition> orCondtionsFilterList = new ArrayList<>();

		Iterator<String> htblNotMappedFieldsClassesIterator = htblNotMappedFieldsClasses.keySet().iterator();

		/*
		 * htblClassNameCheckList maintains that no duplicate classNames enters
		 * in the conditionList. duplicates may happen when getting superClasses
		 * 
		 * like if Developer and Person are the two Classes in
		 * htblNotMappedFieldsClasses both have Agent as superClass so Agent
		 * will be duplicated
		 */
		Hashtable<String, String> htblClassNameCheckList = new Hashtable<>();

		/*
		 * tmp is used to store any superClass that will be added to the query
		 * condition inOrder to avoid loading its properties again in this
		 * validation(this may happen if there are nestedObject value for
		 * dynamicLoaded property so this nested object will be parsed after
		 * loadingDynamicProperties. it might happen that this
		 * nestedObjectProperty has notMappedField and this nested object is of
		 * type on of the superClasses so avoid loading dynamicProperties again
		 * for this class)
		 */
		Hashtable<String, Class> tmp = new Hashtable<>();

		/*
		 * creating conditionsList
		 */
		while (htblNotMappedFieldsClassesIterator.hasNext()) {

			String classUri = htblNotMappedFieldsClassesIterator.next();

			/*
			 * if the class with classUri has its dynamic properties loaded
			 * before in this request validation so skip it
			 */
			if (htblPrevNotMappedFieldsClasses.containsKey(classUri)) {
				continue;
			}

			Class subjectClass = htblNotMappedFieldsClasses.get(classUri);

			if (!htblClassNameCheckList.containsKey(classUri)) {
				orCondtionsFilterList
						.add(new SqlCondition(DynamicConceptColumns.CLASS_URI.toString(), subjectClass.getUri()));
				htblClassNameCheckList.put(classUri, "");
			}

			for (Class superClass : subjectClass.getSuperClassesList()) {
				if (!htblClassNameCheckList.containsKey(superClass.getUri())) {
					orCondtionsFilterList
							.add(new SqlCondition(DynamicConceptColumns.CLASS_URI.toString(), superClass.getUri()));

					htblClassNameCheckList.put(superClass.getUri(), "");
					tmp.put(superClass.getUri(), superClass);
				}
			}
		}

		htblNotMappedFieldsClasses.putAll(tmp);
		List<DynamicConceptModel> res;
		try {
			/*
			 * make sure that there are conditions
			 */
			if (orCondtionsFilterList.size() > 0) {
				System.out.println("loading dynamic properties");
				res = dynamicConceptDao.getConceptsOfApplicationByFilters(applicationName, null, orCondtionsFilterList);
			} else {
				return new Hashtable<>();
			}
		} catch (ErrorObjException ex) {
			throw ex;
		}

		/*
		 * populate dynamicProperties hashtable to enhance performance when
		 * having alot of dynamic properties not cached so to avoid going to the
		 * database more than one time. I load all the dynamic properties of the
		 * specified subject and then cache results
		 * 
		 * I used hashtable to hold dynamic properties to enhance performance
		 * when checking for valid property in the loading dynamic properties. I
		 * did not check in the loop to allow caching all new properties that
		 * were not cached before without terminating the loop when finding the
		 * property needed in the dynamicProperties
		 * 
		 * Also add dynamic property to property list of the subject class in
		 * order to improve performance by caching the dynamic properties
		 */
		Hashtable<String, DynamicConceptModel> dynamicProperties = new Hashtable<>();

		applicationName = applicationName.replaceAll(" ", "").toUpperCase();

		for (DynamicConceptModel dynamicProperty : res) {

			Class subjectClass = htblAllStaticClasses.get(dynamicProperty.getClass_uri());

			cacheLoadedDynamicProperty(dynamicProperty, subjectClass, applicationName);

			dynamicProperties.put(dynamicProperty.getProperty_name(), dynamicProperty);
		}
		return dynamicProperties;
	}

	/*
	 * cacheLoadedDynamicProperty method is used to cache a loaded dynamic
	 * property by adding it to subclass's propertiesList and add it to any of
	 * the subjectClass's subClasses' propertiesList (Properties inheritance)
	 */
	private void cacheLoadedDynamicProperty(DynamicConceptModel dynamicProperty, Class subjectClass,
			String applicationName) {

		// check if the property was cached before
		if (!subjectClass.getProperties().contains(dynamicProperty.getProperty_name())) {

			subjectClass.getHtblPropUriName().put(dynamicProperty.getProperty_uri(),
					dynamicProperty.getProperty_name());

			if (dynamicProperty.getProperty_type().equals(PropertyType.DatatypeProperty.toString())) {
				htblAllStaticClasses.get(subjectClass.getUri()).getProperties().put(dynamicProperty.getProperty_name(),
						new DataTypeProperty(htblAllStaticClasses.get(dynamicProperty.getClass_uri()),
								dynamicProperty.getProperty_name(),
								getPrefix(dynamicProperty.getProperty_prefix_alias()),
								getXSDDataTypeEnum(dynamicProperty.getProperty_object_type_uri()), applicationName,
								dynamicProperty.getHasMultipleValues(), dynamicProperty.getIsUnique()));
			} else {
				if (dynamicProperty.getProperty_type().equals(PropertyType.ObjectProperty.toString())) {
					htblAllStaticClasses.get(subjectClass.getUri()).getProperties().put(
							dynamicProperty.getProperty_name(),
							new ObjectProperty(htblAllStaticClasses.get(dynamicProperty.getClass_uri()),
									dynamicProperty.getProperty_name(),
									getPrefix(dynamicProperty.getProperty_prefix_alias()),
									htblAllStaticClasses.get(dynamicProperty.getProperty_object_type_uri()),
									applicationName, dynamicProperty.getHasMultipleValues(),
									dynamicProperty.getIsUnique()));
				}
			}
		}
		/*
		 * Check if the subjectClass has subClasses(Type Classes)
		 */
		if (!subjectClass.isHasTypeClasses()) {
			return;
		}

		/*
		 * caching property into subClasses after made sure that it has
		 * typeClasses(SubClasses)
		 */
		Iterator<String> htblSubClassesIterator = subjectClass.getClassTypesList().keySet().iterator();

		while (htblSubClassesIterator.hasNext()) {
			String subClassName = htblSubClassesIterator.next();
			Class subClass = subjectClass.getClassTypesList().get(subClassName);

			// check if the property was cached before
			if (!subClass.getProperties().contains(dynamicProperty.getProperty_name())) {

				subClass.getHtblPropUriName().put(dynamicProperty.getProperty_uri(),
						dynamicProperty.getProperty_name());

				if (dynamicProperty.getProperty_type().equals(PropertyType.DatatypeProperty.toString())) {
					htblAllStaticClasses.get(subClass.getUri()).getProperties().put(dynamicProperty.getProperty_name(),
							new DataTypeProperty(htblAllStaticClasses.get(dynamicProperty.getClass_uri()),
									dynamicProperty.getProperty_name(),
									getPrefix(dynamicProperty.getProperty_prefix_alias()),
									getXSDDataTypeEnum(dynamicProperty.getProperty_object_type_uri()), applicationName,
									dynamicProperty.getHasMultipleValues(), dynamicProperty.getIsUnique()));
				} else {
					if (dynamicProperty.getProperty_type().equals(PropertyType.ObjectProperty.toString())) {
						htblAllStaticClasses.get(subClass.getUri()).getProperties().put(
								dynamicProperty.getProperty_name(),
								new ObjectProperty(htblAllStaticClasses.get(dynamicProperty.getClass_uri()),
										dynamicProperty.getProperty_name(),
										getPrefix(dynamicProperty.getProperty_prefix_alias()),
										htblAllStaticClasses.get(dynamicProperty.getProperty_object_type_uri()),
										applicationName, dynamicProperty.getHasMultipleValues(),
										dynamicProperty.getIsUnique()));
					}
				}
			}

		}

	}

	/*
	 * get Prefix enum that maps the String prefixAlias from a dynamicProperty
	 */
	private Prefix getPrefix(String prefixAlias) {

		if (Prefix.FOAF.getPrefix().equals(prefixAlias)) {
			return Prefix.FOAF;
		}

		if (Prefix.SSN.getPrefix().equals(prefixAlias)) {
			return Prefix.SSN;
		}

		if (Prefix.IOT_LITE.getPrefix().equals(prefixAlias)) {
			return Prefix.IOT_LITE;
		}

		if (Prefix.IOT_PLATFORM.getPrefix().equals(prefixAlias)) {
			return Prefix.IOT_PLATFORM;
		}

		if (Prefix.GEO.getPrefix().equals(prefixAlias)) {
			return Prefix.GEO;
		}

		if (Prefix.XSD.getPrefix().equals(prefixAlias)) {
			return Prefix.XSD;
		}

		if (Prefix.OWL.getPrefix().equals(prefixAlias)) {
			return Prefix.OWL;
		}

		if (Prefix.RDFS.getPrefix().equals(prefixAlias)) {
			return Prefix.RDFS;
		}

		if (Prefix.RDF.getPrefix().equals(prefixAlias)) {
			return Prefix.RDF;
		}

		if (Prefix.QU.getPrefix().equals(prefixAlias)) {
			return Prefix.QU;
		}

		if (Prefix.DUL.getPrefix().equals(prefixAlias)) {
			return Prefix.DUL;
		}

		return null;
	}

	/*
	 * getXSDDataTypeEnum return XsdDataType enum instance
	 */
	private XSDDataTypes getXSDDataTypeEnum(String dataType) {

		if (XSDDataTypes.boolean_type.getXsdTypeURI().equals(dataType)) {
			return XSDDataTypes.boolean_type;
		}

		if (XSDDataTypes.decimal_typed.getXsdTypeURI().equals(dataType)) {
			return XSDDataTypes.decimal_typed;
		}

		if (XSDDataTypes.float_typed.getXsdTypeURI().equals(dataType)) {
			return XSDDataTypes.float_typed;
		}

		if (XSDDataTypes.integer_typed.getXsdTypeURI().equals(dataType)) {
			return XSDDataTypes.integer_typed;
		}

		if (XSDDataTypes.string_typed.getXsdTypeURI().equals(dataType)) {
			return XSDDataTypes.string_typed;
		}

		if (XSDDataTypes.dateTime_typed.getXsdTypeURI().equals(dataType)) {
			return XSDDataTypes.dateTime_typed;
		}

		if (XSDDataTypes.double_typed.getXsdTypeURI().equals(dataType)) {
			return XSDDataTypes.double_typed;
		}

		return null;
	}

	/*
	 * it loads dynamic properties of the given class in the passed application
	 * domain and also caches the dynamic properties to improve performance
	 */
	public Hashtable<String, DynamicConceptModel> getDynamicProperties(String applicationName, Class subjectClass) {

		/*
		 * to get the dynamic properties only one time
		 */

		ArrayList<SqlCondition> orCondtionsFilterList = new ArrayList<>();
		orCondtionsFilterList.add(new SqlCondition(DynamicConceptColumns.CLASS_URI.toString(), subjectClass.getUri()));

		for (Class superClass : subjectClass.getSuperClassesList()) {
			orCondtionsFilterList
					.add(new SqlCondition(DynamicConceptColumns.CLASS_URI.toString(), superClass.getUri()));

		}

		List<DynamicConceptModel> res;
		try {
			res = dynamicConceptDao.getConceptsOfApplicationByFilters(applicationName, null, orCondtionsFilterList);
		} catch (ErrorObjException ex) {
			throw ex;
		}

		/*
		 * populate dynamicProperties hashtable to enhance performance when
		 * having alot of dynamic properties not cached so to avoid going to the
		 * database more than one time. I load all the dynamic properties of the
		 * specified subject and then cache results
		 *
		 * I used hashtable to hold dynamic properties to enhance performance
		 * when checking for valid property in the loading dynamic properties. I
		 * did not check in the loop to allow caching all new properties that
		 * were not cached before without terminating the loop when finding the
		 * property needed in the dynamicProperties
		 *
		 * Also add dynamic property to property list of the subject class in
		 * order to improve performance by caching the dynamic properties
		 */
		Hashtable<String, DynamicConceptModel> dynamicProperties = new Hashtable<>();

		applicationName = applicationName.replaceAll(" ", "").toUpperCase();

		for (DynamicConceptModel dynamicProperty : res) {

			Class propertySubjectClass = htblAllStaticClasses.get(dynamicProperty.getClass_uri());

			// skip if the property was cached before
			if (propertySubjectClass.getProperties().contains(dynamicProperty.getProperty_name())) {
				continue;
			}

			propertySubjectClass.getHtblPropUriName().put(dynamicProperty.getProperty_uri(),
					dynamicProperty.getProperty_name());

			cacheLoadedDynamicProperty(dynamicProperty, propertySubjectClass, applicationName);
			dynamicProperties.put(dynamicProperty.getProperty_name(), dynamicProperty);
		}

		// System.out.println(htblAllStaticClasses.get("http://xmlns.com/foaf/0.1/Person").getHtblPropUriName());
		// System.out.println(htblAllStaticClasses.get("http://iot-platform#Developer").getHtblPropUriName());

		return dynamicProperties;
	}

	public Hashtable<String, Class> getHtblAllStaticClasses() {
		if (htblAllStaticClasses == null)
			init();

		return htblAllStaticClasses;
	}

	private void init() {
		htblAllStaticClasses = new Hashtable<>();
		htblAllStaticClasses.put("http://iot-platform#Application", Application.getApplicationInstance());
		htblAllStaticClasses.put("http://xmlns.com/foaf/0.1/Person", Person.getPersonInstance());
		htblAllStaticClasses.put("http://iot-platform#Admin", Admin.getAdminInstance());
		htblAllStaticClasses.put("http://iot-platform#Developer", Developer.getDeveloperInstance());
		htblAllStaticClasses.put("http://iot-platform#NormalUser", NormalUser.getNormalUserInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/UNIS/fiware/iot-lite#ActuatingDevice",
				ActuatingDevice.getActuatingDeviceInstance());
		htblAllStaticClasses.put("http://xmlns.com/foaf/0.1/Agent", Agent.getAgentInstance());
		htblAllStaticClasses.put("http://iot-platform#Amount", Amount.getAmountInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Attribute",
				Attribute.getAttributeInstance());
		htblAllStaticClasses.put("http://iot-platform#CommunicatingDevice",
				CommunicatingDevice.getCommunicatingDeviceInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Condition", Condition.getConditionInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Coverage",
				Coverage.getCoverageInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Deployment", Deployment.getDeploymentInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#DeploymentRelatedProcess",
				DeploymentRelatedProcess.getDeploymentRelatedProcessInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Device", Device.getDeviceInstance());
		htblAllStaticClasses.put("http://iot-platform#DeviceModule", DeviceModule.getDeviceModuleInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#FeatureOfInterest",
				FeatureOfInterest.getFeatureOfInterestInstance());
		htblAllStaticClasses.put("http://xmlns.com/foaf/0.1/Group", Group.getGroupInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Input", Input.getInputInstance());
		htblAllStaticClasses.put("http://iot-platform#IOTSystem", IOTSystem.getIOTSystemInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#MeasurementCapability",
				MeasurementCapability.getMeasurementCapabilityInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#MeasurementProperty",
				MeasurementProperty.getMeasurementPropertyInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Metadata",
				Metadata.getMetadataInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Object",
				ObjectClass.getObjectClassInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Observation", Observation.getObservationInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#ObservationValue",
				ObservationValue.getObservationValueInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#OperatingProperty",
				OperatingProperty.getOperatingPropertyInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#OperatingRange",
				OperatingRange.getOperatingRangeInstance());
		htblAllStaticClasses.put("http://xmlns.com/foaf/0.1/Organization", Organization.getOrganizationInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Output", Output.getOutputInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Platform", Platform.getPlatformInstance());
		htblAllStaticClasses.put("http://www.w3.org/2003/01/geo/wgs84_pos#Point", Point.getPointInstacne());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Process", Process.getProcessInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Property",
				com.iotplatform.ontology.classes.Property.getPropertyInstance());
		htblAllStaticClasses.put("http://purl.org/NET/ssnx/qu/qu#QuantityKind", QuantityKind.getQuantityKindInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Sensing", Sensing.getSensingInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#SensingDevice",
				SensingDevice.getSensingDeviceInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Sensor", Sensor.getSensorInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#SensorDataSheet",
				SensorDataSheet.getSensorDataSheetInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#SensorOutput",
				SensorOutput.getSensorOutputInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Service", Service.getServiceInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Stimulus", Stimulus.getStimulusInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#SurvivalProperty",
				SurvivalProperty.getSurvivalPropertyInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#SurvivalRange",
				SurvivalRange.getSurvivalRangeInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#System", SystemClass.getSystemInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/UNIS/fiware/iot-lite#TagDevice",
				TagDevice.getTagDeviceInstance());
		htblAllStaticClasses.put("http://purl.org/NET/ssnx/qu/qu#Unit", Unit.getUnitInstance());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#BatteryLifetime",
				SurvivalProperty.getSurvivalPropertyInstance().getClassTypesList().get("BatteryLifetime"));
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#SystemLifetime",
				SurvivalProperty.getSurvivalPropertyInstance().getClassTypesList().get("SystemLifetime"));

		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#MaintenanceSchedule",
				OperatingProperty.getOperatingPropertyInstance().getClassTypesList().get("MaintenanceSchedule"));
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#OperatingPowerRange",
				OperatingProperty.getOperatingPropertyInstance().getClassTypesList().get("OperatingPowerRange"));

		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Accuracy",
				MeasurementProperty.getMeasurementPropertyInstance().getClassTypesList().get("Accuracy"));
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#DetectionLimit",
				MeasurementProperty.getMeasurementPropertyInstance().getClassTypesList().get("DetectionLimit"));
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Drift",
				MeasurementProperty.getMeasurementPropertyInstance().getClassTypesList().get("Drift"));
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Frequency",
				MeasurementProperty.getMeasurementPropertyInstance().getClassTypesList().get("Frequency"));
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Latency",
				MeasurementProperty.getMeasurementPropertyInstance().getClassTypesList().get("Latency"));
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#MeasurementRange",
				MeasurementProperty.getMeasurementPropertyInstance().getClassTypesList().get("MeasurementRange"));
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Precision",
				MeasurementProperty.getMeasurementPropertyInstance().getClassTypesList().get("Precision"));
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Resolution",
				MeasurementProperty.getMeasurementPropertyInstance().getClassTypesList().get("Resolution"));
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#ResponseTime",
				MeasurementProperty.getMeasurementPropertyInstance().getClassTypesList().get("ResponseTime"));
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Selectivity",
				MeasurementProperty.getMeasurementPropertyInstance().getClassTypesList().get("Selectivity"));
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Sensitivity",
				MeasurementProperty.getMeasurementPropertyInstance().getClassTypesList().get("Sensitivity"));

	}

}
