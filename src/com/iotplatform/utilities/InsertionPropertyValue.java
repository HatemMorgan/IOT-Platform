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
	private boolean isPropertyHasSingleValue;

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

	public InsertionPropertyValue(String prefixedObjectValueClassName, String propertyName, Object value,
			boolean isObject) {
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

	public boolean isObject() {
		return isObject;
	}

	public String getPrefixedObjectValueClassName() {
		return prefixedObjectValueClassName;
	}

	public boolean isPropertyHasSingleValue() {
		return isPropertyHasSingleValue;
	}

	public void setPropertyHasSingleValue(boolean isPropertyHasSingleValue) {
		this.isPropertyHasSingleValue = isPropertyHasSingleValue;
	}

	@Override
	public String toString() {
		return "InsertionPropertyValue [prefixedObjectValueClassName=" + prefixedObjectValueClassName
				+ ", propertyName=" + propertyName + ", value=" + value + ", isObject=" + isObject
				+ ", isPropertyHasSingleValue=" + isPropertyHasSingleValue + "]";
	}

}
