package com.iotplatform.utilities;

/*
 * PropertyValue class is used to create instances which has a propertyName and an object value and a boolean isObject which
 * specifies if the value was object or just a datatype value (eg. string,int,etc.)
 * 
 * This class is used by the class that take a post request json and parse it to ontology classes and properties
 * so the ValueType instance will represent if the value of an objectProperty is a class(isObject=true) or it is a
 * datatype value (isObject = false)
 * 
 * This Class is also used to construct the proper propertyValue pair that will be used in the insert query
 */

public class InsertionPropertyValue {

	/*
	 * the prefixedName of objectValue class type
	 */
	private String prefixedObjectValueClassName;
	private String propertyName;
	private Object value;
	private boolean isObject;

	public InsertionPropertyValue(String propertyName, Object value) {
		this.propertyName = propertyName;
		this.value = value;
	}

	public InsertionPropertyValue(String propertyName, Object value, boolean isObject) {
		this.propertyName = propertyName;
		this.value = value;
		this.isObject = isObject;
	}

	public InsertionPropertyValue(Object value, boolean isObject) {
		this.value = value;
		this.isObject = isObject;
	}

	public InsertionPropertyValue(String prefixedObjectValueClassName, String propertyName, Object value, boolean isObject) {
		this.prefixedObjectValueClassName = prefixedObjectValueClassName;
		this.propertyName = propertyName;
		this.value = value;
		this.isObject = isObject;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public Object getValue() {
		return value;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public boolean isObject() {
		return isObject;
	}

	public void setObject(boolean isObject) {
		this.isObject = isObject;
	}

	public String getPrefixedObjectValueClassName() {
		return prefixedObjectValueClassName;
	}

	@Override
	public String toString() {
		return "PropertyValue [prefixedClassName=" + prefixedObjectValueClassName + ", propertyName=" + propertyName
				+ ", value=" + value + ", isObject=" + isObject + "]";
	}

}
