package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

/*
 * This Class maps the Process Class in the ontology
 * 
 * A process has an output and possibly inputs and, for a composite process, describes the temporal and 
 * dataflow dependencies and relationships amongst its parts. [SSN XG]
 */

@Component
public class Process extends Class {

	public Process(String name, String uri, Prefixes prefix) {
		super("Process", "http://purl.oclc.org/NET/ssnx/ssn#Process", Prefixes.SSN);
		init();
	}

	public Process() {
		super("Process", "http://purl.oclc.org/NET/ssnx/ssn#Process", Prefixes.SSN);
		init();
	}

	private void init() {

	}
}
