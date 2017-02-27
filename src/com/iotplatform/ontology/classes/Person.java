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

	private Application applicationClass;

	private static Person personInstance;

	@Autowired
	public Person(Application applicationClass) {
		super("Person", "http://xmlns.com/foaf/0.1/Person", Prefixes.FOAF);

		if (this.getProperties().size() == 0) {
			init();
		}

	}

	public Person() {

		super("Person", "http://xmlns.com/foaf/0.1/Person", Prefixes.FOAF);
	}

	public Person(String name, String uri, Prefixes prefix) {
		super(name, uri, prefix);

		if (this.getProperties().size() == 0) {
			init();
		}

	}

	public synchronized static Person getPersonInstance() {

		if (personInstance == null) {
			personInstance = new Person();

		}
		return personInstance;
	}

	public Application getApplicationClass() {
		return applicationClass;
	}

	private void init() {

		applicationClass = new Application();

		this.getProperties().put("age", new DataTypeProperty("age", Prefixes.FOAF, XSDDataTypes.integer_typed));
		this.getProperties().put("birthday",
				new DataTypeProperty("birthday", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("familyName",
				new DataTypeProperty("familyName", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("firstName",
				new DataTypeProperty("firstName", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("middleName",
				new DataTypeProperty("middleName", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("gender", new DataTypeProperty("gender", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("id", new DataTypeProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed));
		this.getProperties().put("title", new DataTypeProperty("title", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("userName",
				new DataTypeProperty("userName", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("knows", new ObjectProperty("knows", Prefixes.FOAF, Person.getPersonInstance()));
		this.getProperties().put("mbox", new DataTypeProperty("mbox", Prefixes.FOAF, XSDDataTypes.string_typed));

		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "age", "age");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "birthday", "birthday");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "familyName", "familyName");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "firstName", "firstName");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "middleName", "middleName");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "gender", "gender");
		this.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "title", "title");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "userName", "userName");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "knows", "knows");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "mbox", "mbox");

		System.out.println("--------------------> " + ((ObjectProperty) this.getProperties().get("knows")).getObject());
	}

}
