package com.iotplatform.validations;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.XSDDataTypes;
import com.iotplatform.ontology.classes.Admin;
import com.iotplatform.ontology.classes.Application;
import com.iotplatform.ontology.classes.Developer;
import com.iotplatform.ontology.classes.Person;

@Component
public class RequestValidation {

	public RequestValidation() {
		System.out.println("Request Validation Bean Created");
	}

	/*
	 * checkIfFieldsValid checks if the fields passed by the http request are
	 * valid or not
	 */
	public boolean checkIfFieldsValid(Class ontologyClass, ArrayList<String> fields) {

		Hashtable<String, Property> htblProperties = ontologyClass.getProperties();

		for (String field : fields) {
			if (!htblProperties.containsKey(field)) {
				return false;
			}
		}

		return true;
	}

	/*
	 * checkIfDatatypesValid checks that the datatype of the values passed with
	 * the property are valid to maintain data integrity and consistency
	 */
	public boolean checkIfDatatypesValid(Class ontologyClass, Hashtable<String, Object> htblPropValue) {

		Hashtable<String, Property> properties = ontologyClass.getProperties();

		return false;
	}

	private boolean isPropertyValid(Property property, Object value) {

		if (property instanceof DataProperty) {
			DataProperty targetDataProperty = (DataProperty) property;
			return isDataValueValid(targetDataProperty, value);
		}else{
			// Object Property
			ObjectProperty targetObjectproperty = (ObjectProperty) property;
			Class objectClassType = targetObjectproperty.getObject();
			
		}

		return false;
	}

	private boolean isDataValueValid(DataProperty dataProperty, Object value) {

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
	
	
	private boolean isObjectValuePropertyValid(String applicationName, Class valueClassType , Object value ){
		
		
		return false;
	}

	 public static void main(String[] args) {
//	 RequestValidation requestValidation = new RequestValidation();
//	 ArrayList<String> fields = new ArrayList<>();
//	 fields.add("firstName");
//	 fields.add("knows");
//	 // this will fail the check make developedApplication to pass the check
//	 fields.add("adminOf");
//	 System.out.println(requestValidation.checkIfFieldsValid(new Developer(),
//	 fields));
		 
	 }
}
