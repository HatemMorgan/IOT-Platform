package com.iotplatform.ontology;

/*
 *  An Abstract class that describe the representation of a property in triples
 *  A property must have a subject which must be an instance of a Class
 */
public abstract class Property {
	private Class subject;

	public Property(Class subject) {

		this.subject = subject;
	}

	public Class getSubject() {
		return subject;
	}

}
