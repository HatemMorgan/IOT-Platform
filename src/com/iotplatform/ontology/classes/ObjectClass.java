package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

/*
 *  This Class maps the Object class in the ontology
 *  
 *  An Object or IoT entity that represent the place.  (i.e. room, car, table)
 */

@Component
public class ObjectClass extends Class {

	public ObjectClass(String name, String uri, Prefixes prefix) {
		super("Object", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Object", Prefixes.IOT_LITE);
		init();
	}

	private void init() {

	}

}
