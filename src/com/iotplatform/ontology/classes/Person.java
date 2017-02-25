package com.iotplatform.ontology.classes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
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
		this.getProperties().put("age", new DataTypeProperty("age", Prefixes.FOAF, XSDDataTypes.integer_typed));
		this.getProperties().put("birthday",new DataTypeProperty("birthday", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("familyName",new DataTypeProperty("familyName", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("firstName",new DataTypeProperty("firstName", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("lastName",new DataTypeProperty("lastName", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("gender", new DataTypeProperty("gender", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("id", new DataTypeProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed));
		this.getProperties().put("title", new DataTypeProperty("title", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("userName",new DataTypeProperty("userName", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("knows", new ObjectProperty("knows", Prefixes.FOAF, this));
		this.getProperties().put("mbox", new DataTypeProperty("mbox", Prefixes.FOAF, XSDDataTypes.string_typed));
	}

}
