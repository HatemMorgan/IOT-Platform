package com.iotplatform.ontology.classes;

import java.util.Hashtable;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.OntologyClass;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.XSDDataTypes;

@Component
public class Person extends Class {

	private Hashtable<String, Property> properties;

	public Person() {
		super("Person", "http://xmlns.com/foaf/0.1/Person", Prefixes.FOAF);

		properties = new Hashtable<>();
		properties.put("age", new DataProperty("age", Prefixes.FOAF, XSDDataTypes.integer_typed));
		properties.put("birthday", new DataProperty("birthday", Prefixes.FOAF, XSDDataTypes.string_typed));
		properties.put("familyName", new DataProperty("familyName", Prefixes.FOAF, XSDDataTypes.string_typed));
		properties.put("firstName", new DataProperty("firstName", Prefixes.FOAF, XSDDataTypes.string_typed));
		properties.put("lastName", new DataProperty("lastName", Prefixes.FOAF, XSDDataTypes.string_typed));
		properties.put("gender", new DataProperty("gender", Prefixes.FOAF, XSDDataTypes.string_typed));
		properties.put("id", new DataProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed));
		properties.put("title", new DataProperty("title", Prefixes.FOAF, XSDDataTypes.string_typed));
		properties.put("userName", new DataProperty("userName", Prefixes.FOAF, XSDDataTypes.string_typed));
		properties.put("knows", new ObjectProperty("knows", Prefixes.FOAF, OntologyClass.Person));
	}

	public Hashtable<String, Property> getProperties() {
		return properties;
	}

}
