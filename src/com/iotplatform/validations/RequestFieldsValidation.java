package com.iotplatform.validations;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.daos.DynamicConceptDao;
import com.iotplatform.ontology.Class;
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

			}
		}

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

}
