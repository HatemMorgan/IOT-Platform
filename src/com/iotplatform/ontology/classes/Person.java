package com.iotplatform.ontology.classes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

@Component
public class Person extends Class {
	
	@Autowired
	private Application applicationClass;
	public Person() {
		super("Person", "http://xmlns.com/foaf/0.1/Person", Prefixes.FOAF);

		if (this.getProperties().size() == 0) {
			init();
		}

		System.out.println("Person Bean Created");
	}

	public Person(String name, String uri, Prefixes prefix) {
		super(name, uri, prefix);
		if (this.getProperties().size() == 0) {
			init();
		}
	}

	
	public Application getApplicationClass() {
		return applicationClass;
	}

	private void init() {
		this.getProperties().put("age", new DataProperty("age", Prefixes.FOAF, XSDDataTypes.integer_typed));
		this.getProperties().put("birthday", new DataProperty("birthday", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("familyName",
				new DataProperty("familyName", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("firstName", new DataProperty("firstName", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("lastName", new DataProperty("lastName", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("gender", new DataProperty("gender", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("id", new DataProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed));
		this.getProperties().put("title", new DataProperty("title", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("userName", new DataProperty("userName", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("knows", new ObjectProperty("knows", Prefixes.FOAF,this));
	}

}
