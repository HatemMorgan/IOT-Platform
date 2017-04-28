package com.iotplatform.utilities;

/*
 * QueryField is used to create instances that will holds the propertyName , type
 *  (represents if it will have single value or an object value) and type of objectValue
 */
public class QueryFieldUtility {

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

	/*
	 * subjectVariableName is used to save the the subjectVariable (eg.
	 * ?subject1) to be used by MainDao to save the subjectVariable of
	 * objectProperty to be used after that by SelectionUtility to know the
	 * subjectVariableName of an objectProperty that is connected to another
	 * node (another object defined by another subjctVariable)
	 */
	private String subjectVariableName;

	/*
	 * isValueObjectType is a boolean attribute the tells if the objectValue can
	 * have different types (the object class is a superClass of many classes so
	 * all the subClasses can be type of individuals of this class) ex:
	 * foaf:member in group class its range is foaf:Agent which has a lot of
	 * subClasses
	 * 
	 * this boolean is used when the user request body (used to construct query)
	 * has values field for a fieldName (which maps to property like
	 * foaf:member)
	 */
	private boolean isValueObjectType;

	public QueryFieldUtility(String prefixedPropertyName, String objectValueTypeClassName, String individualUniqueIdentifier) {
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

	public String getSubjectVariableName() {
		return subjectVariableName;
	}

	public void setSubjectVariableName(String subjectVariableName) {
		this.subjectVariableName = subjectVariableName;
	}

	public boolean isValueObjectType() {
		return isValueObjectType;
	}

	public void setValueObjectType(boolean isValueObjectType) {
		this.isValueObjectType = isValueObjectType;
	}

	@Override
	public String toString() {
		return "QueryField [prefixedPropertyName=" + prefixedPropertyName + ", isValueObject=" + isValueObject
				+ ", objectValueTypeClassName=" + objectValueTypeClassName + ", individualUniqueIdentifier="
				+ individualUniqueIdentifier + ", subjectVariableName=" + subjectVariableName + ", isValueObjectType="
				+ isValueObjectType + "]";
	}

}
