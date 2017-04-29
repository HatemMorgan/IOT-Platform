package com.iotplatform.validations;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.UUID;

import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.daos.DynamicOntologyDao;
import com.iotplatform.daos.ValidationDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.InvalidPropertyValuesException;
import com.iotplatform.exceptions.InvalidRequestBodyException;
import com.iotplatform.exceptions.InvalidRequestFieldsException;
import com.iotplatform.exceptions.InvalidTypeValidationException;
import com.iotplatform.exceptions.InvalidUpdateRequestBodyException;
import com.iotplatform.exceptions.NotSuppliedObligatoryFieldsException;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.XSDDatatype;
import com.iotplatform.ontology.mapers.DynamicOntologyMapper;
import com.iotplatform.ontology.mapers.OntologyMapper;
import com.iotplatform.utilities.InsertionPropertyValue;
import com.iotplatform.utilities.NotMappedInsertRequestFieldUtility;
import com.iotplatform.utilities.ValueOfTypeClassUtility;

import oracle.spatial.rdf.client.jena.Oracle;

/**
 * 
 * InsertRequestValidation class is used to validate insert post request body
 * and parse it.
 * 
 * 1- It checks for Obligatory fields exist in the request body (fields that
 * must be exist for that class because not all fields are required to be exist)
 * 
 * 2- It checks that fields passed by the request are valid fields by checking
 * that they maps to existing properties in the passed subject class (which maps
 * the ontology classes). It also load dynamic properties or classes that was
 * created for the requested application domain, to check if the fields that
 * does not mapped to a property in the mainOntology that it maps to a dynamic
 * one
 * 
 * 3- it checks that there is no unique constraint or data integrity constrains
 * Violations
 * 
 * 4- It parse request body (JSON) into classes and properties in order to
 * perform a mapping from JSON to semantic web structure to be used by
 * InsertionQuery class to construct the insert query to insert data
 *
 * 
 * @author HatemMorgan
 *
 * 
 */

@Component
public class InsertRequestValidation {

	private ValidationDao validationDao;
	// private DynamicConceptsUtility dynamicPropertiesUtility;
	private DynamicOntologyDao dynamicOntologyDao;

	@Autowired
	public InsertRequestValidation(ValidationDao validationDao, DynamicOntologyDao dynamicOntologyDao) {
		this.validationDao = validationDao;
		this.dynamicOntologyDao = dynamicOntologyDao;
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
	public Hashtable<String, ArrayList<ArrayList<InsertionPropertyValue>>> validateRequestFields(
			LinkedHashMap<String, Object> htblFieldValue, Class subjectClass, String applicationModelName) {
		/*
		 * check if the obligatory fields exist. if does not exist throw an
		 * exception
		 */
		if (!areObligatoryFieldsExist(subjectClass, htblFieldValue)) {
			throw new NotSuppliedObligatoryFieldsException(subjectClass.getUniqueIdentifierPropertyName(),
					subjectClass.getName(), subjectClass.getName());
		}

		Iterator<String> htblFieldValueIterator = htblFieldValue.keySet().iterator();

		/*
		 * Hashtable of classes' name that need to get their dynamic properties
		 * to check if the fields maps to one of them or these fields are
		 * invalid fields
		 */
		Hashtable<String, String> htbNotMappedFieldsClasses = new Hashtable<>();

		/*
		 * notFoundFieldValueList holds fieldsValue pairs that do not have a
		 * static property mapping and are waited to be checked for mapping
		 * after loading dynamic properties
		 */
		ArrayList<NotMappedInsertRequestFieldUtility> notFoundFieldValueList = new ArrayList<>();

		/*
		 * htblClassPropertyValue holds the constructed propertyValue
		 */
		Hashtable<String, ArrayList<ArrayList<InsertionPropertyValue>>> htblClassPropertyValue = new Hashtable<>();

		/*
		 * classValueList is list of ValueOfTypeClass instances (holds
		 * objectValue and its classType). it will be used to check
		 * dataIntegrity constraints
		 */
		ArrayList<ValueOfTypeClassUtility> classValueList = new ArrayList<>();

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

		if (!subjectClass.isHasUniqueIdentifierProperty() && !htblFieldValue.containsKey("id")) {

			String id = UUID.randomUUID().toString();

			/*
			 * add property id for classTypeObject and add generated UUID as the
			 * object of it
			 */

			Property idProperty = subjectClass.getProperties().get("id");
			InsertionPropertyValue idPropertyValue = new InsertionPropertyValue(
					idProperty.getPrefix().getPrefix() + idProperty.getName(), id, false);

			/*
			 * add idPropertyValue object to htblClassPropertyValue
			 */

			ArrayList<ArrayList<InsertionPropertyValue>> instancesList = new ArrayList<>();
			ArrayList<InsertionPropertyValue> propertyValueList = new ArrayList<>();
			instancesList.add(propertyValueList);
			htblClassPropertyValue.put(subjectClass.getName(), instancesList);
			htblClassPropertyValue.get(subjectClass.getName()).get(0).add(idPropertyValue);

		} else {

			/*
			 * There is a uniqueIdentfier for this subjectClass that is received
			 * from the user eg. userName for DeveloperClass so add subjectClass
			 * and a new empty arraylist to hold its instances
			 */
			ArrayList<ArrayList<InsertionPropertyValue>> instancesList = new ArrayList<>();
			ArrayList<InsertionPropertyValue> propertyValueList = new ArrayList<>();
			instancesList.add(propertyValueList);
			htblClassPropertyValue.put(subjectClass.getName(), instancesList);

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

			if (isFieldMapsToStaticProperty(subjectClass, fieldName, value, htbNotMappedFieldsClasses,
					notFoundFieldValueList, 0)) {
				Property property = subjectClass.getProperties().get(fieldName);

				parseAndConstructFieldValue(subjectClass, property, value, htblClassPropertyValue,
						htbNotMappedFieldsClasses, notFoundFieldValueList, 0, htblUniquePropValueList, classValueList,
						subjectClass.getName(), applicationModelName);
			}

		}

		parseAndConstructNotMappedFieldsValues(subjectClass.getName(), htblClassPropertyValue,
				htbNotMappedFieldsClasses, notFoundFieldValueList, htblUniquePropValueList, classValueList,
				applicationModelName);

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
			if (validationDao.hasNoConstraintViolations(applicationModelName, classValueList, htblUniquePropValueList,
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

	public Hashtable<String, ArrayList<ArrayList<InsertionPropertyValue>>> validateRequestFields(
			LinkedHashMap<String, Object> htblFieldValue, Class subjectClass, String applicationModelName,
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<Object>>> htblUniquePropValueList,
			ArrayList<ValueOfTypeClassUtility> classValueList) {

		Iterator<String> htblFieldValueIterator = htblFieldValue.keySet().iterator();

		/*
		 * Hashtable of classes' name that need to get their dynamic properties
		 * to check if the fields maps to one of them or these fields are
		 * invalid fields
		 */
		Hashtable<String, String> htbNotMappedFieldsClasses = new Hashtable<>();

		/*
		 * notFoundFieldValueList holds fieldsValue pairs that do not have a
		 * static property mapping and are waited to be checked for mapping
		 * after loading dynamic properties
		 */
		ArrayList<NotMappedInsertRequestFieldUtility> notFoundFieldValueList = new ArrayList<>();

		/*
		 * htblClassPropertyValue holds the constructed propertyValue
		 */
		Hashtable<String, ArrayList<ArrayList<InsertionPropertyValue>>> htblClassPropertyValue = new Hashtable<>();

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
		 * check that the user is not trying to insert a new uniqueIdentifer
		 * value
		 */
		if (subjectClass.isHasUniqueIdentifierProperty()
				&& htblFieldValue.containsKey(subjectClass.getUniqueIdentifierPropertyName())) {
			throw new InvalidUpdateRequestBodyException(
					"Invalid Update requet body. you cannot insert a new value for field: "
							+ subjectClass.getUniqueIdentifierPropertyName() + ". because "
							+ "this field is a unique one which only has only one unique "
							+ "value. If you want to change its value you have to add it "
							+ "to update part in the update request body. ");
		} else {

			/*
			 * check that the user is not inserting a new id to an individual of
			 * type class that has not uniqueIdnetifer and the system is
			 * generating a UUID for it
			 */
			if (!subjectClass.isHasUniqueIdentifierProperty() && htblFieldValue.containsKey("id")) {
				throw new InvalidUpdateRequestBodyException(
						"Invalid Update requet body. you cannot insert a new value for field: " + " id . because "
								+ "this field is a unique one which only has only one unique "
								+ "value. If you want to change its value you have to add it "
								+ "to update part in the update request body (not recommended ). ");
			}

		}

		ArrayList<ArrayList<InsertionPropertyValue>> instancesList = new ArrayList<>();
		ArrayList<InsertionPropertyValue> propertyValueList = new ArrayList<>();
		instancesList.add(propertyValueList);
		htblClassPropertyValue.put(subjectClass.getName(), instancesList);

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

			if (isFieldMapsToStaticProperty(subjectClass, fieldName, value, htbNotMappedFieldsClasses,
					notFoundFieldValueList, 0)) {
				Property property = subjectClass.getProperties().get(fieldName);

				parseAndConstructFieldValue(subjectClass, property, value, htblClassPropertyValue,
						htbNotMappedFieldsClasses, notFoundFieldValueList, 0, htblUniquePropValueList, classValueList,
						subjectClass.getName(), applicationModelName);
			}

		}

		parseAndConstructNotMappedFieldsValues(subjectClass.getName(), htblClassPropertyValue,
				htbNotMappedFieldsClasses, notFoundFieldValueList, htblUniquePropValueList, classValueList,
				applicationModelName);
		System.out.println(classValueList);
		System.out.println(htblUniquePropValueList);
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
			if (validationDao.hasNoConstraintViolations(applicationModelName, classValueList, htblUniquePropValueList,
					subjectClass)) {
				return htblClassPropertyValue;
			}

		}

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
			Hashtable<String, String> htbNotMappedFieldsClasses,
			ArrayList<NotMappedInsertRequestFieldUtility> notFoundFieldValueList, int index) {

		if (subjectClass.getProperties().containsKey(fieldName)) {
			return true;
		} else {

			// System.out.println(subjectClass.getName() + " " + fieldName);

			htbNotMappedFieldsClasses.put(subjectClass.getName(), subjectClass.getName());
			NotMappedInsertRequestFieldUtility notMappedFieldValue = new NotMappedInsertRequestFieldUtility(
					subjectClass, value, index, fieldName);
			notFoundFieldValueList.add(notMappedFieldValue);

			for (Class superClass : subjectClass.getSuperClassesList()) {
				htbNotMappedFieldsClasses.put(superClass.getName(), subjectClass.getName());

			}

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
			Hashtable<String, ArrayList<ArrayList<InsertionPropertyValue>>> htblClassPropertyValue,
			Hashtable<String, String> htbNotMappedFieldsClasses,
			ArrayList<NotMappedInsertRequestFieldUtility> notFoundFieldValueList, int indexCount,
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<Object>>> htblUniquePropValueList,
			ArrayList<ValueOfTypeClassUtility> classValueList, String requestClassName, String applicationModelName) {

		/*
		 * check if the value is of type primitive datatype
		 */
		if ((value instanceof String) || (value instanceof Integer) || (value instanceof Float)
				|| (value instanceof Double) || (value instanceof Boolean)) {

			if (value.toString().isEmpty() || value.toString().replaceAll(" ", "").isEmpty()) {
				throw new InvalidRequestBodyException(property.getName(), "its value must not be empty",
						requestClassName);
			}

			/*
			 * Object property so add it to htblClassValue to send it to
			 * requestValidationDao
			 */
			if (property instanceof ObjectProperty) {

				/*
				 * get property range objectClass if it is an objectProperty
				 */
				Class objectClass = null;

				String objectClassName = ((ObjectProperty) property).getObjectClassName();

				/*
				 * get objectClass from mainOntology if it exist
				 */
				if (OntologyMapper.getHtblMainOntologyClassesMappers().containsKey(objectClassName.toLowerCase())) {

					/*
					 * get the objectClass from MainOntologyClassesMapper
					 */
					objectClass = OntologyMapper.getHtblMainOntologyClassesMappers().get(objectClassName.toLowerCase());
				} else {

					if ((DynamicOntologyMapper.getHtblappDynamicOntologyClasses().contains(applicationModelName)
							&& DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
									.containsKey(objectClassName.toLowerCase()))) {
						objectClass = DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
								.get(objectClassName.toLowerCase());
					} else {
						htbNotMappedFieldsClasses.put(objectClassName, objectClassName);

						NotMappedInsertRequestFieldUtility notMappedFieldValue = new NotMappedInsertRequestFieldUtility(
								subjectClass, value, indexCount, property.getName());
						notFoundFieldValueList.add(notMappedFieldValue);

					}

				}

				if (objectClass != null) {
					classValueList.add(new ValueOfTypeClassUtility(objectClass, value));
				} else {
					return;
				}

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
			InsertionPropertyValue propertyValue;
			if (subjectClass.isHasUniqueIdentifierProperty()
					&& subjectClass.getUniqueIdentifierPropertyName().equals(property.getName())) {
				propertyValue = new InsertionPropertyValue(property.getPrefix().getPrefix() + property.getName(), value,
						false);
			} else {
				/*
				 * construct a new PropertyValue instance to hold the prefiexed
				 * propertyName and prefixed value
				 */
				propertyValue = new InsertionPropertyValue(property.getPrefix().getPrefix() + property.getName(),
						getValue(property, value), false);
			}

			/*
			 * check if the property is not multiValued (has only one value) in
			 * order to set isPropertyHasSingleValue of propertyValue to true
			 */
			if (!property.isMulitpleValues()) {
				propertyValue.setPropertyHasSingleValue(true);
			}

			/*
			 * add PropertyValue object to htblClassPropertyValue
			 */
			htblClassPropertyValue.get(subjectClass.getName()).get(indexCount).add(propertyValue);

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

				if (valueObject.isEmpty()) {
					throw new InvalidRequestBodyException(property.getName(), "its value must not be an empty object",
							requestClassName);

				}

				/*
				 * true if the type Class of objectProperty is from MainOntology
				 */
				boolean mainOntologyClassType = false;

				/*
				 * get Object property range classType
				 */
				Class classType = null;

				String objectClassName = ((ObjectProperty) property).getObjectClassName();

				/*
				 * get objectClass from dynamicOntology cache if it exist
				 */
				if ((DynamicOntologyMapper.getHtblappDynamicOntologyClasses().containsKey(applicationModelName)
						&& DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
								.containsKey(objectClassName.toLowerCase()))) {
					classType = DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
							.get(objectClassName.toLowerCase());

				} else {
					if (OntologyMapper.getHtblMainOntologyClassesMappers().containsKey(objectClassName.toLowerCase())) {

						/*
						 * get the objectClass from MainOntologyClassesMapper
						 */
						classType = OntologyMapper.getHtblMainOntologyClassesMappers()
								.get(objectClassName.toLowerCase());

						mainOntologyClassType = true;
					} else {
						htbNotMappedFieldsClasses.put(objectClassName, objectClassName);

						NotMappedInsertRequestFieldUtility notMappedFieldValue = new NotMappedInsertRequestFieldUtility(
								subjectClass, value, indexCount, property.getName());
						notFoundFieldValueList.add(notMappedFieldValue);

					}

				}

				if (classType != null) {

					/*
					 * check if there is a fieldName= type which means that
					 * value of this field describes a type class then change
					 * the subClass type to be the subjectClass
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
					 * check if the obligatory fields exist. if does not exist
					 * throw an exception
					 */
					if (!areObligatoryFieldsExist(classType, valueObject)) {
						throw new NotSuppliedObligatoryFieldsException(classType.getUniqueIdentifierPropertyName(),
								classType.getName(), requestClassName);
					}

					/*
					 * linking subject class with object class by adding a the
					 * unique identifier as the object value of the property
					 * 
					 * if it has unique Identifier this means that a unique
					 * property value added by the user must be the unique
					 * identifier that will be the subject of object value and
					 * references the object value instance to the subject
					 */
					String objectUniqueIdentifier;

					if (classType.isHasUniqueIdentifierProperty()) {
						objectUniqueIdentifier = valueObject.get(classType.getUniqueIdentifierPropertyName())
								.toString();

					} else {
						/*
						 * There is no uniqueIdentfier for this object class so
						 * the platform has to generate a UUID to be the unique
						 * Identifier that will be the subject of object value
						 * and references the object value instance to the
						 * subject
						 */

						objectUniqueIdentifier = UUID.randomUUID().toString();

					}

					/*
					 * construct a new PropertyValue instance to hold the
					 * prefiexed propertyName and prefixed value
					 * 
					 * Here I am linking the subjectClass with a reference
					 * (uniqueIdentifier) of the new class instance to construct
					 * a proper graph nodes with relationships
					 * 
					 * so I create a new PropertyValue instance to hold the
					 * property(objectProperty that has this objectValue) and
					 * the uniqueIdentifier value that represents a unique
					 * reference to objectValue
					 * 
					 */
					InsertionPropertyValue propertyValue = new InsertionPropertyValue(
							property.getPrefix().getPrefix() + property.getName(),
							getValue(property, objectUniqueIdentifier), true);

					/*
					 * check if the property is not multiValued (has only one
					 * value) in order to set isPropertyHasSingleValue of
					 * propertyValue to true
					 */
					if (!property.isMulitpleValues()) {
						propertyValue.setPropertyHasSingleValue(true);
					}

					/*
					 * add PropertyValue object to htblClassPropertyValue
					 * 
					 * indexCount represents the index of the
					 * subjectClassInstance
					 */

					htblClassPropertyValue.get(subjectClass.getName()).get(indexCount).add(propertyValue);

					/*
					 * Check if the classType exist in htblClassPropertyValue.
					 * 
					 * If it exist, this means that a new class instance of this
					 * class has to be created and I have to increment the
					 * indexCount
					 */

					if (htblClassPropertyValue.containsKey(classType.getName())) {
						ArrayList<InsertionPropertyValue> propertyValueList = new ArrayList<>();
						htblClassPropertyValue.get(classType.getName()).add(propertyValueList);
					} else {
						ArrayList<ArrayList<InsertionPropertyValue>> instancesList = new ArrayList<>();
						ArrayList<InsertionPropertyValue> propertyValueList = new ArrayList<>();
						instancesList.add(propertyValueList);
						htblClassPropertyValue.put(classType.getName(), instancesList);
					}

					/*
					 * add property id for classTypeObject and add generated
					 * UUID as the object of it. I have to check if the
					 * classType has no uniqueIdentifier which means that the
					 * system has to add an id property that hold the generated
					 * UUID
					 * 
					 * I created this here after typeValidation to add the
					 * idPropertyValue to the instance of the classType
					 * eg.(Circle)
					 */

					int classInstanceIndex = htblClassPropertyValue.get(classType.getName()).size() - 1;

					if (!classType.isHasUniqueIdentifierProperty() && !valueObject.containsKey("id")) {
						Property idProperty = classType.getProperties().get("id");
						InsertionPropertyValue idPropertyValue = new InsertionPropertyValue(
								idProperty.getPrefix().getPrefix() + idProperty.getName(), objectUniqueIdentifier,
								false);

						/*
						 * add idPropertyValue object to htblClassPropertyValue
						 * 
						 * I will always add a new property to the last
						 * instanceClass represented by an
						 * arraylist<PropertyValue> because I finish a single
						 * object then return back recursively to complete
						 * parsing other fields
						 * 
						 * any new propertyValue added will be for the same
						 * instance so it will be always exist in the end of the
						 * arraylist that represent instances of classType.
						 * Because I iterate on the fields of the new object
						 * instance so I will finish the current new instance of
						 * classType then complete the rest fields
						 * 
						 */

						htblClassPropertyValue.get(classType.getName()).get(classInstanceIndex).add(idPropertyValue);
					}

					for (String fieldName : valueObject.keySet()) {

						/*
						 * skip if the fieldName is type because type is not a
						 * property it only express a type(subClassName) of
						 * superClass and I checked and used it before if it
						 * exist
						 */
						if (fieldName.equals("type")) {
							continue;
						}

						if (mainOntologyClassType) {
							if (!classType.getProperties().containsKey(fieldName)
									&& DynamicOntologyMapper.getHtblappDynamicOntologyClasses()
											.containsKey(applicationModelName)
									&& DynamicOntologyMapper.getHtblappDynamicOntologyClasses()
											.get(applicationModelName).containsKey(classType.getName().toLowerCase())) {

								classType = DynamicOntologyMapper.getHtblappDynamicOntologyClasses()
										.get(applicationModelName).get(classType.getName().toLowerCase());
							}
						}

						/*
						 * if it returns true then the field is a valid field
						 * (it maps to a property in the properties list of
						 * passed classs)
						 * 
						 * if it return false means that no static mapping so it
						 * will add the subject class to classList and
						 * fieldNameValue pair to htblNotFoundFieldValue
						 */
						Object fieldValue = valueObject.get(fieldName);
						if (isFieldMapsToStaticProperty(classType, fieldName, fieldValue, htbNotMappedFieldsClasses,
								notFoundFieldValueList, classInstanceIndex)) {

							Property classTypeProperty = classType.getProperties().get(fieldName);

							/*
							 * if subjectClass(outer class instance that
							 * contains property with value instance that
							 * represents this classType instance) has an
							 * ObjectProperty then I have to pass subjectClass
							 * prefixedName and property to the recursive call
							 * inOrder to link any uniquePropertyValue in the
							 * nested ClassType instance with the property of
							 * its subjectClass to be used after that in
							 * unqiueConstraint validation
							 * 
							 * uniqueIdentifier is the subjectClass
							 * uniqueIdentifer which represents the
							 * subjectClassInstance(subject of this nested
							 * objectValue) random generated id to be used in
							 * uniqueConstraintValidation
							 * 
							 */
							if (property instanceof ObjectProperty) {
								parseAndConstructFieldValue(classType, classTypeProperty, fieldValue,
										htblClassPropertyValue, htbNotMappedFieldsClasses, notFoundFieldValueList,
										classInstanceIndex, htblUniquePropValueList, classValueList, requestClassName,
										applicationModelName);
							} else {
								/*
								 * if property is DataTypeProperty then pass
								 * both with null because we do not need to keep
								 * track of previous linkage
								 * 
								 * it will rarely get here because already the
								 * value is an object value
								 */
								parseAndConstructFieldValue(classType, classTypeProperty, fieldValue,
										htblClassPropertyValue, htbNotMappedFieldsClasses, notFoundFieldValueList,
										classInstanceIndex, htblUniquePropValueList, classValueList, requestClassName,
										applicationModelName);
							}
						}

					}
				} else {
					return;
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

						if (valueList.isEmpty()) {
							throw new InvalidRequestBodyException(property.getName(),
									"its value must not be an empty array", requestClassName);

						}

						/*
						 * iterate on the list and do a recursive call to parse
						 * and validate every single value in the valueList
						 */
						for (Object singleValue : valueList) {

							parseAndConstructFieldValue(subjectClass, property, singleValue, htblClassPropertyValue,
									htbNotMappedFieldsClasses, notFoundFieldValueList, indexCount,
									htblUniquePropValueList, classValueList, requestClassName, applicationModelName);
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

	private void parseAndConstructNotMappedFieldsValues(String requestClassName,
			Hashtable<String, ArrayList<ArrayList<InsertionPropertyValue>>> htblClassPropertyValue,
			Hashtable<String, String> htbNotMappedFieldsClasses,
			ArrayList<NotMappedInsertRequestFieldUtility> notFoundFieldValueList,
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<Object>>> htblUniquePropValueList,
			ArrayList<ValueOfTypeClassUtility> classValueList, String applicationModelName) {

		if (notFoundFieldValueList.size() > 0 && htbNotMappedFieldsClasses.size() > 0) {
			dynamicOntologyDao.loadAndCacheDynamicClassesofApplicationDomain(applicationModelName,
					htbNotMappedFieldsClasses);

			/*
			 * construct new data structures to hold new unmapped fields
			 */
			Hashtable<String, String> htblnewNotMappedFieldsClasses = new Hashtable<>();
			ArrayList<NotMappedInsertRequestFieldUtility> newNotFoundFieldValueList = new ArrayList<>();

			for (NotMappedInsertRequestFieldUtility notFoundFieldValue : notFoundFieldValueList) {

				String field = notFoundFieldValue.getFieldName();
				String subjectClassName = notFoundFieldValue.getPropertyClass().getName().toLowerCase();

				/*
				 * After loading dynamic Properties, I am caching all the loaded
				 * properties so If field does not mapped to one of the
				 * properties(contains static ones and cached dynamic ones) of
				 * the subjectClass , I will throw InvalidRequestFieldsException
				 * to indicate the field is invalid
				 */
				if (!(DynamicOntologyMapper.getHtblappDynamicOntologyClasses().containsKey(applicationModelName)
						&& DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
								.containsKey(subjectClassName)
						&& DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
								.get(subjectClassName).getProperties().containsKey(field))) {

					throw new InvalidRequestFieldsException(requestClassName, field);
				} else {

					Property property = DynamicOntologyMapper.getHtblappDynamicOntologyClasses()
							.get(applicationModelName).get(subjectClassName).getProperties().get(field);
					Class subjectClass = notFoundFieldValue.getPropertyClass();
					int index = notFoundFieldValue.getClassInstanceIndex();
					Object propertyValue = notFoundFieldValue.getPropertyValue();

					parseAndConstructFieldValue(subjectClass, property, propertyValue, htblClassPropertyValue,
							htblnewNotMappedFieldsClasses, newNotFoundFieldValueList, index, htblUniquePropValueList,
							classValueList, requestClassName, applicationModelName);
				}

			}

			if (newNotFoundFieldValueList.size() > 0 && htblnewNotMappedFieldsClasses.size() > 0) {
				parseAndConstructNotMappedFieldsValues(requestClassName, htblClassPropertyValue,
						htblnewNotMappedFieldsClasses, newNotFoundFieldValueList, htblUniquePropValueList,
						classValueList, applicationModelName);
			}

		}

	}

	/*
	 * parseAndConstructNotMappedFieldsValues method is used to parse values of
	 * fields that has no static mapping and may have mapping after loading
	 * dynamic properties
	 */
	// private void parseAndConstructNotMappedFieldsValues(String
	// applicationName, Class subjectClass,
	// Hashtable<Class, ArrayList<ArrayList<InsertionPropertyValue>>>
	// htblClassPropertyValue,
	// ArrayList<String> notMappedFieldsClassesList, Hashtable<String, Class>
	// htblPrevNotMappedFieldsClasses,
	// ArrayList<ValueOfFieldNotMappedToStaticProperty> notFoundFieldValueList,
	// LinkedHashMap<String, LinkedHashMap<String, ArrayList<Object>>>
	// htblUniquePropValueList,
	// ArrayList<ValueOfTypeClass> classValueList, String applicationModelName)
	// {
	//
	// /*
	// * get Dynamic Properties of the classes in the classList which contains
	// * the domain class of the fields in the request that are not mapped to
	// * static properties
	// */
	// if (notFoundFieldValueList.size() > 0) {
	// // Hashtable<String, DynamicConceptModel> loadedDynamicProperties =
	// // dynamicPropertiesUtility
	// // .getDynamicProperties(applicationName,
	// // htblNotMappedFieldsClasses, htblPrevNotMappedFieldsClasses);
	//
	// /*
	// * Check that the fields that had no mappings are valid or not
	// *
	// * this loop will loop for the size of notFoundFieldValueList that
	// * is coming, note that size of notFoundFieldValueList can be
	// * changed if there are fields that has mapping to dynamicProperties
	// * are objectProperties and the request coming has a nested object
	// * for this field so recursively I had to parse and construct it by
	// * calling parseAndConstructFieldValue method
	// *
	// * This nested Object may have some fields that has no static
	// * mapping and need to checked after loading the dynamicProperties
	// * related to objectClassType and applicationDomain so I have to add
	// * this fields to notFoundFieldValueList and recursively call again
	// * this method
	// */
	// int size = notFoundFieldValueList.size();
	// for (int i = 0; i < size; i++) {
	// ValueOfFieldNotMappedToStaticProperty notFoundFieldValue =
	// notFoundFieldValueList.get(i);
	//
	// String field = notFoundFieldValue.getFieldName();
	//
	// /*
	// * After loading dynamic Properties, I am caching all the loaded
	// * properties so If field does not mapped to one of the
	// * properties(contains static ones and cached dynamic ones) of
	// * the subjectClass , I will throw InvalidRequestFieldsException
	// * to indicate the field is invalid
	// */
	// if
	// (!notFoundFieldValue.getPropertyClass().getProperties().containsKey(field))
	// {
	// throw new InvalidRequestFieldsException(subjectClass.getName(), field);
	// } else {
	//
	// /*
	// * passed field is a static property so add it to
	// * htblStaticProperty so check that the property is valid
	// * for this application domain
	// *
	// * if the applicationName is null so this field maps a
	// * property in the main ontology .
	// *
	// * if the applicationName is equal to passed applicationName
	// * so it is a dynamic added property to this application
	// * domain
	// *
	// * else it will be a dynamic property in another application
	// * domain which will happen rarely
	// */
	// Class dynamicPropertyClass =
	// OntologyMapper.getHtblMainOntologyClassesUriMappers()
	// .get(loadedDynamicProperties.get(field).getClass_uri());
	// Property property = dynamicPropertyClass.getProperties().get(field);
	//
	// if (property.getApplicationName() == null
	// || property.getApplicationName().equals(applicationName.replace(" ",
	// "").toUpperCase())) {
	//
	// if (property instanceof DataTypeProperty || (property instanceof
	// ObjectProperty
	// && !(notFoundFieldValue.getPropertyValue() instanceof
	// java.util.LinkedHashMap<?, ?>
	// || notFoundFieldValue.getPropertyValue() instanceof
	// java.util.ArrayList))) {
	//
	// /*
	// * Field is valid dynamic property and has a mapping
	// * for a dynamic property. So I will parse the value
	// * and add prefixes
	// */
	// parseAndConstructFieldValue(notFoundFieldValue.getPropertyClass(),
	// property,
	// notFoundFieldValue.getPropertyValue(), htblClassPropertyValue,
	// notMappedFieldsClassesList, notFoundFieldValueList,
	// notFoundFieldValue.getClassInstanceIndex(), htblUniquePropValueList,
	// classValueList,
	// subjectClass.getName(), applicationModelName);
	// } else {
	//
	// /*
	// * if property is an object property so I have to
	// * pass to parseAndConstructFieldValue an new
	// * classList instance and a new
	// * notFoundFieldValueList instance in order to add
	// * any unMapped fields for the nestedObjectValues
	// * (if the value is object or list)
	// *
	// *
	// *
	// * reIntializing htblNotMappedFieldsClasses to hold
	// * any new classes that may have not mapped fields
	// * to load them also and add all clases in
	// * htblNotMappedFieldsClasses to
	// * htblPrevNotMappedFieldsClasses
	// */
	// htblPrevNotMappedFieldsClasses.putAll(htblNotMappedFieldsClasses);
	// htblNotMappedFieldsClasses = new Hashtable<>();
	//
	// /*
	// * reIntialize notFoundFieldValueList to hold
	// * unmapped field and value
	// */
	// ArrayList<ValueOfFieldNotMappedToStaticProperty>
	// tempNotFoundFieldValueList = new ArrayList<>();
	//
	// parseAndConstructFieldValue(notFoundFieldValue.getPropertyClass(),
	// property,
	// notFoundFieldValue.getPropertyValue(), htblClassPropertyValue,
	// notMappedFieldsClassesList, tempNotFoundFieldValueList,
	// notFoundFieldValue.getClassInstanceIndex(), htblUniquePropValueList,
	// classValueList,
	// subjectClass.getName(), applicationModelName);
	//
	// /*
	// * check if there was unmapped fields in the
	// * nestedObjectValue to load the dynamicProperties
	// * and check those fields by recursively calling
	// * this method again
	// */
	// if (notMappedFieldsClassesList.size() > 0) {
	// parseAndConstructNotMappedFieldsValues(applicationName,
	// dynamicPropertyClass,
	// htblClassPropertyValue, notMappedFieldsClassesList,
	// htblPrevNotMappedFieldsClasses, tempNotFoundFieldValueList,
	// htblUniquePropValueList, classValueList, applicationModelName);
	// }
	//
	// }
	// } else {
	//
	// /*
	// * this means that this class has a property with the
	// * same name but it is not for the specified application
	// * domain
	// */
	//
	// throw new InvalidRequestFieldsException(subjectClass.getName(), field);
	//
	// }
	// }
	//
	// }
	// }
	//
	// }

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
			XSDDatatype xsdDataType = ((DataTypeProperty) property).getDataType();
			value = "\"" + value.toString() + "\"" + xsdDataType.getXsdType();
			return value;
		} else {
			return Prefix.IOT_PLATFORM.getPrefix() + value.toString().toLowerCase().replaceAll(" ", "");
		}
	}

	/*
	 * isDataValueValid checks that the datatype of the values passed with the
	 * property are valid to maintain data integrity and consistency.
	 * 
	 */
	private boolean isDataValueValid(DataTypeProperty dataProperty, Object value) {
		XSDDatatype xsdDataType = dataProperty.getDataType();
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

	/**
	 * areObligatoryFieldsExist is used to check if the obligatory fields (that
	 * must be supplied by the user) defined by subjectClass exist in
	 * htblFieldValue.
	 * 
	 * The default obligatory field is the uniqueIdentifierField(mapped to
	 * uniqueIdentifierProperty in subjectClass). For now this is the only
	 * obligatory field so the check will be only on this field. if subjectClass
	 * has a uniqueIdentifierProperty
	 * 
	 * 
	 * @param subjectClass
	 *            The class type of the inserted individual
	 * 
	 * @param htblFieldValue
	 *            The fieldValueMap of the instance which contains the
	 *            fields(mapped to a property) and the value of this field
	 * @return It will return a boolean true if the obligatoryFields of
	 *         subjectClass are supplied in htblFieldValue of the inserted
	 *         individual
	 * 
	 */
	private boolean areObligatoryFieldsExist(Class subjectClass, LinkedHashMap<String, Object> htblFieldValue) {

		/*
		 * check if the subjectClass has uniqueIdentifierProperty
		 */
		if (subjectClass.isHasUniqueIdentifierProperty()) {

			/*
			 * get uniqueIdentifier property name eg. userName for Person class
			 */
			String uniqueIdentiferPropertyName = subjectClass.getUniqueIdentifierPropertyName();

			/*
			 * check if htblFieldValue has field with name equal
			 * uniqueIdentiferPropertyName
			 * 
			 * return true if it exist and false if it does not
			 */
			if (htblFieldValue.containsKey(uniqueIdentiferPropertyName)) {
				return true;
			} else {
				return false;
			}

		} else {

			/*
			 * return true because there are not obligatory fields to check for
			 * because as I stated the only obligatory field is the
			 * uniqueIdentifierField which maps to uniqueIdentifierProperty
			 */
			return true;
		}

	}

	public static void main(String[] args) {
		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		String szUser = "rdfusr";
		String szPasswd = "rdfusr";
		String szJdbcDriver = "oracle.jdbc.driver.OracleDriver";

		// BasicDataSource dataSource = new BasicDataSource();
		// dataSource.setDriverClassName(szJdbcDriver);
		// dataSource.setUrl(szJdbcURL);
		// dataSource.setUsername(szUser);
		// dataSource.setPassword(szPasswd);

		// DynamicConceptsDao dynamicConceptDao = new
		// DynamicConceptsDao(dataSource);

		Oracle oracle = new Oracle(szJdbcURL, szUser, szPasswd);
		ValidationDao validationDao = new ValidationDao(oracle);

		DynamicOntologyDao dynamicOntologyDao = new DynamicOntologyDao(oracle);

		System.out.println("Connected to Database");

		InsertRequestValidation requestFieldsValidation = new InsertRequestValidation(validationDao,
				dynamicOntologyDao);

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

		LinkedHashMap<String, Object> htblFieldValue = new LinkedHashMap<>();

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
		// // htblFieldValue.put("id", "21203-321202");
		// htblFieldValue.put("hasCoverage", coverage);
		// htblFieldValue.put("hasSurvivalRange", survivalRange);
		// // htblFieldValue.put("test", "2134-2313-242-33332");

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
		// hatemmorgan.put("knows", "karammorgan");
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
		ahmedmorgnan.put("loves", hatemmorgan);

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

		htblFieldValue.put("adminOf", "TESTAPPLICATION");
		// htblFieldValue.put("knows", ahmedmorgnan);
		htblFieldValue.put("hates", ahmedmorgnan);
		// ArrayList<LinkedHashMap<String, Object>> loveList = new
		// ArrayList<>();
		// loveList.add(hatemmorgan2);
		// loveList.add(hatemmorgan);
		// htblFieldValue.put("love", loveList);
		// htblFieldValue.put("job", "Engineeer");

		try {
			long startTime = System.currentTimeMillis();
			Hashtable<String, ArrayList<ArrayList<InsertionPropertyValue>>> htblClassPropertyValue = requestFieldsValidation
					.validateRequestFields(htblFieldValue,
							OntologyMapper.getOntologyMapper().getHtblMainOntologyClassesMappers().get("admin"),
							"TESTAPPLICATION_MODEL");
			// Hashtable<Class, ArrayList<ArrayList<PropertyValue>>>
			// htblClassPropertyValue = requestFieldsValidation
			// .validateRequestFields("TESTAPPLICATION", htblFieldValue, new
			// Developer());

			System.out.println("Time Taken: " + ((System.currentTimeMillis() - startTime) / 1000.0));

			Iterator<String> iterator = htblClassPropertyValue.keySet().iterator();
			while (iterator.hasNext()) {
				String clssName = iterator.next();
				System.out.println(clssName + "[ ");
				ArrayList<ArrayList<InsertionPropertyValue>> list = htblClassPropertyValue.get(clssName);
				for (ArrayList<InsertionPropertyValue> arrayList : list) {
					System.out.print(arrayList);
					System.out.println();
				}
				System.out.println(" ]");
			}

			// System.out.println(requestFieldsValidation.htblAllStaticClasses.get("http://xmlns.com/foaf/0.1/Person")
			// .getProperties());
			// System.out.println(
			// requestFieldsValidation.htblAllStaticClasses.get("http://iot-platform#Developer").getProperties());

		} catch (ErrorObjException e) {
			System.out.println(e.getExceptionMessage());
		}
	}
}
