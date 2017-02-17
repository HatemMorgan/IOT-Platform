package com.iotplatform.ontology;

/*
 * It reperesents a Data Property that is a subclass of Property .
 * It must have a subject of type class and a value of literal (can be typed literal or not typed literal)
 */
public class DataProperty extends Property {

	private Literal object;

	public DataProperty(Class subject, Literal object) {
		super(subject);
		this.object = object;
	}

	public Literal getObject() {
		return object;
	}

}
