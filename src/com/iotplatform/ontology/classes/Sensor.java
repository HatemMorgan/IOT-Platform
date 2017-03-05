package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Prefixes;

/*
 *   This class maps the ssn:Sensor class in the ontology
 *   
 *   A sensor can do (implements) sensing: that is, a sensor is any entity that can follow a sensing method
 *   and thus observe some Property of a FeatureOfInterest. 
 *   Sensors may be physical devices, computational methods, a laboratory setup with a person following a method,
 *   or any other thing that can follow a Sensing Method to observe a Property.
 */

@Component
public class Sensor extends Device {

	public Sensor() {
		super("Sensor", "http://purl.oclc.org/NET/ssnx/ssn#Sensor", Prefixes.SSN);
		init();
	}

	public Sensor(String name, String uri, Prefixes prefix) {
		super(name, uri, prefix);
		init();

	}

	private void init() {

	}
}
