package com.iotplatform.ontology.classes;


import org.springframework.stereotype.Component;

import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;

/*
 *  This class maps the NormalUser class in the ontology
 */


@Component
public class NormalUser extends Person {


	public NormalUser() {
		super("NormalUser", "http://iot-platform#NormalUser", Prefixes.IOT_PLATFORM);

		super.getProperties().put("usesApplication",
				new ObjectProperty("usesApplication", Prefixes.IOT_PLATFORM, Application.getApplicationInstance(),false,false));

		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "usesApplication", "usesApplication");

		super.getSuperClassesList().add(Person.getPersonInstance());
	}



}
