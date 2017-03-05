package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

/*
 *  THis class maps the ssn:Platform class in the ontology
 *  
 *  An Entity to which other Entities can be attached - particuarly Sensors and other Platforms. 
 *   For example, a post might act as the Platform, a bouy might act as a Platform,
 *   or a fish might act as a Platform for an attached sensor.
 *  
 */

@Component
public class Platform extends Class {

	public Platform() {
		super("Platform", "http://purl.oclc.org/NET/ssnx/ssn#Platform", Prefixes.SSN);
		init();
	}

	private void init() {

	}
}
