package com.iotplatform.ontology;

/*
 * It defines a mapping from xsd datatype to java datatype
 */

public class DataType {
	private Object javaType;
	private XSDDataTypes xsdType;

	public DataType(Object javaType, XSDDataTypes xsdType) {

		this.javaType = javaType;
		this.xsdType = xsdType;
	}

	public Object getJavaType() {
		return javaType;
	}

	public XSDDataTypes getXsdType() {
		return xsdType;
	}

}
