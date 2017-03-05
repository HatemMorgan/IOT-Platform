package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Prefixes;

/*
 *  This Class maps the Polygon class in the ontology
 *  
 *  The coverage is made up by linking several points by strait lines.
 */

@Component
public class Polygon extends Coverage {

	public Polygon() {
		super("Polygon", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Polygon", Prefixes.IOT_LITE);
		init();
	}

	private void init() {

	}

}
