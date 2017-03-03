package com.iotplatform.ontology;

public enum Prefixes {
	SSN("http://purl.oclc.org/NET/ssnx/ssn#","ssn:","ssn"), 
	GEO("http://www.w3.org/2003/01/geo/wgs84_pos#","geo:","geo"), 
	IOT_LITE( "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#","iot-lite:","iot-lite"),
	IOT_PLATFORM("http://iot-platform#","iot-platform:","iot-platform"),
	FOAF("http://xmlns.com/foaf/0.1/","foaf:","foaf"),
	XSD("http://www.w3.org/2001/XMLSchema#","xsd:","xsd"),
	OWL("http://www.w3.org/2002/07/owl#","owl:","owl"),
	RDFS("http://www.w3.org/2000/01/rdf-schema#","rdfs:","rdfs"),
	RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf:","rdf");

	private final String uri ;
	private final String prefix;
	private final String prefixName;
	
	
	private Prefixes(final String uri,final String prefix,String prefixName) {
		this.uri = uri;
		this.prefix = prefix;
		this.prefixName = prefixName;
	}
	
	
	public String getUri() {
		return uri;
	}
	public String getPrefix() {
		return prefix;
	}
	public String getPrefixName() {
		return prefixName;
	}
	
	
	
}
