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

	public OperatingProperty() {
		super("OperatingProperty", "http://purl.oclc.org/NET/ssnx/ssn#OperatingProperty", Prefixes.SSN);
		init();
	}

	private void init() {
		operatingPropertyTypesList = new Hashtable<>();

		/*
		 * Schedule of maintenance for a system/sensor in the specified
		 * conditions.
		 */
		operatingPropertyTypesList.put("MaintenanceSchedule", new Class("MaintenanceSchedule",
				"http://purl.oclc.org/NET/ssnx/ssn#MaintenanceSchedule", Prefixes.SSN));

		/*
		 * Power range in which system/sensor is expected to operate.
		 */
		operatingPropertyTypesList.put("OperatingPowerRange", new Class("OperatingPowerRange",
				"http://purl.oclc.org/NET/ssnx/ssn#OperatingPowerRange", Prefixes.SSN));
	}
}
