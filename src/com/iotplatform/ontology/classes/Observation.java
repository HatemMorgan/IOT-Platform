package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

/*
 * This Class maps the Observation Class in the ontology
 * 
 * An Observation is made by a sensor and it is a Situation in which a Sensing method has been used to estimate or calculate 
 * a value of a Property of a FeatureOfInterest.  Links to Sensing and Sensor describe what made the Observation
 *  and how; links to Property and Feature detail what was sensed; the result is the output of a Sensor; 
 *  other metadata details times etc.
 */

@Component
public class Observation extends Class {

	public Observation() {
		super("Observation", "http://purl.oclc.org/NET/ssnx/ssn#Observation", Prefixes.SSN);
		init();
	}

	private void init() {

	}

}
