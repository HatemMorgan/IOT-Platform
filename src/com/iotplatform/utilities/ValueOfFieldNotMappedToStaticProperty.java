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
	private Class propertyClass;

	/*
	 * value of the notMappedField
	 */
	private Object propertyValue;

	/*
	 * index of instance in the propertyClass instancesList
	 */
	private int classInstanceIndex;

	/*
	 * randomID is a random generated ID to represent the subjectInstance of
	 * this property this randomID is used in uniqueConstraint Validation to
	 * reference the uniquePropertyValues of the instance that has this property
	 */
	private String randomID;

	public ValueOfFieldNotMappedToStaticProperty(Class propertyClass, Object propertyValue, int classInstanceIndex,
			String randomID) {
		this.propertyClass = propertyClass;
		this.propertyValue = propertyValue;
		this.classInstanceIndex = classInstanceIndex;
		this.randomID = randomID;
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

	public String getRandomID() {
		return randomID;
	}

}
