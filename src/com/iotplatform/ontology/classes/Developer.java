package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;

/*
 *  This class maps the Developer class in the ontology
 */

@Component
public class Developer extends Person {

	private static Developer developerInstance;
	private Class developerSubjectClassInstance;

	public Developer() {
		super("Developer", "http://iot-platform#Developer", Prefixes.IOT_PLATFORM, "userName", false);
		init();
	}

	/*
	 * This constructor is used to perform overloading constructor technique and
	 * the parameter String nothing will be passed always with null
	 * 
	 * I have done this overloaded constructor to instantiate the static
	 * developerInstance to avoid java.lang.StackOverflowError exception that
	 * Occur when calling init() to add properties to developerInstance
	 * 
	 */
	public Developer(String nothing) {
		super("Developer", "http://iot-platform#Developer", Prefixes.IOT_PLATFORM, "userName", false);
	}

	private Class getDeveloperSubjectClassInstance() {
		if (developerSubjectClassInstance == null)
			developerSubjectClassInstance = new Class("Developer", "http://iot-platform#Developer",
					Prefixes.IOT_PLATFORM, "userName", false);

		return developerSubjectClassInstance;
	}

	public synchronized static Developer getDeveloperInstance() {
		if (developerInstance == null) {
			developerInstance = new Developer(null);
			initDeveloperStaticInstance(developerInstance);
		}

		return developerInstance;
	}

	public static void initDeveloperStaticInstance(Developer developerInstance) {
		developerInstance.getProperties().put("developedApplication",
				new ObjectProperty(developerInstance.getDeveloperSubjectClassInstance(), "developedApplication",
						Prefixes.IOT_PLATFORM, Application.getApplicationInstance(), false, false));

		developerInstance.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "developedApplication",
				"developedApplication");

		developerInstance.getSuperClassesList().add(Person.getPersonInstance());
	}

	private void init() {
		super.getProperties().put("developedApplication", new ObjectProperty(getDeveloperSubjectClassInstance(),
				"developedApplication", Prefixes.IOT_PLATFORM, Application.getApplicationInstance(), false, false));

		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "developedApplication", "developedApplication");

		super.getSuperClassesList().add(Person.getPersonInstance());
	}

	public static void main(String[] args) {
		Developer developer = new Developer();
		System.out.println(developer.getProperties().size());
		System.out.println(Developer.getDeveloperInstance().getProperties().size());
		System.out.println(developer.getClassTypesList());
		System.out.println(Developer.getDeveloperInstance().getClassTypesList());
	}

}
