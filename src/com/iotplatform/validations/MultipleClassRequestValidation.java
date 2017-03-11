package com.iotplatform.validations;

import java.util.ArrayList;
import java.util.Hashtable;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.utilities.InsertionUtility;
import com.iotplatform.utilities.PropertyValue;

/*
 * MultipleClassRequestValidation class is used to validate multiple Class request
 * (A request that consists of more than one object)
 * 
 * eg. ActuatingDevice,CommunicatingDevice,Sensor
 */

@Component
public class MultipleClassRequestValidation {

	/*
	 * isFieldsValid checks if the fields passed by the http request are valid
	 * or not and it returns a hashtable of propertyValue keyValue
	 * 
	 * This method takes a hashtable of fields and values . the values may be
	 * single value or an Arraylist or an object so this method will reconstruct
	 * the request body into the appropriate classes and check for the validity
	 * of each field(by checking if maps to a valid property or not)
	 */
	private Hashtable<Object, Object> isFieldsValid(String applicationName, Class subjectClass,
			Hashtable<String, Object> htblPropertyValue) {
		return null;
	}

	public ArrayList<PropertyValue> isRequestValid(String applicationName, Class subjectClass,
			Hashtable<String, Object> htblPropertyValue) {
		return null;
		// Hashtable<Object, Object> htblPropValue =
		// isFieldsValid(applicationName, subjectClass, htblPropertyValue);
		// ArrayList<PropertyValue> propertyValueList =
		// InsertionUtility.constructPropValueList(htblPropValue);
		// return isProrpertyValueValid(propertyValueList, subjectClass,
		// applicationName);

	}

}
