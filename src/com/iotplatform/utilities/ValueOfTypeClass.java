package com.iotplatform.utilities;

import com.iotplatform.ontology.Class;

/*
 * ValueTypeClass is used to create instances that represent an object value of type class
 * 
 * ex: iot-platform:hatemmorgan a foaf:Agent 
 * 
 * where value = iot-platform:hatemmorgan and type class is foaf:Agent
 * 
 * This Class is used to create list of ValueOfTypeClass instances that is used in dataIntegrityValidation query
 * 
 * It also used in creating uniqueConstraintValidation query
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

	@Override
	public String toString() {
		return "ValueOfTypeClass [typeClass=" + typeClass + ", value=" + value + "]";
	}

}
