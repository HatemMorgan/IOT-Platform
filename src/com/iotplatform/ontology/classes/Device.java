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

	/*
	 * String nothing parameter is added for overloading constructor technique
	 * because I need to initialize an instance without having properties and it
	 * will be always passed by null
	 */
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
		super.getSuperClassesList().add(SystemClass.getSystemInstance());

		// Adding Class properties

		/*
		 * Links the devices with their coverages. It is one to one relationShip
		 * because a device can have only on coverage
		 */
		super.getProperties().put("hasCoverage",
				new ObjectProperty("hasCoverage", Prefixes.IOT_LITE, Coverage.getCoverageInstance(), false, false));

		/*
		 * A device has a service.
		 */
		super.getProperties().put("exposedBy",
				new ObjectProperty("exposedBy", Prefixes.IOT_LITE, Service.getServiceInstance(), false, false));

		/*
		 * Links a Device with custom metadata about that device.
		 */
		super.getProperties().put("hasMetadata",
				new ObjectProperty("hasMetadata", Prefixes.IOT_LITE, Metadata.getMetadataInstance(), true, false));

		super.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "hasCoverage", "hasCoverage");
		super.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "exposedBy", "exposedBy");
		super.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "hasMetadata", "hasMetadata");

	}

	// public static void main(String[] args) {
	// Device device = new Device();
	// System.out.println(device.getProperties());
	// }
}
