package com.iotplatform.validations;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.daos.DynamicConceptDao;
import com.iotplatform.daos.ValidationDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.InvalidRequestFieldsException;
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
import com.iotplatform.utilities.InsertionUtility;
import com.iotplatform.utilities.PropertyValue;
import com.iotplatform.utilities.SqlCondition;

import oracle.spatial.rdf.client.jena.Oracle;

/*
 * MultipleClassRequestValidation class is used to validate multiple Class request
 * (A request that consists of more than one object)
 * 
 * eg. ActuatingDevice,CommunicatingDevice,Sensor
 */

@Component
public class MultipleClassRequestValidation {

	private ValidationDao validationDao;
	private DynamicConceptDao dynamicConceptDao;
	private Hashtable<String, Class> htblAllStaticClasses;

	@Autowired
	public MultipleClassRequestValidation(ValidationDao validationDao, DynamicConceptDao dynamicConceptDao) {
		this.validationDao = validationDao;
		this.dynamicConceptDao = dynamicConceptDao;
		init();
	}

	/*
	 * it loads dynamic properties of the given classsList in the passed
	 * application domain and also caches the dynamic properties to improve
	 * performance
	 */
	public Hashtable<String, DynamicConceptModel> getDynamicProperties(String applicationName,
			ArrayList<Class> classList) {

		/*
		 * to get the dynamic properties only one time
		 */

		ArrayList<SqlCondition> orCondtionsFilterList = new ArrayList<>();

		for (Class subjectClass : classList) {
			orCondtionsFilterList
					.add(new SqlCondition(DynamicConceptColumns.CLASS_URI.toString(), subjectClass.getUri()));

			for (Class superClass : subjectClass.getSuperClassesList()) {
				orCondtionsFilterList
						.add(new SqlCondition(DynamicConceptColumns.CLASS_URI.toString(), superClass.getUri()));

			}
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

			Class subjectClass = htblAllStaticClasses.get(dynamicProperty.getClass_uri());

			// skip if the property was cached before
			if (subjectClass.getProperties().contains(dynamicProperty.getProperty_name())) {
				continue;
			}
			// System.out.println(dynamicProperty.getProperty_uri());

			subjectClass.getHtblPropUriName().put(dynamicProperty.getProperty_uri(),
					dynamicProperty.getProperty_name());

			if (dynamicProperty.getProperty_type().equals(PropertyType.DatatypeProperty.toString())) {
				subjectClass.getProperties().put(dynamicProperty.getProperty_name(),
						new DataTypeProperty(dynamicProperty.getProperty_name(),
								getPrefix(dynamicProperty.getProperty_prefix_alias()),
								getXSDDataTypeEnum(dynamicProperty.getProperty_object_type_uri()), applicationName,
								dynamicProperty.getHasMultipleValues(), dynamicProperty.getIsUnique()));
			} else {
				if (dynamicProperty.getProperty_type().equals(PropertyType.ObjectProperty.toString())) {
					subjectClass.getProperties().put(dynamicProperty.getProperty_name(), new ObjectProperty(
							dynamicProperty.getProperty_name(), getPrefix(dynamicProperty.getProperty_prefix_alias()),
							htblAllStaticClasses.get(dynamicProperty.getProperty_object_type_uri()), applicationName,
							dynamicProperty.getHasMultipleValues(), dynamicProperty.getIsUnique()));
				}
			}
			dynamicProperties.put(dynamicProperty.getProperty_name(), dynamicProperty);
		}

		return dynamicProperties;
	}

	/*
	 * isFieldsValid checks if the fields passed by the http request are valid
	 * or not and it returns a hashtable of propertyValue keyValue
	 * 
	 * This method takes a hashtable with class as a key and hashtable of
	 * fieldsValues ( fields(propertyName) and values, the values may be single
	 * value or an Arraylist or an object). It also takes the application
	 * subjectClass which is passed to thrown exception object.
	 * 
	 * This method will return ArrayList<PropertyValue> which breaks all the
	 * value objects or arraylist into single propertyValue pair and get the
	 * prfixPropertyName
	 *
	 * 
	 * to be inserted after that and also to be validated for data integrity
	 * constraint and unique constraint and dataType constraint a
	 */

	private ArrayList<PropertyValue> isFieldsValid(String applicationName,
			Hashtable<Class, Hashtable<String, PropertyValue>> htblClassFieldValue, Class requestSubjectClass) {

		// list of classes that need to bring their dynamic properties
		ArrayList<Class> classList = new ArrayList<>();

		/*
		 * This hashtable holds the fieldValue pair that has no mapping to a
		 * property and will be waiting to another check to dynamic properties
		 * after being loaded.
		 */
		Hashtable<String, PropertyValue> htblNotFoundFieldValue = new Hashtable<>();

		// returned propertyValue arraylist
		ArrayList<PropertyValue> returnedPropertyValueList = new ArrayList<>();

		Iterator<Class> htblPropertyValueIterator = htblClassFieldValue.keySet().iterator();

		while (htblPropertyValueIterator.hasNext()) {
			Class subjectClass = htblPropertyValueIterator.next();
			Hashtable<String, PropertyValue> htblFieldValue = htblClassFieldValue.get(subjectClass);

			/*
			 * iterate over request fieldValue pair of the specifiedClass
			 */
			Iterator<String> htblFieldValueIterator = htblFieldValue.keySet().iterator();

			while (htblFieldValueIterator.hasNext()) {
				String propertyName = htblFieldValueIterator.next();

				/*
				 * this object contains the value and a boolean isObject which
				 * expresses if this value was an object(represent a class
				 * propertyValue) before parsing, it is of type PropertyValue
				 * and it has only those two attributes
				 */
				PropertyValue valueType = htblFieldValue.get(propertyName);

				Property property = subjectClass.getProperties().get(propertyName);

				/*
				 * check if the property not exist in the
				 * htbClassesAllProperties hashtable . If it is not exist then
				 * add it to classList Arraylist to be passed to
				 * getDynamicPropertiesMethod at the end
				 */

				if (!subjectClass.getProperties().containsKey(propertyName)) {
					htblNotFoundFieldValue.put(propertyName, valueType);
					classList.add(subjectClass);
				} else {

					/*
					 * Field is valid and has a mapping for a property so we
					 * will break any arraylist value and get
					 * prefixedPropertyName then add it to
					 * returnedPropertyValueList
					 */

					returnedPropertyValueList.addAll(
							constructPropertyValue(property, valueType.getValue(), subjectClass, valueType.isObject()));

				}
			}

		}

		/*
		 * get Dynamic Properties
		 */

		if (classList.size() > 0) {
			Hashtable<String, DynamicConceptModel> loadedDynamicProperties = getDynamicProperties(applicationName,
					classList);

			/*
			 * Check that the fields that had no mappings are valid or not
			 */

			Iterator<String> htblNotFoundFieldValueIterator = htblNotFoundFieldValue.keySet().iterator();

			while (htblNotFoundFieldValueIterator.hasNext()) {
				String field = htblNotFoundFieldValueIterator.next();

				if (!loadedDynamicProperties.containsKey(field)) {
					throw new InvalidRequestFieldsException(requestSubjectClass.getName(), field);
				} else {

					/*
					 * passed field is a static property so add it to
					 * htblStaticProperty so check that the property is valid
					 * for this application domain
					 * 
					 * if the applicationName is null so this field maps a
					 * property in the main ontology .
					 * 
					 * if the applicationName is equal to passed applicationName
					 * so it is a dynamic added property to this application
					 * domain
					 * 
					 * else it will be a dynamic property in another application
					 * domain which will happen rarely
					 */

					Class subjectClass = htblAllStaticClasses.get(loadedDynamicProperties.get(field).getClass_uri());
					Property property = subjectClass.getProperties().get(field);

					if (property.getApplicationName() == null
							|| property.getApplicationName().equals(applicationName.replace(" ", "").toUpperCase())) {

						/*
						 * Field is valid dynamic property and has a mapping for
						 * a dynamic property so we will break any arraylist
						 * value and get prefixedPropertyName then add it to
						 * returnedPropertyValueList
						 */

						returnedPropertyValueList
								.addAll(constructPropertyValue(property, htblNotFoundFieldValue.get(field).getValue(),
										subjectClass, htblNotFoundFieldValue.get(field).isObject()));

					} else {

						/*
						 * this means that this class has a property with the
						 * same name but it is not for the specified application
						 * domain
						 */

						throw new InvalidRequestFieldsException(subjectClass.getName(), field);

					}
				}

			}
		}

		return returnedPropertyValueList;

	}

	/*
	 * constructPropertyValue method is used to construct an arraylist of
	 * PrefixedpropertyValues by breakdown any lists and add prefix to values
	 * and properties
	 */
	private ArrayList<PropertyValue> constructPropertyValue(Property property, Object value, Class subjectClass,
			boolean isObject) {

		ArrayList<PropertyValue> propValueList = new ArrayList<>();

		String prefixedProperty = property.getPrefix().getPrefix() + property.getName();
		/*
		 * multiple value and the value passed is instance of of array so It
		 * must be broken to propertyValue objects to be able to check if the
		 * values are valid in case the property is an objectProperty and to
		 * allow inserting values as triples
		 */

		if (property.isMulitpleValues() && value instanceof java.util.ArrayList) {
			ArrayList<Object> valueList = (ArrayList<Object>) value;
			for (int i = 0; i < valueList.size(); i++) {

				/*
				 * get prefixed value to follow semantic web structure
				 */
				Object prefixedValue = getValue(property, valueList.get(i));
				PropertyValue propertyValue = new PropertyValue(prefixedProperty, prefixedValue, subjectClass,
						isObject);
				propValueList.add(propertyValue);
			}
		} else {

			/*
			 * Its a normal property value pair so I will only create a
			 * propertyValue object to hold them and add the object to
			 * propValueList
			 */

			Object prefixedValue = getValue(property, value);
			PropertyValue propertyValue = new PropertyValue(prefixedProperty, prefixedValue, subjectClass, isObject);
			propValueList.add(propertyValue);

		}
		return propValueList;
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

		if (XSDDataTypes.boolean_type.getDataType().equals(dataType)) {
			return XSDDataTypes.boolean_type;
		}

		if (XSDDataTypes.decimal_typed.getDataType().equals(dataType)) {
			return XSDDataTypes.decimal_typed;
		}

		if (XSDDataTypes.float_typed.getDataType().equals(dataType)) {
			return XSDDataTypes.float_typed;
		}

		if (XSDDataTypes.integer_typed.getDataType().equals(dataType)) {
			return XSDDataTypes.integer_typed;
		}

		if (XSDDataTypes.string_typed.getDataType().equals(dataType)) {
			return XSDDataTypes.string_typed;
		}

		if (XSDDataTypes.dateTime_typed.getDataType().equals(dataType)) {
			return XSDDataTypes.dateTime_typed;
		}

		if (XSDDataTypes.double_typed.getDataType().equals(dataType)) {
			return XSDDataTypes.double_typed;
		}

		return null;
	}

	/*
	 * isDataValueValid checks that the datatype of the values passed with the
	 * property are valid to maintain data integrity and consistency.
	 * 
	 */
	private boolean isDataValueValid(DataTypeProperty dataProperty, Object value) {

		XSDDataTypes xsdDataType = dataProperty.getDataType();
		switch (xsdDataType) {
		case boolean_type:
			if (value instanceof Boolean) {
				return true;
			} else {
				return false;
			}
		case decimal_typed:
			if (value instanceof Double) {
				return true;
			} else {
				return false;
			}
		case float_typed:
			if (value instanceof Float) {
				return true;
			} else {
				return false;
			}
		case integer_typed:
			if (value instanceof Integer) {
				return true;
			} else {
				return false;
			}
		case string_typed:
			if (value instanceof String) {
				return true;
			} else {
				return false;
			}
		case dateTime_typed:
			if (value instanceof XMLGregorianCalendar) {
				return true;
			} else {
				return false;
			}
		case double_typed:
			if (value instanceof Double) {
				return true;
			} else {
				return false;
			}
		default:
			return false;
		}

	}

	/*
	 * getValue method returns the appropriate value by appending a prefix
	 */
	private Object getValue(Property property, Object value) {

		if (property instanceof DataTypeProperty) {
			XSDDataTypes xsdDataType = ((DataTypeProperty) property).getDataType();
			value = "\"" + value.toString() + "\"" + xsdDataType.getXsdType();
			return value;
		} else {
			return Prefixes.IOT_PLATFORM.getPrefix() + value;
		}
	}

	/*
	 * isObjectPropertiesValuesValid method takes a list of
	 * prefiexedPropertyValue
	 * 
	 * isProrpertyValueValid checks if the property value is valid (No Data
	 * Integrity constraint and no unique constraint violations)
	 * 
	 * if property value is valid it returns true else it will throw an
	 * appropriate error to determine which type of constraint violations
	 * occured
	 */
	private boolean isObjectPropertiesValuesValid(ArrayList<PropertyValue> prefixedPropertyValue) {
		return false;
	}

	private void init() {
		htblAllStaticClasses = new Hashtable<>();
		htblAllStaticClasses.put("http://iot-platform#Application", new Application());
		htblAllStaticClasses.put("http://xmlns.com/foaf/0.1/Person", new Person());
		htblAllStaticClasses.put("http://iot-platform#Admin", new Admin());
		htblAllStaticClasses.put("http://iot-platform#Developer", new Developer());
		htblAllStaticClasses.put("http://iot-platform#NormalUser", new NormalUser());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/UNIS/fiware/iot-lite#ActuatingDevice",
				new ActuatingDevice());
		htblAllStaticClasses.put("http://xmlns.com/foaf/0.1/Agent", new Agent());
		htblAllStaticClasses.put("http://iot-platform#Amount", new Amount());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Attribute", new Attribute());
		htblAllStaticClasses.put("http://iot-platform#CommunicatingDevice", new CommunicatingDevice());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Condition", new Condition());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Coverage", new Coverage());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Deployment", new Deployment());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#DeploymentRelatedProcess",
				new DeploymentRelatedProcess());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Device", new Device());
		htblAllStaticClasses.put("http://iot-platform#DeviceModule", new DeviceModule());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#FeatureOfInterest", new FeatureOfInterest());
		htblAllStaticClasses.put("http://xmlns.com/foaf/0.1/Group", new Group());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Input", new Input());
		htblAllStaticClasses.put("http://iot-platform#IOTSystem", new IOTSystem());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#MeasurementCapability",
				new MeasurementCapability());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#MeasurementProperty", new MeasurementProperty());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Metadata", new Metadata());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Object", new ObjectClass());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Observation", new Observation());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#ObservationValue", new ObservationValue());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#OperatingProperty", new OperatingProperty());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#OperatingRange", new OperatingRange());
		htblAllStaticClasses.put("http://xmlns.com/foaf/0.1/Organization", new Organization());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Output", new Output());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Platform", new Platform());
		htblAllStaticClasses.put("http://www.w3.org/2003/01/geo/wgs84_pos#Point", new Point());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Process", new Process());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Property",
				new com.iotplatform.ontology.classes.Property());
		htblAllStaticClasses.put("http://purl.org/NET/ssnx/qu/qu#QuantityKind", new QuantityKind());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Sensing", new Sensing());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#SensingDevice", new SensingDevice());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Sensor", new Sensor());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#SensorDataSheet", new SensorDataSheet());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#SensorOutput", new SensorOutput());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Service", new Service());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#Stimulus", new Stimulus());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#SurvivalProperty", new SurvivalProperty());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#SurvivalRange", new SurvivalRange());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/ssnx/ssn#System", new SystemClass());
		htblAllStaticClasses.put("http://purl.oclc.org/NET/UNIS/fiware/iot-lite#TagDevice", new TagDevice());
		htblAllStaticClasses.put("http://purl.org/NET/ssnx/qu/qu#Unit", new Unit());
	}

	public Hashtable<String, Class> getHtblAllStaticClasses() {
		return htblAllStaticClasses;
	}

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

		DynamicConceptDao dynamicConceptDao = new DynamicConceptDao(dataSource);
		ValidationDao validationDao = new ValidationDao(new Oracle(szJdbcURL, szUser, szPasswd));

		MultipleClassRequestValidation multipleClassRequestValidation = new MultipleClassRequestValidation(
				validationDao, dynamicConceptDao);

		// testing loading dynamic properties

		// ArrayList<Class> classList = new ArrayList<>();
		// ActuatingDevice actuatingDevice = new ActuatingDevice();
		// classList.add(actuatingDevice);
		// classList.add(new Service());
		// classList.add(new OperatingRange());
		// classList.add(new SurvivalRange());
		//
		// System.out.println(multipleClassRequestValidation.getDynamicProperties("TESTAPPLICATION",
		// classList));
		// System.out.println("==========================================================================");
		// System.out.println(actuatingDevice.getProperties().toString());

		// testing isValidField method

		Hashtable<Class, Hashtable<String, PropertyValue>> htblClassFieldValue = new Hashtable<>();

		ArrayList<String> hatersList = new ArrayList<>();
		hatersList.add("HatemMorgan");
		hatersList.add("AhmedMorgan");

		ArrayList<String> mails = new ArrayList<>();
		mails.add("hatemmorgan17@gmail.com");
		mails.add("hatem.el-sayed@student.guc.edu.eg");

		Hashtable<String, PropertyValue> htblDeveloperPropValues = new Hashtable<>();
		htblDeveloperPropValues.put("firstName", new PropertyValue("Hatem", false));
		htblDeveloperPropValues.put("knows", new PropertyValue("HatemMorgan", false));
		htblDeveloperPropValues.put("mbox", new PropertyValue(mails, false));
		htblDeveloperPropValues.put("hates", new PropertyValue(hatersList, false));

		Hashtable<String, PropertyValue> htblActuatingDevicePropValues = new Hashtable<>();
		htblActuatingDevicePropValues.put("hasOperatingRange", new PropertyValue("90129-219301-219031", false));
		htblActuatingDevicePropValues.put("test", new PropertyValue("2134-2313-242-33332", false));

		htblClassFieldValue.put(
				multipleClassRequestValidation.getHtblAllStaticClasses().get("http://iot-platform#Developer"),
				htblDeveloperPropValues);
		htblClassFieldValue.put(
				multipleClassRequestValidation.getHtblAllStaticClasses()
						.get("http://purl.oclc.org/NET/UNIS/fiware/iot-lite#ActuatingDevice"),
				htblActuatingDevicePropValues);

		System.out.println(multipleClassRequestValidation.isFieldsValid("TESTAPPLICATION", htblClassFieldValue,
				multipleClassRequestValidation.getHtblAllStaticClasses()
						.get("http://purl.oclc.org/NET/UNIS/fiware/iot-lite#ActuatingDevice")));

	}

}
