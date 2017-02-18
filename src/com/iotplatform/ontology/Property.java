package com.iotplatform.ontology;

/*
 *  An Abstract class that describe the representation of a property in triples
 *  A property must have a subject which must be an instance of a Class
 */
public abstract class Property {
	private String name;
	private Prefixes prefix;

	public Property(String name, Prefixes prefix) {

		this.name = name;
		this.prefix = prefix;
	}

	public String getName() {
		return name;
	}

	public Prefixes getPrefix() {
		return prefix;
	}

}
