package com.iotplatform.ontology;

public enum XSDDataTypes {
	string_typed("^^xsd:string", "string"), integer_typed("^^xsd:integer", "integer"), 
	decimal_typed("^^xsd:decimal","decimal"), float_typed("^^xsd:float", "float"), 
	double_typed("^^xsd:double","double"), boolean_type("^^xsd:boolean", "boolean"), 
	dateTime_typed("^^xsd:dateTime", "dateTime");

	private final String xsdType;
	private final String dataType;

	private XSDDataTypes(final String xsdType, final String dataType) {
		this.xsdType = xsdType;
		this.dataType = dataType;
	}

	public String getXsdType() {
		return xsdType;
	}

	public String getDataType() {
		return dataType;
	}

}