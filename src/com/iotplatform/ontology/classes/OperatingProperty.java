package com.iotplatform.ontology.classes;

import java.util.Hashtable;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

/*
 *  This Class maps ssn:OperatingProperty class in the ontology
 *  
 *  An identifiable characteristic of the environmental and other conditions in which the sensor is 
 *  intended to operate.  May include power ranges, power sources, standard configurations, attachments 
 *  and the like.
 */

@Component
public class OperatingProperty extends Property {

	private Hashtable<String, Class> operatingPropertyTypesList;
	private static OperatingProperty operatingPropertyInstance;

	public OperatingProperty() {
		super("OperatingProperty", "http://purl.oclc.org/NET/ssnx/ssn#OperatingProperty", Prefixes.SSN);
		init();
	}

	/*
	 * String nothing parameter is added for overloading constructor technique
	 * because I need to initialize an instance without having properties and it
	 * will be always passed by null
	 */
	public OperatingProperty(String nothing) {
		super("OperatingProperty", "http://purl.oclc.org/NET/ssnx/ssn#OperatingProperty", Prefixes.SSN);
	}

	public static OperatingProperty getOperatingPropertyInstance() {
		if (operatingPropertyInstance == null)
			operatingPropertyInstance = new OperatingProperty(null);

		return operatingPropertyInstance;
	}

	private void init() {
		operatingPropertyTypesList = new Hashtable<>();

		/*
		 * Schedule of maintenance for a system/sensor in the specified
		 * conditions.
		 */
		Class maintenanceSchedule = new Class("MaintenanceSchedule",
				"http://purl.oclc.org/NET/ssnx/ssn#MaintenanceSchedule", Prefixes.SSN);

		/*
		 * adding ssn:OperatingProperty class to superClassesList to tell the
		 * dao to add triple that expresses that an instance of class
		 * ssn:MaintenanceSchedule is also an instance of class
		 * ssn:OperatingProperty
		 */
		maintenanceSchedule.getSuperClassesList().add(OperatingProperty.getOperatingPropertyInstance());
		operatingPropertyTypesList.put("MaintenanceSchedule", maintenanceSchedule);

		/*
		 * Power range in which system/sensor is expected to operate.
		 */
		Class operatingPowerRange = new Class("OperatingPowerRange",
				"http://purl.oclc.org/NET/ssnx/ssn#OperatingPowerRange", Prefixes.SSN);

		/*
		 * adding ssn:OperatingProperty class to superClassesList to tell the
		 * dao to add triple that expresses that an instance of class
		 * ssn:OperatingPowerRange is also an instance of class
		 * ssn:OperatingProperty
		 */
		operatingPowerRange.getSuperClassesList().add(OperatingProperty.getOperatingPropertyInstance());
		operatingPropertyTypesList.put("OperatingPowerRange", operatingPowerRange);

	}
}
