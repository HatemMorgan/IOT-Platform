package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

/*
 * This class maps the iot-platform:IOTSystem class in the ontology
 * 
 * Describes an IOT system eg: Smart Campus, Smart City. The IOT System consists of one or more Device module.
 */

@Component
public class IOTSystem extends Class {

	private static IOTSystem iotSystemInstance;

	public IOTSystem() {
		super("IOTSystem", "http://iot-platform#IOTSystem", Prefixes.IOT_PLATFORM);
		init();
	}

	/*
	 * String nothing parameter is added for overloading constructor technique
	 * because I need to initialize an instance without having properties and it
	 * will be always passed by null
	 */
	public IOTSystem(String nothing) {
		super("IOTSystem", "http://iot-platform#IOTSystem", Prefixes.IOT_PLATFORM);

	}

	public synchronized static IOTSystem getIOTSystemInstance() {
		if (iotSystemInstance == null)
			iotSystemInstance = new IOTSystem(null);

		return iotSystemInstance;
	}

	private void init() {

		// IOT System Name which must be unique

		super.getProperties().put("name",
				new DataTypeProperty("name", Prefixes.FOAF, XSDDataTypes.string_typed, false, true));

		/*
		 * IOT System Description
		 */
		super.getProperties().put("description",
				new DataTypeProperty("description", Prefixes.IOT_PLATFORM, XSDDataTypes.string_typed, false, false));

		/*
		 * Describes the relation between an IOTSystem and DeviceModule.
		 * 
		 * An IOTSystem instance can have more than one DeviceModule (one to
		 * many relationship)
		 */
		super.getProperties().put("hasDeviceModule", new ObjectProperty("hasDeviceModule", Prefixes.IOT_PLATFORM,
				DeviceModule.getDeviceModuleInstance(), true, false));

		super.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "name", "name");
		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "description", "description");
		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "hasDeviceModule", "hasDeviceModule");

	}

}
