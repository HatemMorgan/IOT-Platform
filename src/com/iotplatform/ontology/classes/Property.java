package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

/*
 *  This Class maps ssn:Property class in the ontology
 *  
 *  An observable Quality of an Event or Object.  That is, not a quality of an abstract entity as is 
 *  also allowed by DUL's Quality, but rather an aspect of an entity that is intrinsic to and cannot exist 
 *  without the entity and is observable by a sensor.
 */

@Component
public class Property extends Class {

	private static Property propertyInstance;

	public Property(String name, String uri, Prefixes prefix) {
		super(name, uri, prefix);
		init();
	}

	public Property() {
		super("Property", "http://purl.oclc.org/NET/ssnx/ssn#Property", Prefixes.SSN);
		init();
	}

	/*
	 * String nothing parameter is added for overloading constructor technique
	 * because I need to initialize an instance without having properties and it
	 * will be always passed by null
	 */
	public Property(String nothing) {
		super("Property", "http://purl.oclc.org/NET/ssnx/ssn#Property", Prefixes.SSN);
	}

	public synchronized static Property getPropertyInstance() {
		if (propertyInstance == null)
			propertyInstance = new Property();

		return propertyInstance;
	}

	private void init() {
		
	}
}
