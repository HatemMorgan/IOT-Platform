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

	private static CommunicatingDevice communicatingDeviceInstance;

	public CommunicatingDevice() {
		super("CommunicatingDevice", "http://iot-platform#CommunicatingDevice", Prefixes.IOT_PLATFORM);
		init();
	}

	/*
	 * String nothing parameter is added for overloading constructor technique
	 * because I need to initialize an instance without having properties and it
	 * will be always passed by null
	 */
	public CommunicatingDevice(String nothing) {
		super("CommunicatingDevice", "http://iot-platform#CommunicatingDevice", Prefixes.IOT_PLATFORM);
	}

	public synchronized static CommunicatingDevice getCommunicatingDeviceInstance() {
		if (communicatingDeviceInstance == null)
			communicatingDeviceInstance = new CommunicatingDevice(null);

		return communicatingDeviceInstance;
	}

	private void init() {

	}
}
