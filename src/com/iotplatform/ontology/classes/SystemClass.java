package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

/*
 *  This class maps the ssn:System class in the ontology 
 * 
 * System is the superClass of device class 
 * 
 * A system can be a SmartCampus of example
 * 
 * System is a unit of abstraction for pieces of infrastructure (and we largely care that they are) for sensing. 
 * A system has components, its subsystems, which are other systems.
 */

@Component
public class SystemClass extends Class {

	private static SystemClass systemInstance;

	public SystemClass() {
		super("System", "http://purl.oclc.org/NET/ssnx/ssn#System", Prefixes.SSN);

		init();

	}

	public SystemClass(String name, String uri, Prefixes prefix) {
		super(name, uri, prefix);
		init();
	}

	/*
	 * String nothing parameter is added for overloading constructor technique
	 * because I need to initialize an instance without having properties and it
	 * will be always passed by null
	 */
	public SystemClass(String nothing) {

		super("System", "http://purl.oclc.org/NET/ssnx/ssn#System", Prefixes.SSN);
	}

	public synchronized static SystemClass getSystemInstance() {
		if (systemInstance == null) {
			systemInstance = new SystemClass(null);
		}

		return systemInstance;

	}

	private void init() {

		/*
		 * id which must be unique
		 */
		super.getProperties().put("id",
				new DataTypeProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, true));

		/*
		 * relation between a system and its parts. A system or its subclasses
		 * can have many subsystems so multipleValues is enabled (one to many
		 * relation)
		 */
		super.getProperties().put("hasSubSystem",
				new ObjectProperty("hasSubSystem", Prefixes.SSN, SystemClass.getSystemInstance(), true, false));

		/*
		 * Relation between a System and a Deployment, recording that the
		 * System/Sensor was deployed in that Deployment. A deployment will have
		 * a name and it will be one to one relation because a senesor or a
		 * system cannot be deployed in two places
		 */

		super.getProperties().put("hasDeployment",
				new ObjectProperty("hasDeployment", Prefixes.SSN, Deployment.getDeploymentInstance(), false, false));

		/*
		 * Relation between a System (e.g., a Sensor) and a Platform. The
		 * relation locates the sensor relative to other described entities
		 * entities: i.e., the Sensor s1's location is Platform p1. More precise
		 * locations for sensors in space (relative to other entities, where
		 * attached to another entity, or in 3D space) are made using DOLCE's
		 * Regions (SpaceRegion). It is one to one relation
		 */
		super.getProperties().put("onPlatform",
				new ObjectProperty("onPlatform", Prefixes.SSN, Platform.getPlatformInstance(), false, false));

		/*
		 * A Relation from a System to a SurvivalRange. It is a one to one
		 * relationShip because a system/device has only one survivalRange
		 */
		super.getProperties().put("hasSurvivalRange", new ObjectProperty("hasSurvivalRange", Prefixes.SSN,
				SurvivalRange.getSurvivalRangeInstance(), false, false));

		/*
		 * Relation from a System to an OperatingRange describing the normal
		 * operating environment of the System. It is one to one relationShip
		 * because a system/device has only one operatingRange
		 */
		super.getProperties().put("hasOperatingRange", new ObjectProperty("hasOperatingRange", Prefixes.SSN,
				OperatingRange.getOperatingRangeInstance(), false, false));

		super.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");
		super.getHtblPropUriName().put(Prefixes.SSN.getUri() + "hasSubSystem", "hasSubSystem");
		super.getHtblPropUriName().put(Prefixes.SSN.getUri() + "hasDeployment", "hasDeployment");
		super.getHtblPropUriName().put(Prefixes.SSN.getUri() + "onPlatform", "onPlatform");
		super.getHtblPropUriName().put(Prefixes.SSN.getUri() + "hasSurvivalRange", "hasSurvivalRange");
		super.getHtblPropUriName().put(Prefixes.SSN.getUri() + "hasOperatingRange", "hasOperatingRange");

	}

	// public static void main(String[] args) {
	// SystemClass system = new SystemClass();
	// System.out.println(system.getProperties().toString());
	// }
}
