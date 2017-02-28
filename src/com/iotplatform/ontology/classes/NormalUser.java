package com.iotplatform.ontology.classes;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;

@Component
public class NormalUser extends Person {

	private ArrayList<Class> superClassesList;

	public NormalUser() {
		super("NormalUser", "http://iot-platform#NormalUser", Prefixes.IOT_PLATFORM);

		super.getProperties().put("usesApplication",
				new ObjectProperty("usesApplication", Prefixes.IOT_PLATFORM, this.getApplicationClass()));

		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "usesApplication", "usesApplication");

		super.getSuperClassesList().add(Person.getPersonInstance());
	}

	public ArrayList<Class> getSuperClassesList() {
		return superClassesList;
	}

}
