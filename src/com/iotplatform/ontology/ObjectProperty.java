package com.iotplatform.ontology;

/*
 * It reperesents a Data Property that is a subclass of Property .
 * It must have a subject of type class and a value of instance of Class
 */
public class ObjectProperty extends Property {
	private Class object;

	public ObjectProperty(String name, Prefixes prefix, Class object) {
		super(name, prefix);
		this.object = object;
	}

	public Class getObject() {
		return object;
	}

}
