package com.iotplatform.utilities;

import com.iotplatform.ontology.Class;

/*
 * ValueOfFieldNotMappedToStaticProperty class is used to create instance that holds the value of the not mapped
 * field to a static property and waiting to be checked after loading dynamic properties 
 * 
 */
public class NotMappedInsertRequestFieldUtility {

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
	 * fieldName holds field name that has no static mapping to a property
	 */
	private String fieldName;

	public NotMappedInsertRequestFieldUtility(Class propertyClass, Object propertyValue, int classInstanceIndex,
			String fieldName) {
		this.propertyClass = propertyClass;
		this.propertyValue = propertyValue;
		this.classInstanceIndex = classInstanceIndex;
		this.fieldName = fieldName;
	}

	public NotMappedInsertRequestFieldUtility(Class propertyClass, Object propertyValue, String fieldName) {
		this.propertyClass = propertyClass;
		this.propertyValue = propertyValue;
		this.fieldName = fieldName;
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

	// public String getRandomID() {
	// return randomID;
	// }

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public String toString() {
		return "ValueOfFieldNotMappedToStaticProperty [propertyClass=" + propertyClass + ", propertyValue="
				+ propertyValue + ", classInstanceIndex=" + classInstanceIndex + ", fieldName=" + fieldName + "]";
	}

}
