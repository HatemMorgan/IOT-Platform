package com.iotplatform.utilities;

/* 
 * PropertyValue class is used to create instances from it that describes a property value pair for a specific subject
 */
public class PropertyValue {

	private String propertyName;
	private Object value;

	public PropertyValue(String propertyName, Object value) {
		this.propertyName = propertyName;
		this.value = value;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public Object getValue() {
		return value;
	}

}
