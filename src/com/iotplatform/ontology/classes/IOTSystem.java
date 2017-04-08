package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.XSDDataTypes;

/*
 * This class maps the iot-platform:IOTSystem class in the ontology
 * 
 * Describes an IOT system eg: Smart Campus, Smart City. The IOT System consists of one or more Device module.
 */

@Component
public class IOTSystem extends Class {

	private static IOTSystem iotSystemInstance;
	private Class iotSystemSubjectClassInstance;

	public IOTSystem() {
		super("IOTSystem", "http://iot-platform#IOTSystem", Prefix.IOT_PLATFORM, "name", true);

		init();
	}

	private Class getIotSystemSubjectClassInstance() {
		if (iotSystemSubjectClassInstance == null)
			iotSystemSubjectClassInstance = new Class("IOTSystem", "http://iot-platform#IOTSystem",
					Prefix.IOT_PLATFORM, "name", true);

		return iotSystemSubjectClassInstance;
	}

	public synchronized static IOTSystem getIOTSystemInstance() {
		if (iotSystemInstance == null)
			iotSystemInstance = new IOTSystem();

		return iotSystemInstance;
	}

	private void init() {

		// IOT System Name which must be unique

		super.getProperties().put("name", new DataTypeProperty(getIotSystemSubjectClassInstance(), "name",
				Prefix.FOAF, XSDDataTypes.string_typed, false, true));

		/*
		 * IOT System Description
		 */
		super.getProperties().put("description", new DataTypeProperty(getIotSystemSubjectClassInstance(), "description",
				Prefix.IOT_PLATFORM, XSDDataTypes.string_typed, false, false));

		/*
		 * Describes the relation between an IOTSystem and DeviceModule.
		 * 
		 * An IOTSystem instance can have more than one DeviceModule (one to
		 * many relationship)
		 */
		super.getProperties().put("hasDeviceModule", new ObjectProperty(getIotSystemSubjectClassInstance(),
				"hasDeviceModule", Prefix.IOT_PLATFORM, DeviceModule.getDeviceModuleInstance(), true, false));

		/*
		 * Relation between a IOTSystem and a Deployment, recording that the
		 * IOTSystem was deployed in that Deployment. and it one to one
		 * relationshiip
		 */

		super.getProperties().put("hasDeployment", new ObjectProperty(getIotSystemSubjectClassInstance(),
				"hasDeployment", Prefix.SSN, Deployment.getDeploymentInstance(), false, false));

		super.getHtblPropUriName().put(Prefix.FOAF.getUri() + "name", "name");
		super.getHtblPropUriName().put(Prefix.IOT_PLATFORM.getUri() + "description", "description");
		super.getHtblPropUriName().put(Prefix.IOT_PLATFORM.getUri() + "hasDeviceModule", "hasDeviceModule");
		super.getHtblPropUriName().put(Prefix.SSN.getUri() + "hasDeployment", "hasDeployment");

	}

}
