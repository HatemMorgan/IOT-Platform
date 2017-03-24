package com.iotplatform.ontology;

/*
 * It reperesents a Data Property that is a subclass of Property .
 * It must have a subject of type class and a value of literal (can be typed literal or not typed literal)
 */
public class DataTypeProperty extends Property {

	private XSDDataTypes dataType;

	public DataTypeProperty(Class subjectClass, String name, Prefixes prefix, XSDDataTypes dataType,
			boolean mulitpleValues, boolean unique) {
		super(subjectClass, name, prefix, mulitpleValues, unique);
		this.dataType = dataType;
	}

	public DataTypeProperty(Class subjectClass, String name, Prefixes prefix, XSDDataTypes dataType,
			String applicationName, int mulitpleValues, int unique) {
		super(subjectClass, name, prefix, applicationName, mulitpleValues, unique);
		this.dataType = dataType;
	}

	public XSDDataTypes getDataType() {
		return dataType;
	}

	@Override
	public String toString() {
		return "DataTypeProperty [dataType=" + dataType + ", getDataType()=" + getDataType() + ", getName()="
				+ getName() + ", getPrefix()=" + getPrefix() + ", getApplicationName()=" + getApplicationName()
				+ ", isMulitpleValues()=" + isMulitpleValues() + ", isUnique()=" + isUnique() + ", getSubjectClass()="
				+ getSubjectClass()+ "]";
	}

}
