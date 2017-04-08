package com.iotplatform.ontology.classes;

import java.util.Hashtable;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefix;

/*
 *  This Class maps ssn:OperatingProperty class in the ontology
 *  
 *  An identifiable characteristic of the environmental and other conditions in which the sensor is 
 *  intended to operate.  May include power ranges, power sources, standard configurations, attachments 
 *  and the like.
 */

@Component
public class OperatingProperty extends Property {

	private static OperatingProperty operatingPropertyInstance;

	public OperatingProperty() {
		super("OperatingProperty", "http://purl.oclc.org/NET/ssnx/ssn#OperatingProperty", Prefix.SSN, null, true);
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
	public OperatingProperty(String nothing) {
		super("OperatingProperty", "http://purl.oclc.org/NET/ssnx/ssn#OperatingProperty", Prefix.SSN, null, true,
				null);
	}

	public static OperatingProperty getOperatingPropertyInstance() {
		if (operatingPropertyInstance == null) {
			operatingPropertyInstance = new OperatingProperty(null);
			initPropertyStaticInstance(operatingPropertyInstance);
			initOperatingPropertyStaticInstance(operatingPropertyInstance);
		}

		return operatingPropertyInstance;
	}

	private void init() {
		this.getSuperClassesList().add(Property.getPropertyInstance());

		/*
		 * emptying classTypelist
		 */
		this.setClassTypesList(new Hashtable<>());

		/*
		 * Schedule of maintenance for a system/sensor in the specified
		 * conditions.
		 */
		Class maintenanceSchedule = new Class("MaintenanceSchedule",
				"http://purl.oclc.org/NET/ssnx/ssn#MaintenanceSchedule", Prefix.SSN, null, false);

		/*
		 * adding ssn:OperatingProperty class to superClassesList to tell the
		 * dao to add triple that expresses that an instance of class
		 * ssn:MaintenanceSchedule is also an instance of class
		 * ssn:OperatingProperty
		 */
		maintenanceSchedule.getSuperClassesList().add(OperatingProperty.getOperatingPropertyInstance());
		maintenanceSchedule.getSuperClassesList()
				.addAll(OperatingProperty.getOperatingPropertyInstance().getSuperClassesList());
		maintenanceSchedule.setProperties(super.getProperties());
		maintenanceSchedule.setHtblPropUriName(super.getHtblPropUriName());
		this.getClassTypesList().put("MaintenanceSchedule", maintenanceSchedule);

		/*
		 * Power range in which system/sensor is expected to operate.
		 */
		Class operatingPowerRange = new Class("OperatingPowerRange",
				"http://purl.oclc.org/NET/ssnx/ssn#OperatingPowerRange", Prefix.SSN, null, false);

		/*
		 * adding ssn:OperatingProperty class to superClassesList to tell the
		 * dao to add triple that expresses that an instance of class
		 * ssn:OperatingPowerRange is also an instance of class
		 * ssn:OperatingProperty
		 */
		operatingPowerRange.getSuperClassesList().add(OperatingProperty.getOperatingPropertyInstance());
		operatingPowerRange.getSuperClassesList()
				.addAll(OperatingProperty.getOperatingPropertyInstance().getSuperClassesList());
		operatingPowerRange.setProperties(super.getProperties());
		operatingPowerRange.setHtblPropUriName(super.getHtblPropUriName());
		this.getClassTypesList().put("OperatingPowerRange", operatingPowerRange);

		this.getSuperClassesList().add(Property.getPropertyInstance());
	}

	private static void initOperatingPropertyStaticInstance(OperatingProperty operatingPropertyInstance) {

		operatingPropertyInstance.getSuperClassesList().add(Property.getPropertyInstance());

		/*
		 * emptying classTypelist
		 */
		operatingPropertyInstance.setClassTypesList(new Hashtable<>());

		/*
		 * Schedule of maintenance for a system/sensor in the specified
		 * conditions.
		 */
		Class maintenanceSchedule = new Class("MaintenanceSchedule",
				"http://purl.oclc.org/NET/ssnx/ssn#MaintenanceSchedule", Prefix.SSN, null, false);

		/*
		 * adding ssn:OperatingProperty class to superClassesList to tell the
		 * dao to add triple that expresses that an instance of class
		 * ssn:MaintenanceSchedule is also an instance of class
		 * ssn:OperatingProperty
		 */
		maintenanceSchedule.getSuperClassesList().add(OperatingProperty.getOperatingPropertyInstance());
		maintenanceSchedule.getSuperClassesList()
				.addAll(OperatingProperty.getOperatingPropertyInstance().getSuperClassesList());
		maintenanceSchedule.setProperties(operatingPropertyInstance.getProperties());
		maintenanceSchedule.setHtblPropUriName(operatingPropertyInstance.getHtblPropUriName());
		operatingPropertyInstance.getClassTypesList().put("MaintenanceSchedule", maintenanceSchedule);

		/*
		 * Power range in which system/sensor is expected to operate.
		 */
		Class operatingPowerRange = new Class("OperatingPowerRange",
				"http://purl.oclc.org/NET/ssnx/ssn#OperatingPowerRange", Prefix.SSN, null, false);

		/*
		 * adding ssn:OperatingProperty class to superClassesList to tell the
		 * dao to add triple that expresses that an instance of class
		 * ssn:OperatingPowerRange is also an instance of class
		 * ssn:OperatingProperty
		 */
		operatingPowerRange.getSuperClassesList().add(OperatingProperty.getOperatingPropertyInstance());
		operatingPowerRange.getSuperClassesList()
				.addAll(OperatingProperty.getOperatingPropertyInstance().getSuperClassesList());
		operatingPowerRange.setProperties(operatingPropertyInstance.getProperties());
		operatingPowerRange.setHtblPropUriName(operatingPropertyInstance.getHtblPropUriName());
		operatingPropertyInstance.getClassTypesList().put("OperatingPowerRange", operatingPowerRange);

	}

	public static void main(String[] args) {
		OperatingProperty operatingProperty = new OperatingProperty();

		System.out.println(operatingProperty.getProperties());
		System.out.println(OperatingProperty.getOperatingPropertyInstance().getProperties());

		System.out.println(operatingProperty.getHtblPropUriName().size());
		System.out.println(OperatingProperty.getOperatingPropertyInstance().getHtblPropUriName().size());

		System.out.println(operatingProperty.getSuperClassesList());
		System.out.println(OperatingProperty.getOperatingPropertyInstance().getSuperClassesList());

		System.out.println(operatingProperty.getClassTypesList().get("MaintenanceSchedule").getSuperClassesList());
		System.out.println(OperatingProperty.getOperatingPropertyInstance().getClassTypesList());

	}

}
