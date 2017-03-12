package com.iotplatform.utilities;

/*
 * ValueType class is used to create instances which has an object value and a boolean isObject which
 * specifies if the value was object or just a datatype value (eg. string,int,etc.)
 * 
 * This class is used by the class that take a post request json and parse it to ontology classes and properties
 * so the ValueType instance will represent if the value of an objectProperty is a class(isObject=true) or it is a
 * datatype value (isObject = false)
 */

public class ValueType {
	private Object value;
	private boolean isObject;

	public ValueType(Object value, boolean isObject) {
		this.value = value;
		this.isObject = isObject;
	}

	public Object getValue() {
		return value;
	}

	public boolean isObject() {
		return isObject;
	}

}
