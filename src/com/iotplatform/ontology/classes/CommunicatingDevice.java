package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Prefixes;

/*
 *  This class maps the iot-platform:CommunicatingDevice class in the ontology
 *
 *  Describes communicating device which allow communication of a device with the outer world . 
 *  ie: Zegbee, BLE and WIFI
 */

@Component
public class CommunicatingDevice extends Device {

	public CommunicatingDevice() {
		super("CommunicatingDevice", "http://iot-platform#CommunicatingDevice", Prefixes.IOT_PLATFORM);
		init();
	}

	private void init() {

	}
}
