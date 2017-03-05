package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Prefixes;

/*
 * This class maps the Rectangle Class in the ontology
 * 
 * Teh coverage is made up by giving two points which are the oposite corners of a rentangle.
 */

@Component
public class Rectangle extends Coverage {

	public Rectangle() {
		super("Rectangle", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Rectangle", Prefixes.IOT_LITE);
		init();
	}

	private void init() {

	}

}
