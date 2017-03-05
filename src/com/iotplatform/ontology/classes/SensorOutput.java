package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

/*
 *  This Class maps ssn:SensorOutput class in the ontology
 *  
 *  A sensor outputs a piece of information (an observed value), the value itself being represented
 *  by an ObservationValue.
 */

@Component
public class SensorOutput extends Class {

	public SensorOutput() {
		super("SensorOutput", " http://purl.oclc.org/NET/ssnx/ssn#SensorOutput", Prefixes.SSN);
		init();
	}

	private void init() {

	}

}
