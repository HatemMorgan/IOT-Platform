package com.iotplatform.ontology;

public enum Prefixes {
	SSN("http://purl.oclc.org/NET/ssnx/ssn#"), 
	GEO("http://www.w3.org/2003/01/geo/wgs84_pos#"), 
	IOT_LITE( "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#"),
	IOT_PLATFORM("http://iot-platform#"),
	FOAF("http://xmlns.com/foaf/0.1/"),
	XSD("http://www.w3.org/2001/XMLSchema#"),
	OWL("http://www.w3.org/2002/07/owl#"),
	RDFS("http://www.w3.org/2000/01/rdf-schema#"),
	RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#");

	private final String uri ;

	private Prefixes(final String uri) {
		this.uri = uri;
	}
}
