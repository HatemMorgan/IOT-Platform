package com.iotplatform.ontology;

public enum XSDDatatype {
	string_typed("^^xsd:string", "string", "http://www.w3.org/2001/XMLSchema#string"), 
	integer_typed("^^xsd:integer","integer", "http://www.w3.org/2001/XMLSchema#integer"),
	decimal_typed("^^xsd:decimal", "decimal","http://www.w3.org/2001/XMLSchema#decimal"), 
	float_typed("^^xsd:float", "float", "http://www.w3.org/2001/XMLSchema#float"), 
	double_typed("^^xsd:double", "double","http://www.w3.org/2001/XMLSchema#double"), 
	boolean_type("^^xsd:boolean", "boolean","http://www.w3.org/2001/XMLSchema#boolean"), 
	dateTime_typed("^^xsd:dateTime", "dateTime","http://www.w3.org/2001/XMLSchema#dateTime");

	private final String xsdType;
	private final String dataType;
	private final String xsdTypeURI;

	private XSDDatatype(final String xsdType, final String dataType, final String xsdTypeURI) {
		this.xsdType = xsdType;
		this.dataType = dataType;
		this.xsdTypeURI = xsdTypeURI;
	}

	public String getXsdType() {
		return xsdType;
	}

	public String getDataType() {
		return dataType;
	}

	public String getXsdTypeURI() {
		return xsdTypeURI;
	}
	
	

}