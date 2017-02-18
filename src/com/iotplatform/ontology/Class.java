package com.iotplatform.ontology;

import java.util.ArrayList;

/*
 * It defines Classes in ontologies which is one of the main requirements for a semantic ontology
 */
public class Class {
	private String prefix;
	private String uri;

	public Class(String prefix, String uri) {
		this.prefix = prefix;
		this.uri = uri;
	}

}
