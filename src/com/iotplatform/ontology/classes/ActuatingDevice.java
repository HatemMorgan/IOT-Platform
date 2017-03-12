package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Prefixes;

/*
 * This class maps the iot-lite:ActuatingDevice class in the ontology
 * 
 * Device that can actuate over an object or QuantityKind.
 */

@Component
public class ActuatingDevice extends Device {

	public ActuatingDevice() {
		super("ActuatingDevice", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#ActuatingDevice", Prefixes.IOT_LITE);
		
	}

}
