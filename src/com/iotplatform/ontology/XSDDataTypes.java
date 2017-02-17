package com.iotplatform.ontology;

public enum XSDDataTypes {
	string_typed("xsd:string"), integer_typed("xsd:integer"), decimal_typed("xsd:decimal"), float_typed(
			"xsd:float"), double_typed("xsd:double"), boolean_type("xsd:boolean"), dateTime_typed("xsd:dateTime");

	private final String xsdType;

	private XSDDataTypes(final String xsdType) {
		this.xsdType = xsdType;
	}

	public String toString() {
		return xsdType;
	}

}