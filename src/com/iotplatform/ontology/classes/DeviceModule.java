package com.iotplatform.ontology.classes;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.XSDDataTypes;

/*
 * This class maps the DeviceModule class in the ontology
 * 
 * DeviceModule is a module that consists of one or mix of devices (communicating Device, Tag Device,
 *  ActuatingDevice, CommunicatingDevice)
 */

public class DeviceModule extends Class {

	private static DeviceModule deviceModuleInstance;
	private Class deviceModuleSubjectClassInstance;

	public DeviceModule() {
		super("DeviceModule", "http://iot-platform#DeviceModule", Prefix.IOT_PLATFORM, null, true);
		init();
	}

	private Class getDeviceModuleSubjectClassInstance() {
		if (deviceModuleSubjectClassInstance == null)
			deviceModuleSubjectClassInstance = new Class("DeviceModule", "http://iot-platform#DeviceModule",
					Prefix.IOT_PLATFORM, null, true);

		return deviceModuleSubjectClassInstance;
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
		super.getProperties().put("id", new DataTypeProperty(getDeviceModuleSubjectClassInstance(), "id",
				Prefix.IOT_LITE, XSDDataTypes.string_typed, false, false));

		/*
		 * Describes relation between Device module and its device
		 * Components(eg: communicatingDevice,Sensor etc.) and it is one to many
		 * relationship
		 */
		super.getProperties().put("hasDevice", new ObjectProperty(getDeviceModuleSubjectClassInstance(), "hasDevice",
				Prefix.IOT_PLATFORM, Device.getDeviceInstance(), true, false));

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
		super.getProperties().put("onPlatform", new ObjectProperty(getDeviceModuleSubjectClassInstance(), "onPlatform",
				Prefix.SSN, Platform.getPlatformInstance(), false, false));

		super.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "id", "id");
		super.getHtblPropUriName().put(Prefix.IOT_PLATFORM.getUri() + "hasDevice", "hasDevice");
		super.getHtblPropUriName().put(Prefix.SSN.getUri() + "onPlatform", "onPlatform");

	}

}
