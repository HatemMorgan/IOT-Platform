package com.iotplatform.utilities;

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
	 * 
	 */
	Object propertyValue;
	int classInstanceIndex;
}
