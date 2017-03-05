package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

/*
 *  This Class maps the Input Class in the ontology 
 *  
 *  Any information that is provided to a process for its use [MMI OntDev]
 */

@Component
public class Input extends Class {

	public Input() {
		super("Input", "http://purl.oclc.org/NET/ssnx/ssn#Input", Prefixes.SSN);
		init();
	}

	private void init() {

	}

}
