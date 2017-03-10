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
 *  
 *  It is a wrapper for condition and survival property and it is used by System Class to describe that in certain
 *  condition there is a survivalProperty instance which has a value or a range
 */

@Component
public class SurvivalRange extends Property {

	private static SurvivalRange survivalRangeInstance;

	public SurvivalRange() {
		super("SurvivalRange", "http://purl.oclc.org/NET/ssnx/ssn#SurvivalRange", Prefixes.SSN);
		init();
	}

	/*
	 * String nothing parameter is added for overloading constructor technique
	 * because I need to initialize an instance without having properties and it
	 * will be always passed by null
	 */
	public SurvivalRange(String nothing) {
		super("SurvivalRange", "http://purl.oclc.org/NET/ssnx/ssn#SurvivalRange", Prefixes.SSN);
	}

	public synchronized static SurvivalRange getSurvivalRangeInstance() {
		if (survivalRangeInstance == null)
			survivalRangeInstance = new SurvivalRange(null);

		return survivalRangeInstance;
	}

	private void init() {

	}
}
