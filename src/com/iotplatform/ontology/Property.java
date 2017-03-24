package com.iotplatform.ontology;

import java.util.Hashtable;

import com.iotplatform.ontology.classes.Developer;
import com.iotplatform.ontology.classes.Device;
import com.iotplatform.ontology.classes.Person;

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

	public Property(String name, Prefixes prefix, boolean mulitpleValues, boolean unique) {

		this.name = name;
		this.prefix = prefix;
		this.applicationName = null;
		this.mulitpleValues = mulitpleValues;
		this.unique = unique;
	}

	public Property(String name, Prefixes prefix, String applicationName, int mulitpleValues, int unique) {

		this.name = name;
		this.prefix = prefix;
		this.applicationName = applicationName;

		if (mulitpleValues == 1) {
			this.mulitpleValues = true;
		} else {
			this.mulitpleValues = false;
		}

		if (unique == 1) {
			this.unique = true;
		} else {
			this.unique = false;
		}
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

	public boolean isUnique() {
		return unique;
	}

}
