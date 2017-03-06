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

	private static Coverage coverageInstance;

	public Coverage() {
		super("Coverage", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Coverage", Prefixes.IOT_LITE);
		init();
	}

	public Coverage(String name, String uri, Prefixes prefix) {
		super(name, uri, prefix);
		init();
	}

	/*
	 * String nothing parameter is added for overloading constructor technique
	 * because I need to initialize an instance without having properties and it
	 * will be always passed by null
	 */
	public Coverage(String nothing) {
		super("Coverage", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Coverage", Prefixes.IOT_LITE);

	}

	public synchronized static Coverage getCoverageInstance() {
		if (coverageInstance == null)
			coverageInstance = new Coverage(null);

		return coverageInstance;
	}

	private void init() {

	}
}
