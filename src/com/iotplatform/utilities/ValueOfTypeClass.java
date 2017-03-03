package com.iotplatform.utilities;

import com.iotplatform.ontology.Class;

/*
 * ValueTypeClass is used to create instances that represent an object value of type class
 * 
 * ex: iot-platform:hatemmorgan a foaf:Agent 
 * 
 * where value = iot-platform:hatemmorgan and type class is foaf:Agent
 * 
 */

public class ValueOfTypeClass {

	private Class typeClass;
	private Object value;

	public ValueOfTypeClass(Class typeClass, Object value) {
		this.typeClass = typeClass;
		this.value = value;
	}

	public Class getTypeClass() {
		return typeClass;
	}

	public Object getValue() {
		return value;
	}

}
