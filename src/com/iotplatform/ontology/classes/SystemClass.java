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
		super.getProperties().put("hasSubSystem",
				new ObjectProperty("hasSubSystem", Prefixes.SSN, SystemClass.getSystemInstance(), true, false));
	}

	public static void main(String[] args) {
		Group group = new Group();
		System.out.println("herees");
	}
}
