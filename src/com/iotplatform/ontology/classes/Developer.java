package com.iotplatform.ontology.classes;


import org.springframework.stereotype.Component;

import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;

@Component
public class Developer extends Person {


	public Developer() {
		super("Developer", "http://iot-platform#Developer", Prefixes.IOT_PLATFORM);
		super.getProperties().put("developedApplication",
				new ObjectProperty("developedApplication", Prefixes.IOT_PLATFORM, this.getApplicationClass()));

		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "developedApplication", "developedApplication");

		super.getSuperClassesList().add(Person.getPersonInstance());
	}

	

}
