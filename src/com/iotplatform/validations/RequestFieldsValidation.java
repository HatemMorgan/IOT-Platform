package com.iotplatform.validations;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.daos.DynamicConceptDao;
import com.iotplatform.daos.MainDao;
import com.iotplatform.daos.ValidationDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.InvalidPropertyValuesException;
import com.iotplatform.exceptions.InvalidRequestBodyException;
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
import com.iotplatform.utilities.ValueOfTypeClass;

import oracle.spatial.rdf.client.jena.Oracle;

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
	private ValidationDao validationDao;

	@Autowired
	public RequestFieldsValidation(DynamicConceptDao dynamicConceptDao, ValidationDao validationDao) {
		this.dynamicConceptDao = dynamicConceptDao;
		this.validationDao = validationDao;
		init();
	}

	/*
	 * validateRequestFields method is takes Hashtable<String, Object>
	 * htblFieldValue which is the request body represented as key fieldName and
	 * object value and also it takes the subjectClass which is the specified
	 * class in the request url eg. Sensor,ActuatingDevice
	 * 
	 * This Method will return Hashtable<Class,
	 * ArrayList<ArrayList<PropertyValue>>> which contains a Class instance as
	 * key and a list of all instances of that class (instance is represented by
	 * an ArrayList<PropertyValue> which holds all the prefixed properties and
	 * their prefixed values)
	 * 
	 * This method is responsible to iterate on the request body and call
	 * parseAndConstructFieldValue recursive function to parse,breakdown objects
	 * and list , to prefixing property and its value
	 * 
	 * This method creates all need Data Structure instances and pass it by
	 * reference to avoid racing conditions when running server and handling
	 * multiple requests because by this way there will not be any shared
	 * attributes all the Data Structure instances has an internal scope to the
	 * method
	 * 
	 */
	public Hashtable<Class, ArrayList<ArrayList<PropertyValue>>> validateRequestFields(String applicationName,
			Hashtable<String, Object> htblFieldValue, Class subjectClass) {

		/*
		 * set subClass to its static instace
		 */

		subjectClass = htblAllStaticClasses.get(subjectClass.getUri());

		Iterator<String> htblFieldValueIterator = htblFieldValue.keySet().iterator();

		/*
		 * List of classes that need to get their dynamic properties to check if
		 * the fields maps to one of them or these fields are invalid fields
		 */
		Hashtable<String, Class> htblNotMappedFieldsClasses = new Hashtable<>();

		/*
		 * notFoundFieldValueList holds fieldsValue pairs that do not have a
		 * static property mapping and are waited to be checked for mapping
		 * after loading dynamic properties
		 */
		ArrayList<ValueOfFieldNotMappedToStaticProperty> notFoundFieldValueList = new ArrayList<>();

		/*
		 * htblClassPropertyValue holds the constructed propertyValue
		 */
		Hashtable<Class, ArrayList<ArrayList<PropertyValue>>> htblClassPropertyValue = new Hashtable<>();

		/*
		 * classValueList is list of ValueOfTypeClass instances (holds
		 * objectValue and its classType). it will be used to check
		 * dataIntegrity constraints
		 */
		ArrayList<ValueOfTypeClass> classValueList = new ArrayList<>();

		/*
		 * uniquePropValueList is a LikedHashMap of key prefixedClassName and
		 * value LinkedHashMap<String,ArrayList<PropertyValue>> with key
		 * prefixedPropertyName and value list of propertyValue object that
		 * holds the unique propertyName and value I used LinkedHashMap to
		 * ensure that the property will not be duplicated for the
		 * prefixedClassName (this will improve efficiency by reducing graph
		 * patterns as there will never be duplicated properties)
		 * 
		 * This DataStructure instance is used in uniqueConstraintValidation
		 */
		LinkedHashMap<String, LinkedHashMap<String, ArrayList<Object>>> htblUniquePropValueList = new LinkedHashMap<>();

		/*
		 * check if there is a fieldName= type which means that value of this
		 * field describes a type class then change the subClass type to be the
		 * subjectClass
		 * 
		 * Here I iterating on the main fields not breaking down object values
		 * or list values
		 * 
		 * calling parseAndConstructFieldValue will do the parsing and breaking
		 * down object values and list values plus making fieldValidation,
		 * dataIntegrityValidation and uniqueConstraintValidation and add
		 * prefixing property and value to be well mapped to application
		 * ontology to be used after that by DAOs to generate triples and insert
		 * data
		 */
		if (htblFieldValue.containsKey("type") && isobjectValueValidType(subjectClass, htblFieldValue.get("type"))) {
			Class subClassSubject = subjectClass.getClassTypesList().get(htblFieldValue.get("type").toString());
			subjectClass = subClassSubject;

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
			 * add property id for classTypeObject and add generated UUID as the
			 * object of it
			 */

			Property idProperty = subjectClass.getProperties().get("id");
			PropertyValue idPropertyValue = new PropertyValue(idProperty.getPrefix().getPrefix() + idProperty.getName(),
					id, false);

			/*
			 * add idPropertyValue object to htblClassPropertyValue
			 */

			ArrayList<ArrayList<PropertyValue>> instancesList = new ArrayList<>();
			ArrayList<PropertyValue> propertyValueList = new ArrayList<>();
			instancesList.add(propertyValueList);
			htblClassPropertyValue.put(subjectClass, instancesList);
			htblClassPropertyValue.get(subjectClass).get(0).add(idPropertyValue);

		} else {

			/*
			 * There is a uniqueIdentfier for this subjectClass that is received
			 * from the user eg. userName for DeveloperClass so add subjectClass
			 * and a new empty arraylist to hold its instances
			 */
			ArrayList<ArrayList<PropertyValue>> instancesList = new ArrayList<>();
			ArrayList<PropertyValue> propertyValueList = new ArrayList<>();
			instancesList.add(propertyValueList);
			htblClassPropertyValue.put(subjectClass, instancesList);

		}

		/*
		 * Iterate on htblFieldValue
		 */
		while (htblFieldValueIterator.hasNext()) {
			String fieldName = htblFieldValueIterator.next();
			Object value = htblFieldValue.get(fieldName);

			/*
			 * skip if the fieldName is type because type is not a property it
			 * only express a type(subClassName) of superClass
			 */
			if (fieldName.equals("type")) {
				continue;
			}

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

			if (isFieldMapsToStaticProperty(subjectClass, fieldName, value, htblNotMappedFieldsClasses,
					notFoundFieldValueList, 0)) {
				Property property = subjectClass.getProperties().get(fieldName);

				parseAndConstructFieldValue(subjectClass, property, value, htblClassPropertyValue,
						htblNotMappedFieldsClasses, notFoundFieldValueList, 0, htblUniquePropValueList, classValueList,
						subjectClass.getName());
			}

		}

		/*
		 * start htblPrevNotMappedFieldsClasses with an empty hashtable
		 */
		parseAndConstructNotMappedFieldsValues(applicationName, subjectClass, htblClassPropertyValue,
				htblNotMappedFieldsClasses, new Hashtable<>(), notFoundFieldValueList, htblUniquePropValueList,
				classValueList);

		/*
		 * Call ValidationDao to check for data constraint voilations
		 * (DataIntegerityConstraint and uniqueConstraint)
		 * 
		 */
		if (classValueList.size() > 0 || htblUniquePropValueList.size() > 0) {

			/*
			 * check if there are any constraints violations if there are any
			 * violations hasConstraintViolations method will throw the
			 * appropriate error that describes the type of the violation
			 * 
			 * if there is no constraints violations a boolean true will be
			 * returned
			 */
			if (validationDao.hasNoConstraintViolations(applicationName, classValueList, htblUniquePropValueList,
					subjectClass)) {
				return htblClassPropertyValue;
			}

		}

		// System.out.println("==============================================");
		// System.out.println(htblClassPropertyValue.toString());
		// System.out.println("=======================================");
		// System.out.println(classList.toString());
		// System.out.println("======================================");
		// System.out.println(htblNotFoundFieldValue.toString());
		// System.out.println("===============================================");
		// System.out.println(classValueList.toString());
		// System.out.println("===================================================");
		// System.out.println(htblUniquePropValueList.toString());
		// System.out.println("====================================================");
		// System.out.println(
		// validationDao.constructUniqueContstraintCheckSubQueryStr2(htblUniquePropValueList,
		// subjectClass));
		return htblClassPropertyValue;
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
	 * 
	 * uniqueIdentifer is a random generated id that is used in
	 * uniqueConstraintValidation as a reference to uniquePopertyValues of an
	 * instance
	 */
	private boolean isFieldMapsToStaticProperty(Class subjectClass, String fieldName, Object value,
			Hashtable<String, Class> htblNotMappedFieldsClasses,
			ArrayList<ValueOfFieldNotMappedToStaticProperty> notFoundFieldValueList, int index) {

		if (subjectClass.getProperties().containsKey(fieldName)) {
			return true;
		} else {
			htblNotMappedFieldsClasses.put(subjectClass.getUri(), subjectClass);
			ValueOfFieldNotMappedToStaticProperty notMappedFieldValue = new ValueOfFieldNotMappedToStaticProperty(
					subjectClass, value, index, fieldName);
			notFoundFieldValueList.add(notMappedFieldValue);
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
	 * 
	 * objectValueProperty holds the prefixed name of objectProperty that has a
	 * nested object value in order to try to link mainObject with subObject to
	 * use it when making unique constraint validation
	 * 
	 * uniqueIdentifier is the is the subjectClass uniqueIdentifer which
	 * represents the subjectClassInstance random generated id to be used in
	 * uniqueConstraintValidation. It is used to reference uniquePropertyValues
	 * of this subjectClassInstance to be used in uniqueConstraintValidation
	 * 
	 * objectValueUniqueIdentifier is the subjectClass uniqueIdentifer which
	 * represents the subjectClassInstance random generated id to be used in
	 * uniqueConstraintValidation. When the value is of type object
	 * objectValueUniqueIdentifier holds the randomID of the subjectInstance of
	 * this objectValue
	 * 
	 * notFoundFieldValueList is used to be passed to
	 * isFieldMapsToStaticProperty method in order to add the not mapped field
	 * to it
	 * 
	 * classValueList holds the objectValue of an objectProperty and its class
	 * type to be passed to validationDao after parsing and check types and
	 * fields to check if this objectValue is exist or not to maintain data
	 * integrity
	 * 
	 * htblUniquePropValueList holds prpoertyValue for uniqueProperties to be
	 * passed to validationDao after parsing and check types and fields to check
	 * if the value is unique or not
	 */
	private void parseAndConstructFieldValue(Class subjectClass, Property property, Object value,
			Hashtable<Class, ArrayList<ArrayList<PropertyValue>>> htblClassPropertyValue,
			Hashtable<String, Class> htblNotMappedFieldsClasses,
			ArrayList<ValueOfFieldNotMappedToStaticProperty> notFoundFieldValueList, int indexCount,
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<Object>>> htblUniquePropValueList,
			ArrayList<ValueOfTypeClass> classValueList, String requestClassName) {

		// System.out.println(subjectClass.getName() + " " + property.getName()
		// + " " + value.toString());

		/*
		 * check if the value is of type primitive datatype
		 */
		if ((value instanceof String) || (value instanceof Integer) || (value instanceof Float)
				|| (value instanceof Double) || (value instanceof Boolean)) {

			/*
			 * Object property so add it to htblClassValue to send it to
			 * requestValidationDao
			 */
			if (property instanceof ObjectProperty) {
				classValueList.add(new ValueOfTypeClass(((ObjectProperty) property).getObject(), value));

			} else {

				/*
				 * check if the datatype is correct or not
				 */
				if (!isDataValueValid((DataTypeProperty) property, value)) {

					throw new InvalidPropertyValuesException(requestClassName, property.getName());
				}

			}

			/*
			 * check if the property has unique constraint to add the value and
			 * the property to htblUniquePropValueList to be passed to
			 * validationDao to check the no unique constraint violation occured
			 */
			if (property.isUnique()) {
				String prefixedPropertySubjectClassName = property.getSubjectClass().getPrefix().getPrefix()
						+ property.getSubjectClass().getName();

				if (htblUniquePropValueList.containsKey(prefixedPropertySubjectClassName)) {

					/*
					 * check if property of the prefixedPropertySubjectClassName
					 * was added before
					 */
					if (htblUniquePropValueList.get(prefixedPropertySubjectClassName)
							.containsKey(property.getPrefix().getPrefix() + property.getName())) {
						/*
						 * add propertyValue instance to uniquePropertyValueList
						 * of subjectClassInstance
						 */
						htblUniquePropValueList.get(prefixedPropertySubjectClassName)
								.get(property.getPrefix().getPrefix() + property.getName()).add(value);
					} else {
						/*
						 * add prefixedPropertyName and a list to hold
						 * propertyValue objects
						 */

						ArrayList<Object> propertyValueList = new ArrayList<>();
						htblUniquePropValueList.get(prefixedPropertySubjectClassName)
								.put(property.getPrefix().getPrefix() + property.getName(), propertyValueList);

						/*
						 * add propertyValue instance to uniquePropertyValueList
						 * of subjectClassInstance
						 */
						htblUniquePropValueList.get(prefixedPropertySubjectClassName)
								.get(property.getPrefix().getPrefix() + property.getName()).add(value);
					}

				} else {

					/*
					 * Add subjectClass key and new
					 * htblClassInstanceUnqiuePropValueList to
					 * htblClassInstanceUnqiuePropValueList
					 */
					ArrayList<Object> propertyValueList = new ArrayList<>();
					LinkedHashMap<String, ArrayList<Object>> htblClassInstanceUnqiuePropValueList = new LinkedHashMap<>();
					htblClassInstanceUnqiuePropValueList.put(property.getPrefix().getPrefix() + property.getName(),
							propertyValueList);
					htblUniquePropValueList.put(prefixedPropertySubjectClassName, htblClassInstanceUnqiuePropValueList);

					/*
					 * add propertyValue instance to uniquePropertyValueList of
					 * subjectClassInstance
					 */
					htblUniquePropValueList.get(prefixedPropertySubjectClassName)
							.get(property.getPrefix().getPrefix() + property.getName()).add(value);
				}

			}
			/*
			 * check if the property is not the uniqueIdentifierProperty of this
			 * subjectClass in order to not pefixing value to be used by the
			 * MainDao to generate proper triples
			 */
			PropertyValue propertyValue;
			if (subjectClass.isHasUniqueIdentifierProperty()
					&& subjectClass.getUniqueIdentifierPropertyName().equals(property.getName())) {
				propertyValue = new PropertyValue(property.getPrefix().getPrefix() + property.getName(), value, false);
			} else {
				/*
				 * construct a new PropertyValue instance to hold the prefiexed
				 * propertyName and prefixed value
				 */
				propertyValue = new PropertyValue(property.getPrefix().getPrefix() + property.getName(),
						getValue(property, value), false);
			}

			/*
			 * add PropertyValue object to htblClassPropertyValue
			 */
			// int classInstanceIndex =
			// htblClassPropertyValue.get(subjectClass).size() - 1;
			htblClassPropertyValue.get(subjectClass).get(indexCount).add(propertyValue);

		} else {

			// =========================================================================================================
			// Object Value
			// =========================================================================================================

			/*
			 * value is a nested object so I will iterate on all the keyValue
			 * pairs and check if the fields are valid or not and reconstruct
			 * them
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
				 * linking subject class with object class by adding a the
				 * unique identifier as the object value of the property
				 * 
				 * if it has unique Identifier this means that a unique property
				 * value added by the user must be the unique identifier that
				 * will be the subject of object value and references the object
				 * value instance to the subject
				 */
				String objectUniqueIdentifier;

				if (classType.isHasUniqueIdentifierProperty()) {
					objectUniqueIdentifier = valueObject.get(classType.getUniqueIdentifierPropertyName()).toString();

				} else {
					/*
					 * There is no uniqueIdentfier for this object class so the
					 * platform has to generate a UUID to be the unique
					 * Identifier that will be the subject of object value and
					 * references the object value instance to the subject
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
				 * so I create a new PropertyValue instance to hold the
				 * property(objectProperty that has this objectValue) and the
				 * uniqueIdentifier value that represents a unique reference to
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

				htblClassPropertyValue.get(subjectClass).get(indexCount).add(propertyValue);

				/*
				 * check if there is a fieldName= type which means that value of
				 * this field describes a type class then change the subClass
				 * type to be the subjectClass
				 */

				if (valueObject.containsKey("type") && isobjectValueValidType(classType, valueObject.get("type"))) {
					Class subClassSubject = classType.getClassTypesList().get(valueObject.get("type").toString());
					classType = subClassSubject;

				} else {

					/*
					 * throw an error if the type field value is not a valid
					 * type
					 */
					if (valueObject.containsKey("type")
							&& !isobjectValueValidType(subjectClass, valueObject.get("type")))

						throw new InvalidTypeValidationException(requestClassName,
								classType.getClassTypesList().keySet(), classType.getName());

				}

				/*
				 * Check if the classType exist in htblClassPropertyValue.
				 * 
				 * If it exist, this means that a new class instance of this
				 * class has to be created and I have to increment the
				 * indexCount
				 */

				if (htblClassPropertyValue.containsKey(classType)) {
					ArrayList<PropertyValue> propertyValueList = new ArrayList<>();
					htblClassPropertyValue.get(classType).add(propertyValueList);
				} else {
					ArrayList<ArrayList<PropertyValue>> instancesList = new ArrayList<>();
					ArrayList<PropertyValue> propertyValueList = new ArrayList<>();
					instancesList.add(propertyValueList);
					htblClassPropertyValue.put(classType, instancesList);
				}

				/*
				 * add property id for classTypeObject and add generated UUID as
				 * the object of it. I have to check if the classType has no
				 * uniqueIdentifier which means that the system has to add an id
				 * property that hold the generated UUID
				 * 
				 * I created this here after typeValidation to add the
				 * idPropertyValue to the instance of the classType eg.(Circle)
				 */

				int classInstanceIndex = htblClassPropertyValue.get(classType).size() - 1;

				if (!classType.isHasUniqueIdentifierProperty()) {
					Property idProperty = classType.getProperties().get("id");
					PropertyValue idPropertyValue = new PropertyValue(
							idProperty.getPrefix().getPrefix() + idProperty.getName(), objectUniqueIdentifier, false);

					/*
					 * add idPropertyValue object to htblClassPropertyValue
					 * 
					 * I will always add a new property to the last
					 * instanceClass represented by an arraylist<PropertyValue>
					 * because I finish a single object then return back
					 * recursively to complete parsing other fields
					 * 
					 * any new propertyValue added will be for the same instance
					 * so it will be always exist in the end of the arraylist
					 * that represent instances of classType. Because I iterate
					 * on the fields of the new object instance so I will finish
					 * the current new instance of classType then complete the
					 * rest fields
					 * 
					 */

					htblClassPropertyValue.get(classType).get(classInstanceIndex).add(idPropertyValue);
				}

				for (String fieldName : valueObject.keySet()) {

					/*
					 * skip if the fieldName is type because type is not a
					 * property it only express a type(subClassName) of
					 * superClass
					 */
					if (fieldName.equals("type")) {
						continue;
					}

					/*
					 * if it returns true then the field is a valid field (it
					 * maps to a property in the properties list of passed
					 * classs)
					 * 
					 * if it return false means that no static mapping so it
					 * will add the subject class to classList and
					 * fieldNameValue pair to htblNotFoundFieldValue
					 */

					Object fieldValue = valueObject.get(fieldName);
					if (isFieldMapsToStaticProperty(classType, fieldName, fieldValue, htblNotMappedFieldsClasses,
							notFoundFieldValueList, classInstanceIndex)) {

						Property classTypeProperty = classType.getProperties().get(fieldName);

						/*
						 * if subjectClass(outer class instance that contains
						 * property with value instance that represents this
						 * classType instance) has an ObjectProperty then I have
						 * to pass subjectClass prefixedName and property to the
						 * recursive call inOrder to link any
						 * uniquePropertyValue in the nested ClassType instance
						 * with the property of its subjectClass to be used
						 * after that in unqiueConstraint validation
						 * 
						 * uniqueIdentifier is the subjectClass uniqueIdentifer
						 * which represents the subjectClassInstance(subject of
						 * this nested objectValue) random generated id to be
						 * used in uniqueConstraintValidation
						 * 
						 */
						if (property instanceof ObjectProperty) {
							parseAndConstructFieldValue(classType, classTypeProperty, fieldValue,
									htblClassPropertyValue, htblNotMappedFieldsClasses, notFoundFieldValueList,
									classInstanceIndex, htblUniquePropValueList, classValueList, requestClassName);
						} else {
							/*
							 * if property is DataTypeProperty then pass both
							 * with null because we do not need to keep track of
							 * previous linkage
							 * 
							 * it will rarely get here because already the value
							 * is an object value
							 */
							parseAndConstructFieldValue(classType, classTypeProperty, fieldValue,
									htblClassPropertyValue, htblNotMappedFieldsClasses, notFoundFieldValueList,
									classInstanceIndex, htblUniquePropValueList, classValueList, requestClassName);
						}
					}

				}
			} else {

				// =========================================================================================================
				// List Value
				// =========================================================================================================

				/*
				 * value is a list values (value may be datatype values or
				 * object values) so I will iterate on the list of values and
				 * recursively parse the values and make field validations
				 * 
				 * eg: hasSurvivalRange : [ { type: "SystemLifeTime" }, { type:
				 * "BatteryLifeTime" } ]
				 * 
				 * 
				 * or : mbox: ["hatem@gmail.com","jsid@yahoo.com"]
				 */
				if (value instanceof java.util.ArrayList) {

					/*
					 * check if the property has multiple values or not
					 */
					if (property.isMulitpleValues()) {
						ArrayList<Object> valueList = (ArrayList<Object>) value;

						/*
						 * iterate on the list and do a recursive call to parse
						 * and validate every single value in the valueList
						 */
						for (Object singleValue : valueList) {

							parseAndConstructFieldValue(subjectClass, property, singleValue, htblClassPropertyValue,
									htblNotMappedFieldsClasses, notFoundFieldValueList, indexCount,
									htblUniquePropValueList, classValueList, requestClassName);
						}
					} else {

						/*
						 * if the passed value is a list value and the property
						 * does not has multiple values then raise
						 * InvalidRequestBodyException
						 */
						throw new InvalidRequestBodyException(property.getName(), "field: " + property.getName()
								+ " does not have multiple values," + " it must have single value or nested object",
								requestClassName);
					}
				} else {

					/*
					 * if the value is not a datatype or an object or a list
					 * then raise InvalidRequestBodyException that tells the
					 * user that this value has an invalid format
					 */
					throw new InvalidRequestBodyException(property.getName(),
							"field: " + property.getName()
									+ "has an invalid format. Please Check the documentations for further information.",
							requestClassName);
				}
			}
		}
	}

	/*
	 * parseAndConstructNotMappedFieldsValues method is used to parse values of
	 * fields that has no static mapping and may have mapping after loading
	 * dynamic properties
	 */
	private void parseAndConstructNotMappedFieldsValues(String applicationName, Class subjectClass,
			Hashtable<Class, ArrayList<ArrayList<PropertyValue>>> htblClassPropertyValue,
			Hashtable<String, Class> htblNotMappedFieldsClasses,
			Hashtable<String, Class> htblPrevNotMappedFieldsClasses,
			ArrayList<ValueOfFieldNotMappedToStaticProperty> notFoundFieldValueList,
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<Object>>> htblUniquePropValueList,
			ArrayList<ValueOfTypeClass> classValueList) {
		// System.out.println("in parseAndConstructNotMappedFieldsValues
		// method");
		/*
		 * get Dynamic Properties of the classes in the classList which contains
		 * the domain class of the fields in the request that are not mapped to
		 * static properties
		 */

		if (htblNotMappedFieldsClasses.size() > 0) {
			Hashtable<String, DynamicConceptModel> loadedDynamicProperties = getDynamicProperties(applicationName,
					htblNotMappedFieldsClasses, htblPrevNotMappedFieldsClasses);

			/*
			 * Check that the fields that had no mappings are valid or not
			 * 
			 * this loop will loop for the size of notFoundFieldValueList that
			 * is coming, note that size of notFoundFieldValueList can be
			 * changed if there are fields that has mapping to dynamicProperties
			 * are objectProperties and the request coming has a nested object
			 * for this field so recursively I had to parse and construct it by
			 * calling parseAndConstructFieldValue method
			 * 
			 * This nested Object may have some fields that has no static
			 * mapping and need to checked after loading the dynamicProperties
			 * related to objectClassType and applicationDomain so I have to add
			 * this fields to notFoundFieldValueList and recursively call again
			 * this method
			 */
			int size = notFoundFieldValueList.size();
			for (int i = 0; i < size; i++) {
				ValueOfFieldNotMappedToStaticProperty notFoundFieldValue = notFoundFieldValueList.get(i);

				String field = notFoundFieldValue.getFieldName();

				// System.out.println("=====>>> " +
				// notFoundFieldValue.getFieldName() + " "
				// + notFoundFieldValue.getPropertyValue().toString());

				/*
				 * After loading dynamic Properties, I am caching all the loaded
				 * properties so If field does not mapped to one of the
				 * properties(contains static ones and cached dynamic ones) of
				 * the subjectClass , I will throw InvalidRequestFieldsException
				 * to indicate the field is invalid
				 */
				if (!notFoundFieldValue.getPropertyClass().getProperties().containsKey(field)) {
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

						if (property instanceof DataTypeProperty || (property instanceof ObjectProperty
								&& !(notFoundFieldValue.getPropertyValue() instanceof java.util.LinkedHashMap<?, ?>
										|| notFoundFieldValue.getPropertyValue() instanceof java.util.ArrayList))) {

							/*
							 * Field is valid dynamic property and has a mapping
							 * for a dynamic property. So I will parse the value
							 * and add prefixes
							 */
							parseAndConstructFieldValue(notFoundFieldValue.getPropertyClass(), property,
									notFoundFieldValue.getPropertyValue(), htblClassPropertyValue,
									htblNotMappedFieldsClasses, notFoundFieldValueList,
									notFoundFieldValue.getClassInstanceIndex(), htblUniquePropValueList, classValueList,
									subjectClass.getName());
						} else {
							/*
							 * if property is an object property so I have to
							 * pass to parseAndConstructFieldValue an new
							 * classList instance and a new
							 * notFoundFieldValueList instance in order to add
							 * any unMapped fields for the nestedObjectValues
							 * (if the value is object or list)
							 */

							/*
							 * reIntializing htblNotMappedFieldsClasses to hold
							 * any new classes that may have not mapped fields
							 * to load them also and add all clases in
							 * htblNotMappedFieldsClasses to
							 * htblPrevNotMappedFieldsClasses
							 */
							htblPrevNotMappedFieldsClasses.putAll(htblNotMappedFieldsClasses);
							htblNotMappedFieldsClasses = new Hashtable<>();

							/*
							 * reIntialize notFoundFieldValueList to hold
							 * unmapped field and value
							 */
							ArrayList<ValueOfFieldNotMappedToStaticProperty> tempNotFoundFieldValueList = new ArrayList<>();

							parseAndConstructFieldValue(notFoundFieldValue.getPropertyClass(), property,
									notFoundFieldValue.getPropertyValue(), htblClassPropertyValue,
									htblNotMappedFieldsClasses, tempNotFoundFieldValueList,
									notFoundFieldValue.getClassInstanceIndex(), htblUniquePropValueList, classValueList,
									subjectClass.getName());

							/*
							 * check if there was unmapped fields in the
							 * nestedObjectValue to load the dynamicProperties
							 * and check those fields by recursively calling
							 * this method again
							 */
							if (htblNotMappedFieldsClasses.size() > 0) {
								parseAndConstructNotMappedFieldsValues(applicationName, dynamicPropertyClass,
										htblClassPropertyValue, htblNotMappedFieldsClasses,
										htblPrevNotMappedFieldsClasses, tempNotFoundFieldValueList,
										htblUniquePropValueList, classValueList);
							}

						}
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
			return Prefixes.IOT_PLATFORM.getPrefix() + value.toString().toLowerCase().replaceAll(" ", "");
		}
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

		System.out.println("Connected to Database");

		RequestFieldsValidation requestFieldsValidation = new RequestFieldsValidation(dynamicConceptDao, validationDao);

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

		// LinkedHashMap<String, Object> condition = new LinkedHashMap<>();
		// condition.put("description", "High Tempreture Condition");
		//
		// LinkedHashMap<String, Object> batteryLifetimeAmount = new
		// LinkedHashMap<>();
		// batteryLifetimeAmount.put("hasDataValue", 20.21);
		//
		// LinkedHashMap<String, Object> systemLifetimeAmount = new
		// LinkedHashMap<>();
		// systemLifetimeAmount.put("hasDataValue", 200.21);
		//
		// LinkedHashMap<String, Object> batteryLifetime = new
		// LinkedHashMap<>();
		// batteryLifetime.put("type", "BatteryLifetime");
		// batteryLifetime.put("hasValue", batteryLifetimeAmount);
		//
		// LinkedHashMap<String, Object> systemLifetime = new LinkedHashMap<>();
		// systemLifetime.put("type", "SystemLifetime");
		// systemLifetime.put("hasValue", systemLifetimeAmount);
		//
		// ArrayList<LinkedHashMap<String, Object>> survivalProperties = new
		// ArrayList<>();
		// survivalProperties.add(systemLifetime);
		// survivalProperties.add(batteryLifetime);
		//
		// LinkedHashMap<String, Object> survivalRange = new LinkedHashMap<>();
		// survivalRange.put("inCondition", condition);
		// survivalRange.put("hasSurvivalProperty", survivalProperties);
		//
		// LinkedHashMap<String, Object> point1 = new LinkedHashMap<>();
		// point1.put("lat", 22.2132);
		// point1.put("long", -4.31211);
		//
		// LinkedHashMap<String, Object> point2 = new LinkedHashMap<>();
		// point2.put("lat", 29.12);
		// point2.put("long", -2.31);
		//
		// LinkedHashMap<String, Object> point3 = new LinkedHashMap<>();
		// point3.put("lat", 134.12);
		// point3.put("long", 20.31);
		//
		// ArrayList<LinkedHashMap<String, Object>> coveragePoints = new
		// ArrayList<>();
		// coveragePoints.add(point1);
		// coveragePoints.add(point2);
		// coveragePoints.add(point3);
		//
		// LinkedHashMap<String, Object> coverage = new LinkedHashMap<>();
		// coverage.put("type", "Circle");
		// coverage.put("location", point1);
		//
		// ArrayList<LinkedHashMap<String, Object>> coveragePoints2 = new
		// ArrayList<>();
		// LinkedHashMap<String, Object> point21 = new LinkedHashMap<>();
		// point21.put("lat", 9.2112);
		// point21.put("long", 320.31);
		//
		// LinkedHashMap<String, Object> point22 = new LinkedHashMap<>();
		// point2.put("lat", 62.12);
		// point2.put("long", -22.31);
		//
		// LinkedHashMap<String, Object> point23 = new LinkedHashMap<>();
		// point3.put("lat", 200.12);
		// point3.put("long", 23.31);
		//
		// coveragePoints2.add(point21);
		// coveragePoints2.add(point22);
		// coveragePoints2.add(point23);
		//
		// LinkedHashMap<String, Object> coverage2 = new LinkedHashMap<>();
		// coverage2.put("type", "Circle");
		// coverage2.put("location", coveragePoints2);
		//
		// ArrayList<LinkedHashMap<String, Object>> coverageList = new
		// ArrayList<>();
		// coverageList.add(coverage);
		// coverageList.add(coverage2);
		//
		// htblFieldValue.put("hasCoverage", coverage);
		// htblFieldValue.put("hasSurvivalRange", survivalRange);
		// htblFieldValue.put("test", "2134-2313-242-33332");

		LinkedHashMap<String, Object> hatemmorgan = new LinkedHashMap<>();

		hatemmorgan.put("type", "NormalUser");
		hatemmorgan.put("age", 20);
		hatemmorgan.put("firstName", "Hatem");
		hatemmorgan.put("middleName", "ELsayed");
		hatemmorgan.put("familyName", "Morgan");
		hatemmorgan.put("birthday", "27/7/1995");
		hatemmorgan.put("gender", "Male");
		hatemmorgan.put("title", "Engineer");
		hatemmorgan.put("userName", "HatemMorganss");

		ArrayList<Object> hatemmorganEmailList = new ArrayList<>();
		hatemmorganEmailList.add("hatemmorgan17ss@gmail.com");
		hatemmorganEmailList.add("hatem.el-sayedss@student.guc.edu.eg");

		hatemmorgan.put("mbox", hatemmorganEmailList);
		hatemmorgan.put("knows", "karammorgan");
		// hatemmorgan.put("job", "Computer Engineeer");

		LinkedHashMap<String, Object> ahmedmorgnan = new LinkedHashMap<>();

		ahmedmorgnan.put("type", "Developer");
		ahmedmorgnan.put("age", 16);
		ahmedmorgnan.put("firstName", "Ahmed");
		ahmedmorgnan.put("middleName", "ELsayed");
		ahmedmorgnan.put("familyName", "Morgan");
		ahmedmorgnan.put("birthday", "25/9/2000");
		ahmedmorgnan.put("gender", "Male");
		ahmedmorgnan.put("title", "Student");
		ahmedmorgnan.put("userName", "AhmedMorganls");

		ArrayList<Object> ahmedorganEmailList = new ArrayList<>();
		ahmedorganEmailList.add("ahmedmorganlss@gmail.com");

		ahmedmorgnan.put("mbox", ahmedorganEmailList);
		ahmedmorgnan.put("job", "High School Student");
		ahmedmorgnan.put("love", hatemmorgan);

		// Haytham Ismail
		htblFieldValue.put("age", 50);
		htblFieldValue.put("firstName", "Haytham");
		htblFieldValue.put("middleName", "Ismail");
		htblFieldValue.put("familyName", "Khalf");
		htblFieldValue.put("birthday", "27/7/1975");
		htblFieldValue.put("gender", "Male");
		htblFieldValue.put("title", "Professor");
		htblFieldValue.put("userName", "HaythamIsmailss");

		ArrayList<Object> emailList = new ArrayList<>();
		emailList.add("haytham.ismailss@gmail.com");
		emailList.add("haytham.ismailss@student.guc.edu.eg");

		htblFieldValue.put("mbox", emailList);

		htblFieldValue.put("developedApplication", "TESTAPPLICATION");
		// htblFieldValue.put("knows", ahmedmorgnan);
		htblFieldValue.put("hates", ahmedmorgnan);
		// ArrayList<LinkedHashMap<String, Object>> loveList = new
		// ArrayList<>();
		// loveList.add(hatemmorgan2);
		// loveList.add(hatemmorgan);
		// htblFieldValue.put("love", loveList);
		htblFieldValue.put("job", "Engineeer");

		try {
			long startTime = System.currentTimeMillis();
			// Hashtable<Class, ArrayList<ArrayList<PropertyValue>>>
			// htblClassPropertyValue =
			// requestFieldsValidation.validateRequestFields("TESTAPPLICATION",
			// htblFieldValue, new ActuatingDevice());
			Hashtable<Class, ArrayList<ArrayList<PropertyValue>>> htblClassPropertyValue = requestFieldsValidation
					.validateRequestFields("TESTAPPLICATION", htblFieldValue, new Developer());

			System.out.println("Time Taken: " + ((System.currentTimeMillis() - startTime) / 1000.0));

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

			System.out.println(requestFieldsValidation.htblAllStaticClasses.get("http://xmlns.com/foaf/0.1/Person")
					.getProperties());
			System.out.println(
					requestFieldsValidation.htblAllStaticClasses.get("http://iot-platform#Developer").getProperties());


		} catch (ErrorObjException e) {
			System.out.println(e.getExceptionMessage());
		}
	}
}
