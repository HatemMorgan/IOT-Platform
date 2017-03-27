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

	public NotMappedDynamicQueryFields(Class subjectClass, String fieldName) {
		this.subjectClass = subjectClass;
		this.fieldName = fieldName;
	}

	public Class getSubjectClass() {
		return subjectClass;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public String toString() {
		return "NotMappedDynamicQueryFields [subjectClass=" + subjectClass + ", fieldName=" + fieldName + "]";
	}
	
	

}
