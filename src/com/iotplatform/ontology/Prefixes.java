package com.iotplatform.ontology;

public enum Prefixes {
	SSN("http://purl.oclc.org/NET/ssnx/ssn#","ssn:"), 
	GEO("http://www.w3.org/2003/01/geo/wgs84_pos#","geo:"), 
	IOT_LITE( "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#","iot-lite:"),
	IOT_PLATFORM("http://iot-platform#","iot-platform:"),
	FOAF("http://xmlns.com/foaf/0.1/","foaf:"),
	XSD("http://www.w3.org/2001/XMLSchema#","xsd:"),
	OWL("http://www.w3.org/2002/07/owl#","owl:"),
	RDFS("http://www.w3.org/2000/01/rdf-schema#","rdfs:"),
	RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf:");

	private final String uri ;
	private final String prefix;
	private Prefixes(final String uri,final String prefix) {
		this.uri = uri;
		this.prefix = prefix;
	}
	public String getUri() {
		return uri;
	}
	public String getPrefix() {
		return prefix;
	}
	
	
}
