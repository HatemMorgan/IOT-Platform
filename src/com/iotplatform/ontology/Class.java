package com.iotplatform.ontology;

import java.util.ArrayList;
import java.util.Hashtable;

/*
 * It defines Classes in ontologies which is one of the main requirements for a semantic ontology
 */
public class Class {
	private String name;
	private String uri;
	private Prefixes prefix;
	private Hashtable<String, Property> properties;
	private Hashtable<String, String> htblPropUriName;

	private ArrayList<Class> superClassesList;

	public Class(String name, String uri, Prefixes prefix) {
		this.name = name;
		this.uri = uri;
		this.prefix = prefix;
		this.htblPropUriName = new Hashtable<>();
		properties = new Hashtable<>();
		superClassesList = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public String getUri() {
		return uri;
	}

	public Prefixes getPrefix() {
		return prefix;
	}

	public Hashtable<String, Property> getProperties() {
		return properties;
	}

	public Hashtable<String, String> getHtblPropUriName() {
		return htblPropUriName;
	}

	public ArrayList<Class> getSuperClassesList() {
		return superClassesList;
	}

}
