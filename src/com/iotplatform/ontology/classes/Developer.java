package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;

/*
 *  This class maps the Developer class in the ontology
 */

@Component
public class Developer extends Person {

	public Developer() {
		super("Developer", "http://iot-platform#Developer", Prefixes.IOT_PLATFORM);

		super.getProperties().put("developedApplication", new ObjectProperty("developedApplication",
				Prefixes.IOT_PLATFORM, Application.getApplicationInstance(), false, false));

		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "developedApplication", "developedApplication");

		super.getSuperClassesList().add(Person.getPersonInstance());
	}

	public static void main(String[] args) {
		Developer developer = new Developer();
		
	}

}
