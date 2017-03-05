package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Prefixes;

/*
 * This class maps the iot-lite:TagDevice class in the ontology
 * 
 * Tag Device such as QR code or bar code.
 */

@Component
public class TagDevice extends Device {

	public TagDevice() {
		super("TagDevice", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#TagDevice", Prefixes.IOT_LITE);
		init();
	}

	private void init() {

	}
}
