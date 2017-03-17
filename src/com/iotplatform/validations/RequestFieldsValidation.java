package com.iotplatform.validations;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.daos.DynamicConceptDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.InvalidRequestFieldsException;
import com.iotplatform.exceptions.InvalidTypeValidationException;
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
import com.iotplatform.utilities.PropertyValue;
import com.iotplatform.utilities.SqlCondition;
import com.iotplatform.utilities.ValueOfFieldNotMappedToStaticProperty;

/*
 * RequestFieldsValidation class is used to validate post request body and parse it.
 * 
 *  It checks for Obligatory fields exist in the request body (fields that must be exist for that class 
 *  because not all fields are required to be exist)
 *  
 *  It checks that fields passed by the request are valid fields by checking that they maps to existing properties 
 *  in the passed subject class (which maps the ontology classes)
 *  
 *  It parse request body (JSON) into classes and properties in order to perform a mapping from JSON to semantic web
 *  structure so it can then be validated for constraint violation and then be inserted in the appropriate format in 
 *  the graph database
 * 
 */

@Component
public class RequestFieldsValidation {

	/*
	 * dynamicConceptDao class is used to get all dynamic properties or dynamic
	 * classes added to the ontology
	 */
	private DynamicConceptDao dynamicConceptDao;
	private Hashtable<String, Class> htblAllStaticClasses;

	@Autowired
	public RequestFieldsValidation(DynamicConceptDao dynamicConceptDao) {
		this.dynamicConceptDao = dynamicConceptDao;
		init();
	}

	/*
	 * validateRequestFields method is takes Hashtable<String, Object>
	 * htblFieldValue which is the request body represented as key fieldName and
	 * object value and also it takes the subjectClass which is the specified
	 * class in the request url eg. Sensor,ActuatingDevice
	 * 
	 * 
	 */
	public Hashtable<Class, ArrayList<PropertyValue>> validateRequestFields(String applicationName,
			Hashtable<String, Object> htblFieldValue, Class subjectClass) {

		Iterator<String> htblFieldValueIterator = htblFieldValue.keySet().iterator();

		/*
		 * List of classes that need to get their dynamic properties to check if
		 * the fields maps to one of them or these fields are invalid fields
		 */
		ArrayList<Class> classList = new ArrayList<>();

		/*
		 * htblNotFoundFieldValue holds fieldsValue pairs that do not have a
		 * static property mapping and are waited to be checked for mapping
		 * after loading dynamic properties
		 */
		Hashtable<String, ValueOfFieldNotMappedToStaticProperty> htblNotFoundFieldValue = new Hashtable<>();

		/*
		 * htblClassPropertyValue holds the constructed propertyValue
		 */
		Hashtable<Class, ArrayList<ArrayList<PropertyValue>>> htblClassPropertyValue = new Hashtable<>();

		/*
		 * check if there is a fieldName= type which means that value of this
		 * field describes a type class then change the subClass type to be the
		 * subjectClass
		 */
		if (htblFieldValue.containsKey("type") && isobjectValueValidType(subjectClass, htblFieldValue.get("type"))) {
			Class subClassSubject = subjectClass.getClassTypesList().get(htblFieldValue.get("type").toString());
			subjectClass = subClassSubject;

			/*
			 * remove the keyValue pair from htblFIeld to avoid further
			 * validation eg.(field Validation)
			 */
			htblFieldValue.remove("type");
		} else {

			/*
			 * throw an error if the type field value is not a valid type
			 */
			if (htblFieldValue.containsKey("type") && !isobjectValueValidType(subjectClass, htblFieldValue.get("type")))

				throw new InvalidTypeValidationException(subjectClass.getName(),
						subjectClass.getClassTypesList().keySet(), subjectClass.getName());
		}

		/*
		 * There is no uniqueIdentfier for this object class so the platform has
		 * to generate a UUID to be the unique Identifier that will be the
		 * subject and will be the value of ID property defined by the platform
		 */

		if (!subjectClass.isHasUniqueIdentifierProperty()) {

			String id = UUID.randomUUID().toString();

			/*
			 * prefixing objectUniqueIdentifier
			 */

			id = "\"" + id + "\"" + XSDDataTypes.string_typed.getXsdType();

			/*
			 * add property id for classTypeObject and add generated UUID as the
			 * object of it
			 */

			Property idProperty = subjectClass.getProperties().get("id");
			PropertyValue idPropertyValue = new PropertyValue(idProperty.getPrefix().getPrefix() + idProperty.getName(),
					id, false);

			/*
			 * add idPropertyValue object to htblClassPropertyValue
			 */

			ArrayList<ArrayList<PropertyValue>> listPropertyValueList = new ArrayList<>();
			ArrayList<PropertyValue> propertyValueList = new ArrayList<>();
			listPropertyValueList.add(propertyValueList);
			htblClassPropertyValue.put(subjectClass, listPropertyValueList);
			htblClassPropertyValue.get(subjectClass).get(0).add(idPropertyValue);

		}

		/*
		 * Iterate on htblFieldValue
		 */
		while (htblFieldValueIterator.hasNext()) {
			String fieldName = htblFieldValueIterator.next();
			Object value = htblFieldValue.get(fieldName);

			/*
			 * if it returns true then the field is a valid field so we have to
			 * parse the value to determine if we need to do further check if
			 * value maps another class eg. hasSurvivalRange property for Sensor
			 * or ActuatingDevice or it need to reconstruct the value to follow
			 * the semantic web structure in order to do further validations and
			 * insertion
			 * 
			 * if it return false means that no static mapping so it will add
			 * the subject class to classList and fieldNameValue pair to
			 * htblNotFoundFieldValue
			 */

			if (isFieldMapsToStaticProperty(subjectClass, fieldName, value, classList, htblNotFoundFieldValue, 0)) {
				Property property = subjectClass.getProperties().get(fieldName);

				parseAndConstructFieldValue(subjectClass, property, value, htblClassPropertyValue, classList,
						htblNotFoundFieldValue, 0);
			}

		}

		/*
		 * get Dynamic Properties of the classes in the classList which contains
		 * the domain class of the fields in the request that are not mapped to
		 * static properties
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

				/*
				 * If field does not map to loaded dynamic properties, I will
				 * throw InvalidRequestFieldsException to indicate the field is
				 * invalid
				 */
				if (!loadedDynamicProperties.containsKey(field)) {
					throw new InvalidRequestFieldsException(subjectClass.getName(), field);
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

					Class dynamicPropertyClass = htblAllStaticClasses
							.get(loadedDynamicProperties.get(field).getClass_uri());
					Property property = dynamicPropertyClass.getProperties().get(field);

					if (property.getApplicationName() == null
							|| property.getApplicationName().equals(applicationName.replace(" ", "").toUpperCase())) {

						/*
						 * Field is valid dynamic property and has a mapping for
						 * a dynamic property. So I will parse the value and add
						 * prefixes
						 */

						parseAndConstructFieldValue(htblNotFoundFieldValue.get(field).getPropertyClass(), property,
								htblNotFoundFieldValue.get(field).getPropertyValue(), htblClassPropertyValue, classList,
								htblNotFoundFieldValue, htblNotFoundFieldValue.get(field).getClassInstanceIndex());

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

		Iterator<Class> iterator = htblClassPropertyValue.keySet().iterator();
		while (iterator.hasNext()) {
			Class clss = iterator.next();
			System.out.println(clss.getName() + "[ ");
			ArrayList<ArrayList<PropertyValue>> list = htblClassPropertyValue.get(clss);
			for (ArrayList<PropertyValue> arrayList : list) {
				System.out.print(arrayList);
				System.out.println();
			}
			System.out.println(" ]");
		}

		System.out.println("==============================================");
		System.out.println(htblClassPropertyValue.toString());
		System.out.println("=======================================");
		System.out.println(classList.toString());
		System.out.println("======================================");
		System.out.println(htblNotFoundFieldValue.toString());
		return null;
	}

	/*
	 * isFieldMapsToStaticProperty checks if a field maps to a static property (
	 * has map in the list of properties of passed subject class)
	 * 
	 * it returns true if there is a mapping
	 * 
	 * return false if there is no mapping and add subject class to passed
	 * classList in order to get dynamic properties of it and it will add the
	 * field and value to htblNotFoundFieldValue hashtable to be checked again
	 * after laading dynamic properties
	 */
	private boolean isFieldMapsToStaticProperty(Class subjectClass, String fieldName, Object value,
			ArrayList<Class> classList, Hashtable<String, ValueOfFieldNotMappedToStaticProperty> htblNotFoundFieldValue,
			int index) {

		if (subjectClass.getProperties().containsKey(fieldName)) {
			return true;
		} else {

			classList.add(subjectClass);
			ValueOfFieldNotMappedToStaticProperty notMappedFieldValue = new ValueOfFieldNotMappedToStaticProperty(
					subjectClass, value, index);
			htblNotFoundFieldValue.put(fieldName, notMappedFieldValue);
			return false;
		}
	}

	/*
	 * parseAndConstructFieldValue method is used to parse the Object value and
	 * reconstruct into prefixed property and prefixed value (constrcut a
	 * propertyValue object) and then add it to htblClassPropertyValue
	 * 
	 * htblClassPropertyValue is a hashtable with Class as key and an arraylist
	 * of arraylists where every arrayList<PropertyValue> represents the
	 * propertyValues of an instance of the class key . The indexCount
	 * represents the index of the instance of the class hashtableKey
	 */
	private void parseAndConstructFieldValue(Class subjectClass, Property property, Object value,
			Hashtable<Class, ArrayList<ArrayList<PropertyValue>>> htblClassPropertyValue, ArrayList<Class> classList,
			Hashtable<String, ValueOfFieldNotMappedToStaticProperty> htblNotFoundFieldValue, int indexCount) {

		// System.out.println("----->" + subjectClass.getName() + " " +
		// property.getName() + " " + value.toString());

		/*
		 * check if the value is of type primitive datatype
		 */
		if ((value instanceof String) || (value instanceof Integer) || (value instanceof Float)
				|| (value instanceof Double) || (value instanceof Boolean)) {

			/*
			 * construct a new PropertyValue instance to hold the prefiexed
			 * propertyName and prefixed value
			 */
			PropertyValue propertyValue = new PropertyValue(property.getPrefix().getPrefix() + property.getName(),
					getValue(property, value), false);

			/*
			 * add PropertyValue object to htblClassPropertyValue
			 */
			int classInstanceIndex = htblClassPropertyValue.get(subjectClass).size() - 1;
			htblClassPropertyValue.get(subjectClass).get(classInstanceIndex).add(propertyValue);

		}

		// =========================================================================================================
		// Object Value
		// =========================================================================================================

		/*
		 * value is a nested object so I will iterate on all the keyValue pairs
		 * and check if the fields are valid or not and reconstruct them
		 * 
		 * eg: hasValue: { hasDataValue : 20.2 }
		 * 
		 * { hasDataValue : 20.2 } is an instance of class Amount
		 * 
		 */
		if (value instanceof java.util.LinkedHashMap<?, ?> && property instanceof ObjectProperty) {
			LinkedHashMap<String, Object> valueObject = (LinkedHashMap<String, Object>) value;
			Class classType = ((ObjectProperty) property).getObject();

			/*
			 * linking subject class with object class by adding a the unique
			 * identifier as the object value of the property
			 * 
			 * if it has unique Identifier this means that a unique property
			 * value added by the user must be the unique identifier that will
			 * be the subject of object value and references the object value
			 * instance to the subject
			 */
			String objectUniqueIdentifier;
			if (classType.isHasUniqueIdentifierProperty()) {
				Property uniqueIdentifierProperty = classType.getUniqueIdentifierProperty();
				objectUniqueIdentifier = valueObject.get(uniqueIdentifierProperty.getName()).toString();

			} else {
				/*
				 * There is no uniqueIdentfier for this object class so the
				 * platform has to generate a UUID to be the unique Identifier
				 * that will be the subject of object value and references the
				 * object value instance to the subject
				 */

				objectUniqueIdentifier = UUID.randomUUID().toString();

			}

			/*
			 * construct a new PropertyValue instance to hold the prefiexed
			 * propertyName and prefixed value
			 * 
			 * Here I am linking the subjectClass with a reference
			 * (uniqueIdentifier) of the new class instance to construct a
			 * proper graph nodes with relationships
			 * 
			 * so I create a new PropertyValue instance to hold the property and
			 * the uniqueIdentifier value that represents a unique reference to
			 * objectValue
			 * 
			 */
			PropertyValue propertyValue = new PropertyValue(property.getPrefix().getPrefix() + property.getName(),
					getValue(property, objectUniqueIdentifier), true);

			/*
			 * add PropertyValue object to htblClassPropertyValue
			 * 
			 * indexCount represents the index of the subjectClassInstance
			 */

			// System.out.println(subjectClass.getName() + " " + indexCount + "
			// " + value.toString());
			htblClassPropertyValue.get(subjectClass).get(indexCount).add(propertyValue);

			/*
			 * check if there is a fieldName= type which means that value of
			 * this field describes a type class then change the subClass type
			 * to be the subjectClass
			 */

			if (valueObject.containsKey("type") && isobjectValueValidType(classType, valueObject.get("type"))) {
				Class subClassSubject = classType.getClassTypesList().get(valueObject.get("type").toString());
				classType = subClassSubject;

				/*
				 * remove the keyValue pair from htblFIeld to avoid further
				 * validation eg.(field Validation)
				 */
				valueObject.remove("type");
			} else {

				/*
				 * throw an error if the type field value is not a valid type
				 */
				if (valueObject.containsKey("type") && !isobjectValueValidType(subjectClass, valueObject.get("type")))

					throw new InvalidTypeValidationException(classType.getName(),
							classType.getClassTypesList().keySet(), subjectClass.getName());

			}

			/*
			 * Check if the classType exist in htblClassPropertyValue.
			 * 
			 * If it exist, this means that a new class instance of this class
			 * has to be created and I have to increment the indexCount
			 */

			if (htblClassPropertyValue.containsKey(classType)) {
				ArrayList<PropertyValue> propertyValueList = new ArrayList<>();
				htblClassPropertyValue.get(classType).add(propertyValueList);
			} else {
				ArrayList<ArrayList<PropertyValue>> listPropertyValueList = new ArrayList<>();
				ArrayList<PropertyValue> propertyValueList = new ArrayList<>();
				listPropertyValueList.add(propertyValueList);
				htblClassPropertyValue.put(classType, listPropertyValueList);
			}

			/*
			 * add property id for classTypeObject and add generated UUID as the
			 * object of it
			 */
			Property idProperty = classType.getProperties().get("id");
			PropertyValue idPropertyValue = new PropertyValue(idProperty.getPrefix().getPrefix() + idProperty.getName(),
					"\"" + objectUniqueIdentifier + "\"" + XSDDataTypes.string_typed.getXsdType(), false);

			/*
			 * add idPropertyValue object to htblClassPropertyValue
			 * 
			 * I will always add a new property to the last instanceClass
			 * represented by an arraylist<PropertyValue> because I finish a
			 * single object then return back recursively to complete parsing
			 * other fields
			 * 
			 * any new propertyValue added will be for the same instance so it
			 * will be always exist in the end of the arraylist that represent
			 * instances of classType. Because I iterate on the fields of the
			 * new object instance so I will finish the current new instance of
			 * classType then complete the rest fields
			 * 
			 */
			int classInstanceIndex = htblClassPropertyValue.get(classType).size() - 1;
			htblClassPropertyValue.get(classType).get(classInstanceIndex).add(idPropertyValue);

			for (String fieldName : valueObject.keySet()) {

				/*
				 * if it returns true then the field is a valid field (it maps
				 * to a property in the properties list of passed classs)
				 * 
				 * if it return false means that no static mapping so it will
				 * add the subject class to classList and fieldNameValue pair to
				 * htblNotFoundFieldValue
				 */

				Object fieldValue = valueObject.get(fieldName);
				if (isFieldMapsToStaticProperty(classType, fieldName, fieldValue, classList, htblNotFoundFieldValue,
						classInstanceIndex)) {

					Property classTypeProperty = classType.getProperties().get(fieldName);

					parseAndConstructFieldValue(classType, classTypeProperty, fieldValue, htblClassPropertyValue,
							classList, htblNotFoundFieldValue, classInstanceIndex);
				}

			}
		}

		// =========================================================================================================
		// List Value
		// =========================================================================================================

		/*
		 * value is a list values (value may be datatype values or object
		 * values) so I will iterate on the list of values and recursively parse
		 * the values and make field validations
		 * 
		 * eg: hasSurvivalRange : [ { type: "SystemLifeTime" }, { type:
		 * "BatteryLifeTime" } ]
		 * 
		 * 
		 * or : mbox: ["hatem@gmail.com","jsid@yahoo.com"]
		 */
		if (value instanceof java.util.ArrayList) {
			ArrayList<Object> valueList = (ArrayList<Object>) value;

			/*
			 * iterate on the list and do a recursive call to parse and validate
			 * every single value in the valueList
			 */
			for (Object singleValue : valueList) {

				parseAndConstructFieldValue(subjectClass, property, singleValue, htblClassPropertyValue, classList,
						htblNotFoundFieldValue, indexCount);
			}
		}
	}

	/*
	 * objectTypeCheck method checks if the type value is valid or not
	 */
	private boolean isobjectValueValidType(Class subjectClass, Object value) {

		if (subjectClass.isHasTypeClasses()) {
			return subjectClass.getClassTypesList().containsKey(value.toString());
		} else {
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

		RequestFieldsValidation requestFieldsValidation = new RequestFieldsValidation(dynamicConceptDao);

		// { "hasCoverage":[
		// {"type":"Circle","location": [
		//
		// {"lat":22.2132,"long":-4.31211},{"lat":22.2132,"long": -4.31211},
		// {"lat":22.2132,"long": -4.31211} ]
		//
		// },
		//
		// { "type":"Circle","location":[
		// {"lat":22.2132,"long": -4.31211},{"lat":22.2132,"long": -4.31211},
		// {"lat":22.2132,"long":-4.31211} ]
		//
		// }
		//
		// ],
		//
		// "hasSurvivalRange":{"inCondition":{"description":"High Tempreture
		// Condition"},
		//
		// "hasSurvivalPorperty":[
		// {"type":"BatteryLifeTime","hasValue":{"hasDataValue":20.01}},
		// {"type":"SystemLifeTime","hasValue": {"hasDataValue":200.01}}
		//
		// ]
		//
		// }
		// }

		Hashtable<String, Object> htblFieldValue = new Hashtable<>();

		LinkedHashMap<String, Object> condition = new LinkedHashMap<>();
		condition.put("description", "High Tempreture Condition");

		LinkedHashMap<String, Object> batteryLifetimeAmount = new LinkedHashMap<>();
		batteryLifetimeAmount.put("hasDataValue", 20.21);

		LinkedHashMap<String, Object> systemLifetimeAmount = new LinkedHashMap<>();
		systemLifetimeAmount.put("hasDataValue", 200.21);

		LinkedHashMap<String, Object> batteryLifetime = new LinkedHashMap<>();
		batteryLifetime.put("type", "BatteryLifetime");
		batteryLifetime.put("hasValue", batteryLifetimeAmount);

		LinkedHashMap<String, Object> systemLifetime = new LinkedHashMap<>();
		systemLifetime.put("type", "SystemLifetime");
		systemLifetime.put("hasValue", systemLifetimeAmount);

		ArrayList<LinkedHashMap<String, Object>> survivalProperties = new ArrayList<>();
		survivalProperties.add(systemLifetime);
		survivalProperties.add(batteryLifetime);

		LinkedHashMap<String, Object> survivalRange = new LinkedHashMap<>();
		survivalRange.put("inCondition", condition);
		survivalRange.put("hasSurvivalProperty", survivalProperties);

		LinkedHashMap<String, Object> point1 = new LinkedHashMap<>();
		point1.put("lat", 22.2132);
		point1.put("long", -4.31211);

		LinkedHashMap<String, Object> point2 = new LinkedHashMap<>();
		point2.put("lat", 29.12);
		point2.put("long", -2.31);

		LinkedHashMap<String, Object> point3 = new LinkedHashMap<>();
		point3.put("lat", 134.12);
		point3.put("long", 20.31);

		ArrayList<LinkedHashMap<String, Object>> coveragePoints = new ArrayList<>();
		coveragePoints.add(point1);
		coveragePoints.add(point2);
		coveragePoints.add(point3);

		LinkedHashMap<String, Object> coverage = new LinkedHashMap<>();
		coverage.put("type", "Circle");
		coverage.put("location", coveragePoints);

		ArrayList<LinkedHashMap<String, Object>> coveragePoints2 = new ArrayList<>();
		LinkedHashMap<String, Object> point21 = new LinkedHashMap<>();
		point21.put("lat", 9.2112);
		point21.put("long", 320.31);

		LinkedHashMap<String, Object> point22 = new LinkedHashMap<>();
		point2.put("lat", 62.12);
		point2.put("long", -22.31);

		LinkedHashMap<String, Object> point23 = new LinkedHashMap<>();
		point3.put("lat", 200.12);
		point3.put("long", 23.31);

		coveragePoints2.add(point21);
		coveragePoints2.add(point22);
		coveragePoints2.add(point23);

		LinkedHashMap<String, Object> coverage2 = new LinkedHashMap<>();
		coverage2.put("type", "Circle");
		coverage2.put("location", coveragePoints2);

		ArrayList<LinkedHashMap<String, Object>> coverageList = new ArrayList<>();
		coverageList.add(coverage);
		coverageList.add(coverage2);

		htblFieldValue.put("hasCoverage", coverageList);
		htblFieldValue.put("hasSurvivalRange", survivalRange);
		htblFieldValue.put("test", "2134-2313-242-33332");
		try {
			requestFieldsValidation.validateRequestFields("TESTAPPLICATION", htblFieldValue, new ActuatingDevice());
		} catch (ErrorObjException e) {
			System.out.println(e.getExceptionMessage());
		}
	}
}
