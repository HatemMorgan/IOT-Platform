package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
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
	private Class processSubjectClassInstance;

	public Process(String name, String uri, Prefixes prefix, String uniqueIdentifierPropertyName,
			boolean hasTypeClasses) {
		super(name, uri, prefix, uniqueIdentifierPropertyName, hasTypeClasses);
		init();
	}

	private Class getProcessSubjectClassInstance() {
		if (processSubjectClassInstance == null)
			processSubjectClassInstance = new Class("Process", "http://purl.oclc.org/NET/ssnx/ssn#Process",
					Prefixes.SSN, null, true);

		return processSubjectClassInstance;
	}

	public Process(String name, String uri, Prefixes prefix, String uniqueIdentifierPropertyName,
			boolean hasTypeClasses, String nothing) {
		super(name, uri, prefix, uniqueIdentifierPropertyName, hasTypeClasses);
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
		this.getProperties().put("id", new DataTypeProperty(getProcessSubjectClassInstance(), "id", Prefixes.IOT_LITE,
				XSDDataTypes.string_typed, false, false));

		this.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");

		this.getProperties().put("hasInput", new ObjectProperty(getProcessSubjectClassInstance(), "hasInput",
				Prefixes.SSN, Input.getInputInstance(), true, false));

		this.getHtblPropUriName().put(Prefixes.SSN.getUri() + "hasInput", "hasInput");

		this.getProperties().put("hasOutput", new ObjectProperty(getProcessSubjectClassInstance(), "hasOutput",
				Prefixes.SSN, Output.getOutputInstance(), true, false));

		this.getHtblPropUriName().put(Prefixes.SSN.getUri() + "hasOutput", "hasOutput");

		if (this.isHasTypeClasses()) {
			super.getClassTypesList().put("Sensing", Sensing.getSensingInstance());
		}
	}

	public static void initProcessStaticInstance(Process processInstance) {
		processInstance.getProperties().put("id", new DataTypeProperty(processInstance.getProcessSubjectClassInstance(),
				"id", Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, false));

		processInstance.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");

		processInstance.getProperties().put("hasInput",
				new ObjectProperty(processInstance.getProcessSubjectClassInstance(), "hasInput", Prefixes.SSN,
						Input.getInputInstance(), true, false));

		processInstance.getHtblPropUriName().put(Prefixes.SSN.getUri() + "hasInput", "hasInput");

		processInstance.getProperties().put("hasOutput",
				new ObjectProperty(processInstance.getProcessSubjectClassInstance(), "hasOutput", Prefixes.SSN,
						Output.getOutputInstance(), true, false));

		processInstance.getHtblPropUriName().put(Prefixes.SSN.getUri() + "hasOutput", "hasOutput");

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
