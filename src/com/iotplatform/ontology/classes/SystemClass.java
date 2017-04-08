package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefix;
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
	private Class systemSubjectClassInstance;

	public SystemClass() {
		super("System", "http://purl.oclc.org/NET/ssnx/ssn#System", Prefix.SSN, null, true);

		init();

	}

	private Class getSystemSubjectClassInstance() {
		if (systemSubjectClassInstance == null)
			systemSubjectClassInstance = new Class("System", "http://purl.oclc.org/NET/ssnx/ssn#System", Prefix.SSN,
					null, true);

		return systemSubjectClassInstance;
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
		super("System", "http://purl.oclc.org/NET/ssnx/ssn#System", Prefix.SSN, null, true);
	}

	public SystemClass(String name, String uri, Prefix prefix, String uniqueIdentifierPropertyName,
			boolean hasTypeClasses) {
		super(name, uri, prefix, uniqueIdentifierPropertyName, hasTypeClasses);
		init();
	}

	/*
	 * This constructor is used to perform overloading constructor technique and
	 * the parameter String nothing will be passed always with null and this
	 * constructor is used to initialize staticInstance for Person class
	 * 
	 * I added this overloaded constructor to allow subClasses of SystemClass to
	 * create their own static instance and also reference them here in
	 * typeClassList
	 * 
	 * It cause java.lang.StackOverflowError exception when calling the above
	 * constructor because it calls init method
	 * 
	 */
	public SystemClass(String name, String uri, Prefix prefix, String uniqueIdentifierPropertyName,
			boolean hasTypeClasses, String nothing) {
		super(name, uri, prefix, uniqueIdentifierPropertyName, hasTypeClasses);
	}

	public synchronized static SystemClass getSystemInstance() {
		if (systemInstance == null) {
			systemInstance = new SystemClass(null);
			initSystemStaticInstance(systemInstance);

			if (systemInstance.isHasTypeClasses()) {
				systemInstance.getClassTypesList().put("Device", Device.getDeviceInstance());
				systemInstance.getClassTypesList().putAll(Device.getDeviceInstance().getClassTypesList());
			}
		}

		return systemInstance;

	}

	private void init() {

		/*
		 * id which must be unique
		 */
		this.getProperties().put("id", new DataTypeProperty(getSystemSubjectClassInstance(), "id", Prefix.IOT_LITE,
				XSDDataTypes.string_typed, false, false));

		/*
		 * relation between a system and its parts. A system or its subclasses
		 * can have many subsystems so multipleValues is enabled (one to many
		 * relation)
		 */
		this.getProperties().put("hasSubSystem", new ObjectProperty(getSystemSubjectClassInstance(), "hasSubSystem",
				Prefix.SSN, SystemClass.getSystemInstance(), true, false));

		/*
		 * A Relation from a System to a SurvivalRange. It is a one to one
		 * relationShip because a system/device has only one survivalRange
		 */
		this.getProperties().put("hasSurvivalRange", new ObjectProperty(getSystemSubjectClassInstance(),
				"hasSurvivalRange", Prefix.SSN, SurvivalRange.getSurvivalRangeInstance(), true, false));

		/*
		 * Relation from a System to an OperatingRange describing the normal
		 * operating environment of the System. It is one to one relationShip
		 * because a system/device has only one operatingRange
		 */
		this.getProperties().put("hasOperatingRange", new ObjectProperty(getSystemSubjectClassInstance(),
				"hasOperatingRange", Prefix.SSN, OperatingRange.getOperatingRangeInstance(), true, false));

		this.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "id", "id");
		this.getHtblPropUriName().put(Prefix.SSN.getUri() + "hasSubSystem", "hasSubSystem");
		this.getHtblPropUriName().put(Prefix.SSN.getUri() + "hasSurvivalRange", "hasSurvivalRange");
		this.getHtblPropUriName().put(Prefix.SSN.getUri() + "hasOperatingRange", "hasOperatingRange");

		if (this.isHasTypeClasses()) {
			this.getClassTypesList().put("Device", Device.getDeviceInstance());
			this.getClassTypesList().putAll(Device.getDeviceInstance().getClassTypesList());
		}
	}

	public static void initSystemStaticInstance(SystemClass systemInstance) {
		/*
		 * id which must be unique
		 */
		systemInstance.getProperties().put("id", new DataTypeProperty(systemInstance.getSystemSubjectClassInstance(),
				"id", Prefix.IOT_LITE, XSDDataTypes.string_typed, false, true));

		/*
		 * relation between a system and its parts. A system or its subclasses
		 * can have many subsystems so multipleValues is enabled (one to many
		 * relation)
		 */
		systemInstance.getProperties().put("hasSubSystem",
				new ObjectProperty(systemInstance.getSystemSubjectClassInstance(), "hasSubSystem", Prefix.SSN,
						SystemClass.getSystemInstance(), true, false));

		/*
		 * A Relation from a System to a SurvivalRange. It is a one to one
		 * relationShip because a system/device has only one survivalRange
		 */
		systemInstance.getProperties().put("hasSurvivalRange",
				new ObjectProperty(systemInstance.getSystemSubjectClassInstance(), "hasSurvivalRange", Prefix.SSN,
						SurvivalRange.getSurvivalRangeInstance(), true, false));

		/*
		 * Relation from a System to an OperatingRange describing the normal
		 * operating environment of the System. It is one to one relationShip
		 * because a system/device has only one operatingRange
		 */
		systemInstance.getProperties().put("hasOperatingRange",
				new ObjectProperty(systemInstance.getSystemSubjectClassInstance(), "hasOperatingRange", Prefix.SSN,
						OperatingRange.getOperatingRangeInstance(), true, false));

		systemInstance.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "id", "id");
		systemInstance.getHtblPropUriName().put(Prefix.SSN.getUri() + "hasSubSystem", "hasSubSystem");
		systemInstance.getHtblPropUriName().put(Prefix.SSN.getUri() + "hasSurvivalRange", "hasSurvivalRange");
		systemInstance.getHtblPropUriName().put(Prefix.SSN.getUri() + "hasOperatingRange", "hasOperatingRange");

	}

	public static void main(String[] args) {
		SystemClass system = new SystemClass();
		System.out.println(system.getProperties().toString());
		System.out.println(system.getClassTypesList());
		System.out.println(system.getSuperClassesList());
		System.out.println(system.getHtblPropUriName());

		System.out.println("==================================================================");

		System.out.println(SystemClass.getSystemInstance().getProperties().toString());
		System.out.println(SystemClass.getSystemInstance().getClassTypesList());
		System.out.println(SystemClass.getSystemInstance().getHtblPropUriName());
		System.out.println(SystemClass.getSystemInstance().getSuperClassesList());

		System.out.println("===========================Device==========================================");

		System.out.println(system.getClassTypesList().get("Device").getProperties().toString());
		System.out.println(system.getClassTypesList().get("Device").getClassTypesList());
		System.out.println(system.getClassTypesList().get("Device").getSuperClassesList());
		System.out.println(system.getClassTypesList().get("Device").getHtblPropUriName());

		System.out.println("==================================================================");

		System.out
				.println(SystemClass.getSystemInstance().getClassTypesList().get("Device").getProperties().toString());
		System.out.println(SystemClass.getSystemInstance().getClassTypesList().get("Device").getClassTypesList());
		System.out.println(SystemClass.getSystemInstance().getClassTypesList().get("Device").getHtblPropUriName());
		System.out.println(SystemClass.getSystemInstance().getClassTypesList().get("Device").getSuperClassesList());

		System.out.println("===========================Sensor==========================================");

		System.out.println(system.getClassTypesList().get("Sensor").getProperties().toString());
		System.out.println(system.getClassTypesList().get("Sensor").getClassTypesList());
		System.out.println(system.getClassTypesList().get("Sensor").getSuperClassesList());
		System.out.println(system.getClassTypesList().get("Sensor").getHtblPropUriName());

		System.out.println("==================================================================");

		System.out
				.println(SystemClass.getSystemInstance().getClassTypesList().get("Sensor").getProperties().toString());
		System.out.println(SystemClass.getSystemInstance().getClassTypesList().get("Sensor").getClassTypesList());
		System.out.println(SystemClass.getSystemInstance().getClassTypesList().get("Sensor").getHtblPropUriName());
		System.out.println(SystemClass.getSystemInstance().getClassTypesList().get("Sensor").getSuperClassesList());

		System.out.println("===========================ActuatingDevice==========================================");

		System.out.println(system.getClassTypesList().get("ActuatingDevice").getProperties().toString());
		System.out.println(system.getClassTypesList().get("ActuatingDevice").getClassTypesList());
		System.out.println(system.getClassTypesList().get("ActuatingDevice").getSuperClassesList());
		System.out.println(system.getClassTypesList().get("ActuatingDevice").getHtblPropUriName());

		System.out.println("==================================================================");

		System.out.println(
				SystemClass.getSystemInstance().getClassTypesList().get("ActuatingDevice").getProperties().toString());
		System.out.println(
				SystemClass.getSystemInstance().getClassTypesList().get("ActuatingDevice").getClassTypesList());
		System.out.println(
				SystemClass.getSystemInstance().getClassTypesList().get("ActuatingDevice").getHtblPropUriName());
		System.out.println(
				SystemClass.getSystemInstance().getClassTypesList().get("ActuatingDevice").getSuperClassesList());

		System.out.println("===========================TagDevice==========================================");

		System.out.println(system.getClassTypesList().get("TagDevice").getProperties().toString());
		System.out.println(system.getClassTypesList().get("TagDevice").getClassTypesList());
		System.out.println(system.getClassTypesList().get("TagDevice").getSuperClassesList());
		System.out.println(system.getClassTypesList().get("TagDevice").getHtblPropUriName());

		System.out.println("==================================================================");

		System.out.println(
				SystemClass.getSystemInstance().getClassTypesList().get("TagDevice").getProperties().toString());
		System.out.println(SystemClass.getSystemInstance().getClassTypesList().get("TagDevice").getClassTypesList());
		System.out.println(SystemClass.getSystemInstance().getClassTypesList().get("TagDevice").getHtblPropUriName());
		System.out.println(SystemClass.getSystemInstance().getClassTypesList().get("TagDevice").getSuperClassesList());

		System.out.println("===========================CommunicatingDevice==========================================");

		System.out.println(system.getClassTypesList().get("CommunicatingDevice").getProperties().toString());
		System.out.println(system.getClassTypesList().get("CommunicatingDevice").getClassTypesList());
		System.out.println(system.getClassTypesList().get("CommunicatingDevice").getSuperClassesList());
		System.out.println(system.getClassTypesList().get("CommunicatingDevice").getHtblPropUriName());

		System.out.println("==================================================================");

		System.out.println(SystemClass.getSystemInstance().getClassTypesList().get("CommunicatingDevice")
				.getProperties().toString());
		System.out.println(
				SystemClass.getSystemInstance().getClassTypesList().get("CommunicatingDevice").getClassTypesList());
		System.out.println(
				SystemClass.getSystemInstance().getClassTypesList().get("CommunicatingDevice").getHtblPropUriName());
		System.out.println(
				SystemClass.getSystemInstance().getClassTypesList().get("CommunicatingDevice").getSuperClassesList());
	}
}
