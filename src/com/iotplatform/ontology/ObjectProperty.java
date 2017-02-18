package com.iotplatform.ontology;

/*
 * It reperesents a Data Property that is a subclass of Property .
 * It must have a subject of type class and a value of instance of Class
 */
public class ObjectProperty extends Property {
	private OntologyClass object;

	public ObjectProperty(String name, Prefixes prefix, OntologyClass object) {
		super(name, prefix);
		this.object = object;
	}

	public OntologyClass getObject() {
		return object;
	}

	@Override
	public String toString() {
		return "ObjectProperty [object=" + object + ", getName()=" + getName() + ", getPrefix()=" + getPrefix() + "]";
	}

	
	
}
