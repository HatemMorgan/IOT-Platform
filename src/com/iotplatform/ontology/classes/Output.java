package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

/*
 * This class maps the output class in the ontology
 * 
 * Any information that is reported from a process. [MMI OntDev]
 */

@Component
public class Output extends Class {

	public Output(String name, String uri, Prefixes prefix) {
		super("Output", "http://purl.oclc.org/NET/ssnx/ssn#Output", Prefixes.SSN);
		init();
	}

	private void init() {

	}

}
