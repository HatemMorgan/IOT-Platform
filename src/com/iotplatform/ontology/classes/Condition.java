package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Prefixes;

/*
 *  This Class maps ssn:Condition class in the ontology
 *  
 *  Condition class is an observable Quality of an Event or Object. 
 *   That is, not a quality of an abstract entity as is also allowed by DUL's Quality, 
 *   but rather an aspect of an entity that is intrinsic to and cannot exist without the entity 
 *   and is observable by a sensor.
 */

@Component
public class Condition extends Property {

	public Condition() {
		super("Condition", "http://purl.oclc.org/NET/ssnx/ssn#Condition", Prefixes.SSN);
		init();
	}

	private void init() {

	}
}
