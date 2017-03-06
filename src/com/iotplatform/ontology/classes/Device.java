package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Prefixes;

/*
 * This class maps the ssn:Device class in the ontology
 * 
 * A device is a physical piece of technology - a system in a box. 
 * Devices may of course be built of smaller devices and software components (i.e. systems have components).
 */

@Component
public class Device extends SystemClass {

	private static Device deviceInstance;

	public Device() {
		super("Device", "http://purl.oclc.org/NET/ssnx/ssn#Device", Prefixes.SSN);
		init();
	}

	public Device(String name, String uri, Prefixes prefix) {
		super(name, uri, prefix);
		init();
	}

	public Device(String nothing) {
		super("Device", "http://purl.oclc.org/NET/ssnx/ssn#Device", Prefixes.SSN);
	}

	public synchronized static Device getDeviceInstance() {
		if (deviceInstance == null)
			deviceInstance = new Device(null);

		return deviceInstance;
	}

	private void init() {
		
		/*
		 * add ssn:System in the superClass list of ssn:Device class
		 */
		this.getSuperClassesList().add(SystemClass.getSystemInstance());
		
		
		

	}

}
