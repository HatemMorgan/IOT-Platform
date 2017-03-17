package com.iotplatform.ontology;

public enum XSDDataTypes {
	string_typed("^^xsd:string", "string", "http://www.w3.org/2001/XMLSchema#string"), integer_typed("^^xsd:integer",
			"integer", "http://www.w3.org/2001/XMLSchema#integer"), decimal_typed("^^xsd:decimal", "decimal",
					"http://www.w3.org/2001/XMLSchema#string"), float_typed("^^xsd:float", "float",
							"http://www.w3.org/2001/XMLSchema#string"), double_typed("^^xsd:double", "double",
									"http://www.w3.org/2001/XMLSchema#string"), boolean_type("^^xsd:boolean", "boolean",
											"http://www.w3.org/2001/XMLSchema#string"), dateTime_typed("^^xsd:dateTime",
													"dateTime", "http://www.w3.org/2001/XMLSchema#string");

	private final String xsdType;
	private final String dataType;
	private final String xsdTypeURI;

	private XSDDataTypes(final String xsdType, final String dataType, final String xsdTypeURI) {
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

}