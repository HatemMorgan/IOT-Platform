package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

/*
 * This Class maps iot-lite:Coverage Class in the ontology
 * 
 * The coverage of an IoT device (i.e. a temperature sensor inside a room has a coverage of that room).
 */

@Component
public class Coverage extends Class {

	public Coverage() {
		super("Coverage", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Coverage", Prefixes.IOT_LITE);
		init();
	}

	public Coverage(String name, String uri, Prefixes prefix) {
		super(name, uri, prefix);
		init();
	}

	private void init() {

	}
}
