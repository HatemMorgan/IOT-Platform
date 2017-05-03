package com.iotplatform.utilities;

/*
 *  is used to create instance to hold property name and subject variable of of an object value
 *  
 *  eg.  ?subject1 foaf:knows ?subject2
 *  
 *  where ?subject2 is object value and  ?subject1 is subject variable
 */
public class QueryVariableUtility {

	/*
	 * class type of subjectVariableName
	 */
	private String subjectClassUri;

	/*
	 * subjectVariableName is ?subject1 in the above example
	 */
	private String subjectVariableName;

	/*
	 * prefixedPropertyName is the name of property that connects two variables
	 * (nodes) (foaf:knows in the above example)
	 */
	private String prefixedPropertyName;

	public QueryVariableUtility(String subjectVariableName, String prefixedPropertyName, String subjectClassUri) {
		this.subjectVariableName = subjectVariableName;
		this.prefixedPropertyName = prefixedPropertyName;
		this.subjectClassUri = subjectClassUri;
	}

	public String getSubjectVariableName() {
		return subjectVariableName;
	}

	public String getPropertyName() {
		return prefixedPropertyName;
	}

	public String getSubjectClassUri() {
		return subjectClassUri;
	}

	@Override
	public String toString() {
		return "QueryVariable [subjectClassUri=" + subjectClassUri + ", subjectVariableName=" + subjectVariableName
				+ ", prefixedPropertyName=" + prefixedPropertyName + "]";
	}

}
