package com.iotplatform.utilities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
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
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.Property;
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
import com.iotplatform.validations.RequestFieldsValidation;
import com.iotplatform.validations.SingleClassRequestValidation;

@Component
public class SelectionUtility {

	private DynamicConceptDao dynamicConceptDao;
	private Hashtable<String, Class> htblAllStaticClasses;

	@Autowired
	public SelectionUtility(DynamicConceptDao dynamicConceptDao) {
		this.dynamicConceptDao = dynamicConceptDao;
		init();
	}

	/*
	 * constructQueryResult method used to return results without any prefixed
	 * ontology URIs
	 */

	private Object[] constructSinglePropertyValuePair(String applicationName, String propertyURI, Object value,
			Class subjectClass) {
		Object[] res = new Object[2];

		String propertyName = subjectClass.getHtblPropUriName().get(propertyURI);

		if (propertyName == null) {

			/*
			 * update subject class properties list by loading the dynamic
			 * properties from database
			 */

			getDynamicProperties(applicationName, subjectClass);
			propertyName = subjectClass.getHtblPropUriName().get(propertyURI);

		}

		Property property = subjectClass.getProperties().get(propertyName);

		if (property instanceof ObjectProperty) {
			value = value.toString().substring(Prefixes.IOT_PLATFORM.getUri().length(), value.toString().length());
		} else {
			/*
			 * datatype property
			 */
			value = typeCastValueToItsDataType((DataTypeProperty) property, value);
		}

		res[0] = propertyName;
		res[1] = value;

		return res;
	}

	private static Object typeCastValueToItsDataType(DataTypeProperty dataTypeProperty, Object value) {
		if (XSDDataTypes.boolean_type.equals(dataTypeProperty.getDataType())) {
			return Boolean.parseBoolean(value.toString());
		}

		if (XSDDataTypes.decimal_typed.equals(dataTypeProperty.getDataType())) {
			return Double.parseDouble(value.toString());
		}

		if (XSDDataTypes.float_typed.equals(dataTypeProperty.getDataType())) {
			return Float.parseFloat(value.toString());
		}

		if (XSDDataTypes.integer_typed.equals(dataTypeProperty.getDataType())) {
			return Integer.parseInt(value.toString());
		}

		if (XSDDataTypes.string_typed.equals(dataTypeProperty.getDataType())) {
			return value.toString();
		}

		if (XSDDataTypes.dateTime_typed.equals(dataTypeProperty.getDataType())) {
			return Date.parse(value.toString());
		}

		if (XSDDataTypes.double_typed.equals(dataTypeProperty.getDataType())) {
			return Double.parseDouble(value.toString());
		}
		return null;
	}
	/*
	 * constractResponeJsonObject method is responsible take two inputs
	 * 
	 * 1-The ResultSet returned from querying the application model and take the
	 * 2-subjectClass to check if the property returned has multiple values or
	 * not. If it has multiple value it should be returned as an arrayList
	 */

	public List<Hashtable<String, Object>> constractResponeJsonObjectForListSelection(String applicationName,
			ResultSet results, Class subjectClass) throws SQLException {

		List<Hashtable<String, Object>> responseJson = new ArrayList<>();

		Hashtable<Object, Hashtable<String, Object>> temp = new Hashtable<>();

		Hashtable<String, ArrayList<Object>> multipleValuePropNameValueArr = new Hashtable<>();

		Hashtable<String, Property> subjectClassProperties = subjectClass.getProperties();

		while (results.next()) {

			Object subject = results.getObject(1);

			/*
			 * create a new hashtable to hold subject's property and value
			 */

			if (temp.size() == 0) {
				Hashtable<String, Object> htblSubjectPropVal = new Hashtable<>();
				temp.put(subject, htblSubjectPropVal);
				responseJson.add(htblSubjectPropVal);
			}

			// skip rdf:type property

			if (results.getString(2).equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
				continue;
			}

			Object[] preparedPropVal = constructSinglePropertyValuePair(applicationName, results.getString(2),
					results.getString(3), subjectClass);

			String propertyName = preparedPropVal[0].toString();
			Object value = preparedPropVal[1];

			/*
			 * check if the property passed is a multiValues Property it is
			 * after calling constructSinglePropertyValuePair method because
			 * constructSinglePropertyValuePair checks if the property was
			 * cached or need to be cached
			 */

			Property property = subjectClassProperties.get(propertyName);

			if (property.isMulitpleValues()) {

				/*
				 * check if the property was added before to use the previous
				 * created value array
				 */

				if (temp.containsKey(subject)) {

					if (temp.get(subject).containsKey(propertyName)) {

						((ArrayList) temp.get(subject).get(propertyName)).add(value);
					} else {

						/*
						 * property was not added before so create a new
						 * arraylist of objects to hold values and add it to
						 * multipleValuePropNameValueArr hashtable
						 */

						ArrayList<Object> valueList = new ArrayList<>();
						valueList.add(value);
						temp.get(subject).put(propertyName, valueList);
					}
				} else {
					Hashtable<String, Object> htblAdminPropVal = new Hashtable<>();
					temp.put(subject, htblAdminPropVal);

					if (temp.get(subject).containsKey(propertyName)) {

						((ArrayList) temp.get(subject).get(propertyName)).add(value);
					} else {

						/*
						 * property was not added before so create a new
						 * arraylist of objects to hold values and add it to
						 * multipleValuePropNameValueArr hashtable
						 */

						ArrayList<Object> valueList = new ArrayList<>();
						valueList.add(value);
						temp.get(subject).put(propertyName, valueList);
					}
					responseJson.add(htblAdminPropVal);
				}

			} else {

				/*
				 * as long as the current subject equal to subject got from the
				 * results then add the property and value to the hashtable . If
				 * they are not the same this means that this is a new subject
				 * so we have to construct a new hashtable to hold its data
				 */

				if (temp.containsKey(subject)) {
					temp.get(subject).put(propertyName, value);
				} else {

					Hashtable<String, Object> htblAdminPropVal = new Hashtable<>();
					temp.put(subject, htblAdminPropVal);
					temp.get(subject).put(propertyName, value);
					responseJson.add(htblAdminPropVal);

				}
			}
		}
		return responseJson;
	}

	/*
	 * it loads dynamic properties of the given class in the passed application
	 * domain and also caches the dynamic properties to improve performance
	 */
	private Hashtable<String, DynamicConceptModel> getDynamicProperties(String applicationName, Class subjectClass) {

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
	private Prefixes getPrefix(String prefixAlias) {

		if (Prefixes.FOAF.getPrefix().equals(prefixAlias)) {
			return Prefixes.FOAF;
		}

		if (Prefixes.SSN.getPrefix().equals(prefixAlias)) {
			return Prefixes.SSN;
		}

		if (Prefixes.IOT_LITE.getPrefix().equals(prefixAlias)) {
			return Prefixes.IOT_LITE;
		}

		if (Prefixes.IOT_PLATFORM.getPrefix().equals(prefixAlias)) {
			return Prefixes.IOT_PLATFORM;
		}

		if (Prefixes.GEO.getPrefix().equals(prefixAlias)) {
			return Prefixes.GEO;
		}

		if (Prefixes.XSD.getPrefix().equals(prefixAlias)) {
			return Prefixes.XSD;
		}

		if (Prefixes.OWL.getPrefix().equals(prefixAlias)) {
			return Prefixes.OWL;
		}

		if (Prefixes.RDFS.getPrefix().equals(prefixAlias)) {
			return Prefixes.RDFS;
		}

		if (Prefixes.RDF.getPrefix().equals(prefixAlias)) {
			return Prefixes.RDF;
		}

		if (Prefixes.QU.getPrefix().equals(prefixAlias)) {
			return Prefixes.QU;
		}

		if (Prefixes.DUL.getPrefix().equals(prefixAlias)) {
			return Prefixes.DUL;
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
	}

}
