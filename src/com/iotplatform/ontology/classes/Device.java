package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.ObjectProperty;
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

		// Adding Class properties

		/*
		 * Links the devices with their coverages. It is one to one relationShip
		 * because a device can have only on coverage
		 */
		this.getProperties().put("hasCoverage",
				new ObjectProperty("hasCoverage", Prefixes.IOT_LITE, Coverage.getCoverageInstance(), false, false));

		/*
		 * A device has a service.
		 */
		this.getProperties().put("hasService",
				new ObjectProperty("hasService", Prefixes.IOT_PLATFORM, Service.getServiceInstance(), false, false));

		super.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "hasCoverage", "hasCoverage");
		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "hasService", "hasService");

	}

}
