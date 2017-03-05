package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Prefixes;

/*
 * This class maps ssn:SurvivalRange class in the ontology
 * 
 * The conditions a sensor can be exposed to without damage: 
 * i.e., the sensor continues to operate as defined using MeasurementCapability. 
 * If, however, the SurvivalRange is exceeded, the sensor is 'damaged' and MeasurementCapability
 *  specifications may no longer hold.
 */

@Component
public class SurvivalRange extends Property {

	public SurvivalRange() {
		super("SurvivalRange", "http://purl.oclc.org/NET/ssnx/ssn#SurvivalRange", Prefixes.SSN);
		init();
	}

	private void init() {

	}
}
