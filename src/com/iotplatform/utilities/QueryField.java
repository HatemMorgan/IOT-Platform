package com.iotplatform.utilities;

/*
 * QueryField is used to create instances that will holds the propertyName , type
 *  (represents if it will have single value or an object value) and type of objectValue
 */
public class QueryField {

	/*
	 * prefixedPropertyName holds the prefixedName of the property
	 */
	private String prefixedPropertyName;

	/*
	 * isValueObject is a boolean attribute that tells if the value is an
	 * objectVaue
	 * 
	 * true = objectValue , false = singleValue(literal)
	 */
	private boolean isValueObject;

	/*
	 * objectValueType holds the prefiexedName of the class type of objectValue
	 */
	private String objectValueTypeClassName;

	/*
	 * String uniqueIdentifier to represent the key of the individual of type
	 * objectValueTypeClassName
	 */
	private String individualUniqueIdentifier;

	public QueryField(String prefixedPropertyName, String objectValueTypeClassName, String individualUniqueIdentifier) {
		this.prefixedPropertyName = prefixedPropertyName;
		this.objectValueTypeClassName = objectValueTypeClassName;
		this.individualUniqueIdentifier = individualUniqueIdentifier;
		this.isValueObject = (objectValueTypeClassName == null) ? false : true;

	}

	public String getPrefixedPropertyName() {
		return prefixedPropertyName;
	}

	public boolean isValueObject() {
		return isValueObject;
	}

	public String getObjectValueTypeClassName() {
		return objectValueTypeClassName;
	}

	public String getIndividualUniqueIdentifier() {
		return individualUniqueIdentifier;
	}

	@Override
	public String toString() {
		return "QueryField [prefixedPropertyName=" + prefixedPropertyName + ", isValueObject=" + isValueObject
				+ ", objectValueTypeClassName=" + objectValueTypeClassName + ", individualUniqueIdentifier="
				+ individualUniqueIdentifier + "]";
	}

}
