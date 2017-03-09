package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

/*
 * This Class maps Attribute Class in the ontology
 * 
 *  An attribute of an IoT object that can be exposed by an IoT service (i.e. a room (IoT Object) 
 *  has a temperature (Attribute), that can be exposed by a temperature sensor (IoT device).
 */

@Component
public class Attribute extends Class {

	public Attribute() {
		super("Attribute", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Attribute", Prefixes.IOT_LITE);
		init();
	}

	private void init() {

	}

}
