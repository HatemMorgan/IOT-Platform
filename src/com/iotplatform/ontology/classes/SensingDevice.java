package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Prefixes;

/*
 *  This class maps the ssn:SensingDevice class in the ontology
 *  
 *  A sensing device is a device that implements sensing. 
 */

@Component
public class SensingDevice extends Sensor {

	public SensingDevice() {
		super("SensingDevice", "http://purl.oclc.org/NET/ssnx/ssn#SensingDevice", Prefixes.SSN);
		init();
	}

	private void init() {

	}
	
}
