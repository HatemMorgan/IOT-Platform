package com.iotplatform.ontology.classes;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

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

		/*
		 * DeviceModule id which must be unique
		 */
		super.getProperties().put("id",
				new DataTypeProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, true));

		/*
		 * Describes relation between Device module and its device
		 * Components(eg: communicatingDevice,Sensor etc.) and it is one to many
		 * relationship
		 */
		super.getProperties().put("hasDevice",
				new ObjectProperty("hasDevice", Prefixes.IOT_PLATFORM, Device.getDeviceInstance(), true, false));

		/*
		 * Relation between a DeviceModule (e.g., A module consists of one or
		 * more devices ) and a Platform. The relation locates the DeviceModule
		 * relative to other described entities entities: i.e., the DeviceModule
		 * d1's location is Platform p1. More precise locations for DeviceModule
		 * in space (relative to other entities, where attached to another
		 * entity, or in 3D space) are made using DOLCE's Regions (SpaceRegion).
		 * 
		 * One to one relationShip but the platform can has more than one
		 * DeviceModule so onPlatform has to have a uniqueValue
		 * 
		 */
		super.getProperties().put("onPlatform",
				new ObjectProperty("onPlatform", Prefixes.SSN, Platform.getPlatformInstance(), false, false));

		super.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");
		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "hasDevice", "hasDevice");
		super.getHtblPropUriName().put(Prefixes.SSN.getUri() + "onPlatform", "onPlatform");

	}

}
