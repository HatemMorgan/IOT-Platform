package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

/*
 * This Class maps iot-lite:Service Class in the ontology
 * 
 * Service provided by an IoT Device
 * 
 */

@Component
public class Service extends Class {

	public Service() {
		super("Service", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Service", Prefixes.IOT_LITE);
		init();
	}

	private void init() {

	}
}
