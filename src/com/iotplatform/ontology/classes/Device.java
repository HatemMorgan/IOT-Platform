package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Prefixes;

/*
 * This class maps the ssn:Device class in the ontology
 * 
 * A device is a physical piece of technology - a system in a box. 
 * Devices may of course be built of smaller devices and software components (i.e. systems have components).
 */

@Component
public class Device extends SystemClass {

	public Device() {
		super("Device", "http://purl.oclc.org/NET/ssnx/ssn#Device", Prefixes.SSN);
		init();
	}

	public Device(String name, String uri, Prefixes prefix) {
		super(name, uri, prefix);
		init();
	}

	private void init() {

	}

}
