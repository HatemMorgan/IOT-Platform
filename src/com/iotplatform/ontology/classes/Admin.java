package com.iotplatform.ontology.classes;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;

@Component
public class Admin extends Person {

	private ArrayList<Class> superClassesList;

	public Admin() {
		super("Admin", "http://iot-platform#Admin", Prefixes.IOT_PLATFORM);

		this.getProperties().put("adminOf",
				new ObjectProperty("adminOf", Prefixes.IOT_PLATFORM, this.getApplicationClass()));

		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "adminOf", "adminOf");

		super.getSuperClassesList().add(Person.getPersonInstance());
	}

	public ArrayList<Class> getSuperClassesList() {
		return superClassesList;
	}

}
