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
 * System is the superClass of device class and it has properties that describes any system
 * (eg:device like what our ontology has)
 * 
 * hasOperatingRange and hasSurvivalRange Properties describes that the system has and operatingRange 
 * or survivalRage that under certain conditions will have a specified survivalProperty or operatingProperty
 * 
 * so SurvivalRange and OperatingRange classes act like a wrapper for a condition and the property triggered 
 * 
 * System is a unit of abstraction for pieces of infrastructure (and we largely care that they are) for sensing. 
 * A system has components, its subsystems, which are other systems.
 * 
 * 
 */


@Component
public class SystemClass extends Class {

	private static SystemClass systemInstance;

	public SystemClass() {
		super("System", "http://purl.oclc.org/NET/ssnx/ssn#System", Prefixes.SSN,null);

		init();

	}

	/*
	 * This constructor is used to perform overloading constructor technique and
	 * the parameter String nothing will be passed always with null
	 * 
	 * I have done this overloaded constructor to instantiate the static
	 * systemInstance to avoid java.lang.StackOverflowError exception that Occur
	 * when calling init() to add properties to systemInstance
	 * 
	 */
	public SystemClass(String nothing) {
		super("System", "http://purl.oclc.org/NET/ssnx/ssn#System", Prefixes.SSN,null);
	}

	public SystemClass(String name, String uri, Prefixes prefix) {
		super(name, uri, prefix,null);
		init();
	}

	public synchronized static SystemClass getSystemInstance() {
		if (systemInstance == null) {
			systemInstance = new SystemClass(null);
			initSystemStaticInstance(systemInstance);
		}

		return systemInstance;

	}

	private void init() {

		/*
		 * id which must be unique
		 */
		super.getProperties().put("id",
				new DataTypeProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, false));

		/*
		 * relation between a system and its parts. A system or its subclasses
		 * can have many subsystems so multipleValues is enabled (one to many
		 * relation)
		 */
		super.getProperties().put("hasSubSystem",
				new ObjectProperty("hasSubSystem", Prefixes.SSN, SystemClass.getSystemInstance(), true, false));

		/*
		 * A Relation from a System to a SurvivalRange. It is a one to one
		 * relationShip because a system/device has only one survivalRange
		 */
		super.getProperties().put("hasSurvivalRange", new ObjectProperty("hasSurvivalRange", Prefixes.SSN,
				SurvivalRange.getSurvivalRangeInstance(), true, false));

		/*
		 * Relation from a System to an OperatingRange describing the normal
		 * operating environment of the System. It is one to one relationShip
		 * because a system/device has only one operatingRange
		 */
		super.getProperties().put("hasOperatingRange", new ObjectProperty("hasOperatingRange", Prefixes.SSN,
				OperatingRange.getOperatingRangeInstance(), true, false));

		super.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");
		super.getHtblPropUriName().put(Prefixes.SSN.getUri() + "hasSubSystem", "hasSubSystem");
		super.getHtblPropUriName().put(Prefixes.SSN.getUri() + "hasSurvivalRange", "hasSurvivalRange");
		super.getHtblPropUriName().put(Prefixes.SSN.getUri() + "hasOperatingRange", "hasOperatingRange");

	}

	public static void initSystemStaticInstance(SystemClass systemInstance) {
		/*
		 * id which must be unique
		 */
		systemInstance.getProperties().put("id",
				new DataTypeProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, true));

		/*
		 * relation between a system and its parts. A system or its subclasses
		 * can have many subsystems so multipleValues is enabled (one to many
		 * relation)
		 */
		systemInstance.getProperties().put("hasSubSystem",
				new ObjectProperty("hasSubSystem", Prefixes.SSN, SystemClass.getSystemInstance(), true, false));

		/*
		 * A Relation from a System to a SurvivalRange. It is a one to one
		 * relationShip because a system/device has only one survivalRange
		 */
		systemInstance.getProperties().put("hasSurvivalRange", new ObjectProperty("hasSurvivalRange", Prefixes.SSN,
				SurvivalRange.getSurvivalRangeInstance(), true, false));

		/*
		 * Relation from a System to an OperatingRange describing the normal
		 * operating environment of the System. It is one to one relationShip
		 * because a system/device has only one operatingRange
		 */
		systemInstance.getProperties().put("hasOperatingRange", new ObjectProperty("hasOperatingRange", Prefixes.SSN,
				OperatingRange.getOperatingRangeInstance(), true, false));

		systemInstance.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");
		systemInstance.getHtblPropUriName().put(Prefixes.SSN.getUri() + "hasSubSystem", "hasSubSystem");
		systemInstance.getHtblPropUriName().put(Prefixes.SSN.getUri() + "hasSurvivalRange", "hasSurvivalRange");
		systemInstance.getHtblPropUriName().put(Prefixes.SSN.getUri() + "hasOperatingRange", "hasOperatingRange");
	}

	// public static void main(String[] args) {
	// SystemClass system = new SystemClass();
	// System.out.println(system.getProperties().toString());
	// }
}
