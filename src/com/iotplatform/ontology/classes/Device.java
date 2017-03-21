package com.iotplatform.ontology.classes;

import java.util.Hashtable;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.Property;

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
		super("Device", "http://purl.oclc.org/NET/ssnx/ssn#Device", Prefixes.SSN, null, true);
		init();
	}

	public Device(String name, String uri, Prefixes prefix, Property uniqueIdentifierProperty, boolean hasTypeClasses) {
		super(name, uri, prefix, uniqueIdentifierProperty, hasTypeClasses);
		init();
	}

	public Device(String nothing) {
		super("Device", "http://purl.oclc.org/NET/ssnx/ssn#Device", Prefixes.SSN, null, true, null);
	}

	public synchronized static Device getDeviceInstance() {
		if (deviceInstance == null) {
			deviceInstance = new Device(null);
			initDeviceStaticInstance(deviceInstance);
			SystemClass.initSystemStaticInstance(deviceInstance);
			initDeviceStaticInstaceTypeClasses(deviceInstance);
		}
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
		this.getProperties().put("exposedBy",
				new ObjectProperty("exposedBy", Prefixes.IOT_LITE, Service.getServiceInstance(), false, false));

		/*
		 * Links a Device with custom metadata about that device.
		 */
		this.getProperties().put("hasMetadata",
				new ObjectProperty("hasMetadata", Prefixes.IOT_LITE, Metadata.getMetadataInstance(), true, false));

		this.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "hasCoverage", "hasCoverage");
		this.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "exposedBy", "exposedBy");
		this.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "hasMetadata", "hasMetadata");

		if (this.isHasTypeClasses()) {
			this.setClassTypesList(new Hashtable<>());
			this.getClassTypesList().put("Sensor", Sensor.getSensorInstance());
			this.getClassTypesList().put("ActuatingDevice", ActuatingDevice.getActuatingDeviceInstance());
			this.getClassTypesList().put("CommunicatingDevice", CommunicatingDevice.getCommunicatingDeviceInstance());
			this.getClassTypesList().put("TagDevice", TagDevice.getTagDeviceInstance());
		}
	}

	public static void initDeviceStaticInstaceTypeClasses(Device deviceInstance) {
		deviceInstance.getClassTypesList().put("Sensor", Sensor.getSensorInstance());
		deviceInstance.getClassTypesList().put("ActuatingDevice", ActuatingDevice.getActuatingDeviceInstance());
		deviceInstance.getClassTypesList().put("CommunicatingDevice",
				CommunicatingDevice.getCommunicatingDeviceInstance());
		deviceInstance.getClassTypesList().put("TagDevice", TagDevice.getTagDeviceInstance());
	}

	public static void initDeviceStaticInstance(Device deviceInstance) {

		/*
		 * add ssn:System in the superClass list of ssn:Device class
		 */
		deviceInstance.getSuperClassesList().add(SystemClass.getSystemInstance());

		// Adding Class properties

		/*
		 * Links the devices with their coverages. It is one to one relationShip
		 * because a device can have only on coverage
		 */
		deviceInstance.getProperties().put("hasCoverage",
				new ObjectProperty("hasCoverage", Prefixes.IOT_LITE, Coverage.getCoverageInstance(), false, false));

		/*
		 * A device has a service.
		 */
		deviceInstance.getProperties().put("exposedBy",
				new ObjectProperty("exposedBy", Prefixes.IOT_LITE, Service.getServiceInstance(), false, false));

		/*
		 * Links a Device with custom metadata about that device.
		 */
		deviceInstance.getProperties().put("hasMetadata",
				new ObjectProperty("hasMetadata", Prefixes.IOT_LITE, Metadata.getMetadataInstance(), true, false));

		deviceInstance.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "hasCoverage", "hasCoverage");
		deviceInstance.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "exposedBy", "exposedBy");
		deviceInstance.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "hasMetadata", "hasMetadata");

	}

	public static void main(String[] args) {
		Device device = new Device();

		System.out.println(device.getProperties().size());
		System.out.println(Device.getDeviceInstance().getProperties().size());

		System.out.println(device.getHtblPropUriName().size());
		System.out.println(Device.getDeviceInstance().getHtblPropUriName().size());

		System.out.println(device.getSuperClassesList());
		System.out.println(Device.getDeviceInstance().getSuperClassesList());

		System.out.println(device.getClassTypesList());
		System.out.println(Device.getDeviceInstance().getClassTypesList());

	}

}
