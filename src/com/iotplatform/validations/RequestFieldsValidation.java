package com.iotplatform.validations;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.UUID;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.daos.DynamicConceptDao;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.XSDDataTypes;
import com.iotplatform.ontology.classes.ActuatingDevice;
import com.iotplatform.ontology.classes.SurvivalProperty;
import com.iotplatform.ontology.classes.SurvivalRange;
import com.iotplatform.utilities.PropertyValue;

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

	@Autowired
	public RequestFieldsValidation(DynamicConceptDao dynamicConceptDao) {
		this.dynamicConceptDao = dynamicConceptDao;
	}

	/*
	 * validateRequestFields method is takes Hashtable<String, Object>
	 * htblFieldValue which is the request body represented as key fieldName and
	 * object value and also it takes the subjectClass which is the specified
	 * class in the request url eg. Sensor,ActuatingDevice
	 * 
	 * 
	 */
	public Hashtable<Class, ArrayList<PropertyValue>> validateRequestFields(Hashtable<String, Object> htblFieldValue,
			Class subjectClass) {

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
		Hashtable<String, Object> htblNotFoundFieldValue = new Hashtable<>();

		/*
		 * htblClassPropertyValue holds the constructed propertyValue
		 */
		Hashtable<Class, ArrayList<PropertyValue>> htblClassPropertyValue = new Hashtable<>();

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

			if (isFieldMapsToStaticProperty(subjectClass, fieldName, value, classList, htblNotFoundFieldValue)) {
				Property property = subjectClass.getProperties().get(fieldName);

				parseAndConstructFieldValue(subjectClass, property, value, htblClassPropertyValue, classList,
						htblNotFoundFieldValue);
			}

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
			PropertyValue idPropertyValue = new PropertyValue(id, false,
					idProperty.getPrefix().getPrefix() + idProperty.getName());

			/*
			 * add idPropertyValue object to htblClassPropertyValue
			 */
			if (htblClassPropertyValue.containsKey(subjectClass)) {
				htblClassPropertyValue.get(subjectClass).add(idPropertyValue);
			} else {
				ArrayList<PropertyValue> propertyValueList = new ArrayList<>();
				htblClassPropertyValue.put(subjectClass, propertyValueList);
				htblClassPropertyValue.get(subjectClass).add(idPropertyValue);
			}

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
			ArrayList<Class> classList, Hashtable<String, Object> htblNotFoundFieldValue) {

		if (subjectClass.getProperties().containsKey(fieldName)) {
			return true;
		} else {
			classList.add(subjectClass);
			htblNotFoundFieldValue.put(fieldName, value);
			return false;
		}
	}

	/*
	 * parseAndConstructFieldValue method is used to parse the Object value and
	 * reconstruct into prefixed property and prefixed value (constrcut a
	 * propertyValue object) and then add it to htblClassPropertyValue
	 */
	private void parseAndConstructFieldValue(Class subjectClass, Property property, Object value,
			Hashtable<Class, ArrayList<PropertyValue>> htblClassPropertyValue, ArrayList<Class> classList,
			Hashtable<String, Object> htblNotFoundFieldValue) {

		/*
		 * check if the value is of type primitive datatype
		 */
		if ((value instanceof String) || (value instanceof Integer) || (value instanceof Float)
				|| (value instanceof Double) || (value instanceof Boolean)) {

			/*
			 * construct a new PropertyValue instance to hold the prefiexed
			 * propertyName and prefixed value
			 */
			PropertyValue propertyValue = new PropertyValue(getValue(property, value), false,
					property.getPrefix().getPrefix() + property.getName());

			/*
			 * add PropertyValue object to htblClassPropertyValue
			 */
			if (htblClassPropertyValue.containsKey(subjectClass)) {
				htblClassPropertyValue.get(subjectClass).add(propertyValue);
			} else {
				ArrayList<PropertyValue> propertyValueList = new ArrayList<>();
				htblClassPropertyValue.put(subjectClass, propertyValueList);
				htblClassPropertyValue.get(subjectClass).add(propertyValue);
			}

		}

		/*
		 * value is an array of object so I will iterate on all the keyValue
		 * pairs and check if the fields are valid or not and reconstruct them
		 */
		if (value instanceof java.util.LinkedHashMap<?, ?> && property instanceof ObjectProperty) {
			LinkedHashMap<String, Object> valueObject = (LinkedHashMap<String, Object>) value;
			Class classType = ((ObjectProperty) property).getObject();

			/*
			 * linking subject class with object class by adding a the unique
			 * identifier as the object value of the property
			 */

			/*
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
			 * prefixing objectUniqueIdentifier
			 */

			objectUniqueIdentifier = "\"" + objectUniqueIdentifier + "\"" + XSDDataTypes.string_typed.getXsdType();

			/*
			 * add property id for classTypeObject and add generated UUID as the
			 * object of it
			 */

			Property idProperty = classType.getProperties().get("id");
			PropertyValue idPropertyValue = new PropertyValue(objectUniqueIdentifier, false,
					idProperty.getPrefix().getPrefix() + idProperty.getName());

			/*
			 * add idPropertyValue object to htblClassPropertyValue
			 */
			if (htblClassPropertyValue.containsKey(classType)) {
				htblClassPropertyValue.get(classType).add(idPropertyValue);
			} else {
				ArrayList<PropertyValue> propertyValueList = new ArrayList<>();
				htblClassPropertyValue.put(classType, propertyValueList);
				htblClassPropertyValue.get(classType).add(idPropertyValue);
			}

			/*
			 * construct a new PropertyValue instance to hold the prefiexed
			 * propertyName and prefixed value
			 */
			PropertyValue propertyValue = new PropertyValue(objectUniqueIdentifier, false,
					property.getPrefix().getPrefix() + property.getName());
			/*
			 * add PropertyValue object to htblClassPropertyValue
			 */
			if (htblClassPropertyValue.containsKey(subjectClass)) {
				htblClassPropertyValue.get(subjectClass).add(propertyValue);
			} else {
				ArrayList<PropertyValue> propertyValueList = new ArrayList<>();
				htblClassPropertyValue.put(subjectClass, propertyValueList);
				htblClassPropertyValue.get(subjectClass).add(propertyValue);
			}

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

				if (isFieldMapsToStaticProperty(classType, fieldName, fieldValue, classList, htblNotFoundFieldValue)) {
					// System.out.println("====>"+classType.getName() + " : " +
					// classType.getProperties());
					Property classTypeProperty = classType.getProperties().get(fieldName);

					parseAndConstructFieldValue(classType, classTypeProperty, fieldValue, htblClassPropertyValue,
							classList, htblNotFoundFieldValue);
				}

			}
		}

	}

	private boolean objectTypeCheck(Class subjectClass, Object value){
				
		return false;
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

		Hashtable<String, Object> htblFieldValue = new Hashtable<>();

		LinkedHashMap<String, Object> condition = new LinkedHashMap<>();
		condition.put("description", "High Tempreture Condition");

		LinkedHashMap<String, Object> amount = new LinkedHashMap<>();
		amount.put("hasDataValue", 20.21);

		LinkedHashMap<String, Object> survivalProperty = new LinkedHashMap<>();
		survivalProperty.put("hasValue", amount);

		LinkedHashMap<String, Object> survivalRange = new LinkedHashMap<>();
		survivalRange.put("inCondition", condition);
		survivalRange.put("hasSurvivalProperty", survivalProperty);

		// htblFieldValue.put("id", "2032-3232-2342");
		htblFieldValue.put("hasSurvivalRange", survivalRange);

		requestFieldsValidation.validateRequestFields(htblFieldValue, new ActuatingDevice());

	}
}
