package com.iotplatform.utilities;

import com.iotplatform.ontology.Class;

public class NotMappedDynamicQueryFields {

	/*
	 * class type of the not mapped feild
	 */
	private Class subjectClass;

	/*
	 * field name of the not mapped fields
	 */
	private String fieldName;

	private Object fieldObject;

	/*
	 * individualUniqueIdintifier is the uniqueIdentifier of the individual of
	 * type subjectClass
	 */
	private String individualUniqueIdintifier;

	public NotMappedDynamicQueryFields(Class subjectClass, String fieldName, String individualUniqueIdintifier) {
		this.subjectClass = subjectClass;
		this.fieldName = fieldName;
		this.individualUniqueIdintifier = individualUniqueIdintifier;
	}

	public NotMappedDynamicQueryFields(Class subjectClass, String fieldName, Object fieldObject,
			String individualUniqueIdintifier) {
		this.subjectClass = subjectClass;
		this.fieldName = fieldName;
		this.fieldObject = fieldObject;
		this.individualUniqueIdintifier = individualUniqueIdintifier;
	}

	public Class getSubjectClass() {
		return subjectClass;
	}

	public String getFieldName() {
		return fieldName;
	}

	public Object getFieldObject() {
		return fieldObject;
	}

	public String getIndividualUniqueIdintifier() {
		return individualUniqueIdintifier;
	}

	@Override
	public String toString() {
		return "NotMappedDynamicQueryFields [subjectClass=" + subjectClass + ", fieldName=" + fieldName + "]";
	}

}
