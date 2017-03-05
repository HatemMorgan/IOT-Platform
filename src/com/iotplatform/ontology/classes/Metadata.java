package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

/*
 *  This Class maps MetaData Class in the ontology
 *  
 *  Class used to describe properties that cannot be described by QuantityKind and Units.
 *   i.e. the resolution of a sensor.
 */

@Component
public class Metadata extends Class {

	public Metadata() {
		super("Metadata", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Metadata", Prefixes.IOT_LITE);
		init();
	}

	private void init() {

	}

}
