package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Prefixes;

/*
 * This Class maps Circle Class in the ontology
 * 
 * Circle coverage it needs the location of the sensor as the centre of the circle and the radius as a DataProperty.
 */

@Component
public class Circle extends Coverage {

	public Circle() {
		super("Circle", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Circle", Prefixes.IOT_LITE);
		init();
	}

	private void init() {

	}

}
