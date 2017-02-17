package com.iotplatform.ontology;

import java.util.ArrayList;

/*
 * It defines Classes in ontologies which is one of the main requirements for a semantic ontology
 */
public class Class {
	private ArrayList<Property> properties;

	public Class(ArrayList<Property> properties) {
		this.properties = properties;
	}

	public ArrayList<Property> getProperties() {
		return properties;
	}

}
