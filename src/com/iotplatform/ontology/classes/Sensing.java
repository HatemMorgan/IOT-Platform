package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Prefixes;

/*
 *  This class maps ssn:Sensing class in the ontology
 *  
 *  Sensing is a process that results in the estimation, or calculation, of the value of a phenomenon.
 *  
 *  Defining the specification of the procedure implemented in a sensor. 
 *  It may be specified as a known principle or method or, if a computer science approach is followed, 
 *  as a function which has an output and possibly some inputs. which is expressed using ssn:hasInput 
 *  and ssn:hasOutput properties
 */

@Component
public class Sensing extends Process {

	private static Sensing sensingInstance;

	public Sensing() {
		super("Sensing", "http://purl.oclc.org/NET/ssnx/ssn#Sensing", Prefixes.SSN, null, false);
		init();
	}

	public Sensing(String nothing) {
		super("Sensing", "http://purl.oclc.org/NET/ssnx/ssn#Sensing", Prefixes.SSN, null, false, null);
	}

	public synchronized static Sensing getSensingInstance() {
		if (sensingInstance == null) {
			sensingInstance = new Sensing(null);
			initSensingStaticInstance(sensingInstance);
			Process.initProcessStaticInstance(sensingInstance);
		}
		return sensingInstance;
	}

	private void init() {

		this.getSuperClassesList().add(Process.getProcessInstance());
	}

	private static void initSensingStaticInstance(Sensing sensingInstance) {

		sensingInstance.getSuperClassesList().add(Process.getProcessInstance());
	}

	public static void main(String[] args) {
		Sensing sensing = new Sensing();
		System.out.println(sensing.getHtblPropUriName());
		System.out.println(sensing.getProperties());
		System.out.println(sensing.getSuperClassesList());
		System.out.println(sensing.getClassTypesList());

		System.out.println("===========================================");

		System.out.println(Sensing.getSensingInstance().getHtblPropUriName());
		System.out.println(Sensing.getSensingInstance().getProperties());
		System.out.println(Sensing.getSensingInstance().getSuperClassesList());
		System.out.println(Sensing.getSensingInstance().getClassTypesList());
	}
}
