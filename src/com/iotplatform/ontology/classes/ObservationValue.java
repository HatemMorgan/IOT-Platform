package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

/*
 * This Class maps ObservationValue class in the ontology
 * 
 * The value of the result of an Observation.  An Observation has a result which is the output of some sensor,
 *  the result is an information object that encodes some value for a Feature.
 */

@Component
public class ObservationValue extends Class {

	public ObservationValue(String name, String uri, Prefixes prefix) {
		super("ObservationValue", "http://purl.oclc.org/NET/ssnx/ssn#ObservationValue", Prefixes.SSN);
		init();

	}

	private void init() {

	}

}
