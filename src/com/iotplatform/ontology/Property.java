package com.iotplatform.ontology;

/*
 *  An Abstract class that describe the representation of a property in triples
 *  A property must have a subject which must be an instance of a Class
 */
public abstract class Property {
	private String name;
	private Prefixes prefix;
	private String applicationName;
	private boolean mulitpleValues;
	private boolean unique;

	public Property(String name, Prefixes prefix, boolean mulitpleValues,boolean unique ) {

		this.name = name;
		this.prefix = prefix;
		this.applicationName = null;
		this.mulitpleValues = mulitpleValues;
		this.unique = unique;
	}

	public Property(String name, Prefixes prefix, String applicationName) {

		this.name = name;
		this.prefix = prefix;
		this.applicationName = applicationName;
	}

	public String getName() {
		return name;
	}

	public Prefixes getPrefix() {
		return prefix;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public boolean isMulitpleValues() {
		return mulitpleValues;
	}

}
