package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

/*
 * This Class maps iot-platform:Amount class in the main ontology
 * 
 * Describes the amount of of a ssn:MeasurementProperty or ssn:OperatingProperty
 */

@Component
public class Amount extends Class {

	public Amount() {
		super("Amount", "http://iot-platform#Amount", Prefixes.IOT_PLATFORM);

		init();
	}

	private void init() {

	}

}
