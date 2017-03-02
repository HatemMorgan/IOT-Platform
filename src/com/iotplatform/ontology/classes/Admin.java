package com.iotplatform.ontology.classes;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;

/*
 *  This class maps the Admin class in the ontology
 */


@Component
public class Admin extends Person {


	public Admin() {
		super("Admin", "http://iot-platform#Admin", Prefixes.IOT_PLATFORM);

		this.getProperties().put("adminOf",
				new ObjectProperty("adminOf", Prefixes.IOT_PLATFORM, Application.getApplicationInstance(),false,false));

		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "adminOf", "adminOf");

		super.getSuperClassesList().add(Person.getPersonInstance());
	}

	

}
