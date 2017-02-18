package com.iotplatform.ontology.classes;

import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.OntologyClass;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.Property;

@Component
public class Admin extends Class {
	private Person person;
	private Hashtable<String , Property> properties;
	@Autowired
	public Admin(Person person){
		super("Admin", "http://iot-platform#Admin", Prefixes.IOT_PLATFORM);
		this.person = person;
		this.properties =  this.person.getProperties();
		this.properties.put("adminOf",new ObjectProperty("adminOf", Prefixes.IOT_PLATFORM, OntologyClass.Application));
	}
	
}
