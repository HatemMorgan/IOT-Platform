package com.iotplatform.ontology;

/*
 * It reperesents a Data Property that is a subclass of Property .
 * It must have a subject of type class and a value of instance of Class
 */
public class ObjectProperty extends Property {
	private Class object;

	public ObjectProperty(Class subject, Class object) {
		super(subject);
		this.object = object;
	}

	public Class getObject() {
		return object;
	}

}
