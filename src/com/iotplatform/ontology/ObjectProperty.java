package com.iotplatform.ontology;

/*
 * It reperesents a Data Property that is a subclass of Property .
 * It must have a subject of type class and a value of instance of Class
 */
public class ObjectProperty extends Property {
	private Class object;

	public ObjectProperty(String name, Prefixes prefix, Class object, boolean mulitpleValues, boolean unique) {
		super(name, prefix, mulitpleValues, unique);
		this.object = object;
	}

	public ObjectProperty(String name, Prefixes prefix, Class object, String applicationName, int mulitpleValues,
			int unique) {
		super(name, prefix, applicationName, mulitpleValues, unique);
		this.object = object;
	}

	public Class getObject() {
		return object;
	}

	@Override
	public String toString() {
		return "ObjectProperty [object=" + object + "]";
	}

}
