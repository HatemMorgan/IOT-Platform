package com.iotplatform.utilities;

/**
 * DeletePropertyValueUtility is used to create instances that will result of
 * parsing done by DeleteRequestValidation class to be used after that by
 * DeleteQuery to construct query
 * 
 * @author HatemMorgan
 *
 */
public class DeletePropertyValueUtility {

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
	 * valueToBeDeleted holds the old value (that will be deleted) of the
	 * property has multiple values. it will be null if isPropertyMultipleValued
	 * = false
	 */
	private Object valueToBeDeleted;

	public DeletePropertyValueUtility(String propertyPrefixedName) {
		this.propertyPrefixedName = propertyPrefixedName;

	}

	public DeletePropertyValueUtility(String propertyPrefixedName, boolean isPropertyMultipleValued,
			Object valueToBeDeleted) {
		this.propertyPrefixedName = propertyPrefixedName;
		this.isPropertyMultipleValued = isPropertyMultipleValued;
		this.valueToBeDeleted = valueToBeDeleted;
	}

	public String getPropertyPrefixedName() {
		return propertyPrefixedName;
	}

	public boolean isPropertyMultipleValued() {
		return isPropertyMultipleValued;
	}

	public Object getValueToBeDeleted() {
		return valueToBeDeleted;
	}

	@Override
	public String toString() {
		return "DeletePropertyValueUtility [propertyPrefixedName=" + propertyPrefixedName
				+ ", isPropertyMultipleValued=" + isPropertyMultipleValued + ", valueToBeDeleted=" + valueToBeDeleted
				+ "]";
	}

}
