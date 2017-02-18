package com.iotplatform.ontology;

import java.util.ArrayList;

/*
 * It defines Classes in ontologies which is one of the main requirements for a semantic ontology
 */
public class Class {
	private String name;
	private String uri;
	private Prefixes prefix;

	public Class(String name, String uri, Prefixes prefix) {
		this.name = name;
		this.uri = uri;
		this.prefix = prefix;
	}

}
