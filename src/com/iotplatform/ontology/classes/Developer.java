package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

/*
 *  This class maps the Developer class in the ontology
 */

@Component
public class Developer extends Person {

	private static Developer developerInstance;

	public Developer() {
		super("Developer", "http://iot-platform#Developer", Prefixes.IOT_PLATFORM);
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
		super("Developer", "http://iot-platform#Developer", Prefixes.IOT_PLATFORM);
	}

	public synchronized static Developer getDeveloperInstance() {
		if (developerInstance == null) {
			developerInstance = new Developer(null);
			initDeveloperStaticInstance(developerInstance);
		}

		return developerInstance;
	}

	private static void initDeveloperStaticInstance(Developer developerInstance) {
		developerInstance.getProperties().put("developedApplication", new ObjectProperty("developedApplication",
				Prefixes.IOT_PLATFORM, Application.getApplicationInstance(), false, false));

		developerInstance.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "developedApplication",
				"developedApplication");

		developerInstance.getSuperClassesList().add(Person.getPersonInstance());
	}

	private void init() {
		super.getProperties().put("developedApplication", new ObjectProperty("developedApplication",
				Prefixes.IOT_PLATFORM, Application.getApplicationInstance(), false, false));

		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "developedApplication", "developedApplication");

		super.getSuperClassesList().add(Person.getPersonInstance());
	}

}
