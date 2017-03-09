package com.iotplatform.ontology.classes;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

/*
 * This class maps the DeviceModule class in the ontology
 * 
 * DeviceModule is a module that consists of one or mix of devices (communicating Device, Tag Device,
 *  ActuatingDevice, CommunicatingDevice)
 */

public class DeviceModule extends Class {

	private static DeviceModule deviceModuleInstance;

	public DeviceModule() {
		super("DeviceModule", "http://iot-platform#DeviceModule", Prefixes.IOT_PLATFORM);
		init();
	}

	/*
	 * String nothing parameter is added for overloading constructor technique
	 * because I need to initialize an instance without having properties and it
	 * will be always passed by null
	 */
	public DeviceModule(String nothing) {
		super("DeviceModule", "http://iot-platform#DeviceModule", Prefixes.IOT_PLATFORM);
	}

	public synchronized static DeviceModule getDeviceModuleInstance() {
		if (deviceModuleInstance == null)
			deviceModuleInstance = new DeviceModule();

		return deviceModuleInstance;
	}

	private void init() {

	}

}
