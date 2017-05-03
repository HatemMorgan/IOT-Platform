package com.iotplatform.utilities;

/**
 * UpdatePropertyValue is used to create instances that will result of parsing
 * done by UpdateRequestValidation class to be used after that by UpdateQuery to
 * construct query
 * 
 * 
 * @author HatemMorgan
 *
 */
public class UpdatePropertyValueUtility {

	/*
	 * propertyPrefixedName holds prefixName of the preroprty eg: foaf:member
	 */
	private String propertyPrefixedName;

	/*
	 * isPropertyMultipleValued hold true if property has multiple values and
	 * false if it has only one value (single value)
	 */
	private boolean isPropertyMultipleValued;

	/*
	 * oldValue holds the old value if the property has multiple values. it will
	 * be null if isPropertyMultipleValued = false
	 */
	private Object oldValue;

	/*
	 * newValue holds the new value that the propertyPrefixedName property will
	 * be updated to
	 */
	private Object newValue;

	public UpdatePropertyValueUtility(String propertyPrefixedName, boolean isPropertyMultipleValued, Object oldValue,
			Object newValue) {
		this.propertyPrefixedName = propertyPrefixedName;
		this.isPropertyMultipleValued = isPropertyMultipleValued;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public String getPropertyPrefixedName() {
		return propertyPrefixedName;
	}

	public boolean isPropertyMultipleValued() {
		return isPropertyMultipleValued;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public Object getNewValue() {
		return newValue;
	}

	@Override
	public String toString() {
		return "UpdatePropertyValueUtility [propertyPrefixedName=" + propertyPrefixedName
				+ ", isPropertyMultipleValued=" + isPropertyMultipleValued + ", oldValue=" + oldValue + ", newValue="
				+ newValue + "]";
	}

}
