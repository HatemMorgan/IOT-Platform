package com.iotplatform.utilities;

import com.iotplatform.ontology.Class;

/*
 * ValueOfFieldNotMappedToStaticProperty class is used to create instance that holds the value of the not mapped
 * field to a static property and waiting to be checked after loading dynamic properties 
 * 
 */
public class ValueOfFieldNotMappedToStaticProperty {

	/*
	 * represent the invalid field class ( that was not mapped to any static
	 * properties and waiting to be checked against dynamic properties after
	 * being loaded)
	 */
	Class propertyClass;

	/*
	 * value of the notMappedField
	 */
	Object propertyValue;

	/*
	 * index of instance in the propertyClass instancesList
	 */
	int classInstanceIndex;

	public ValueOfFieldNotMappedToStaticProperty(Class propertyClass, Object propertyValue, int classInstanceIndex) {
		this.propertyClass = propertyClass;
		this.propertyValue = propertyValue;
		this.classInstanceIndex = classInstanceIndex;
	}

	public Class getPropertyClass() {
		return propertyClass;
	}

	public Object getPropertyValue() {
		return propertyValue;
	}

	public int getClassInstanceIndex() {
		return classInstanceIndex;
	}

}
