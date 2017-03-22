package com.iotplatform.ontology.classes;

import java.security.Provider;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.XSDDataTypes;

/*
 * This Class maps the Process Class in the ontology
 * 
 * A process has an output and possibly inputs and, for a composite process, describes the temporal and 
 * dataflow dependencies and relationships amongst its parts. [SSN XG]
 */

@Component
public class Process extends Class {

	private static Process processInstance;

	public Process(String name, String uri, Prefixes prefix, Property uniqueIdentifierProperty,
			boolean hasTypeClasses) {
		super(name, uri, prefix, uniqueIdentifierProperty, hasTypeClasses);
		init();
	}

	public Process(String name, String uri, Prefixes prefix, Property uniqueIdentifierProperty, boolean hasTypeClasses,
			String nothing) {
		super(name, uri, prefix, uniqueIdentifierProperty, hasTypeClasses);
	}

	public synchronized static Process getProcessInstance() {
		if (processInstance == null) {
			processInstance = new Process();
		}

		return processInstance;
	}

	public Process() {
		super("Process", "http://purl.oclc.org/NET/ssnx/ssn#Process", Prefixes.SSN, null, true);
		init();
	}

	private void init() {
		super.getProperties().put("id",
				new DataTypeProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, false));

		super.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");

		if (this.isHasTypeClasses()) {
			super.getClassTypesList().put("Sensing", Sensing.getSensingInstance());
		}
	}

	public static void initProcessStaticInstance(Process processInstance) {
		processInstance.getProperties().put("id",
				new DataTypeProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, false));

		processInstance.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");

		if (processInstance.isHasTypeClasses()) {
			processInstance.getClassTypesList().put("Sensing", Sensing.getSensingInstance());
		}
	}

	public static void main(String[] args) {
		Process process = new Process();

		System.out.println(process.getClassTypesList());
		System.out.println(Process.getProcessInstance().getClassTypesList());
		System.out.println(process.getSuperClassesList());
		System.out.println(Process.getProcessInstance().getSuperClassesList());
		System.out.println(Process.getProcessInstance().getClassTypesList().get("Sensing").getSuperClassesList());
	}
}
