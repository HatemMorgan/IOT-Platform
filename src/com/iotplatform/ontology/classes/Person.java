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
		
		applicationClass = new Application();
		
		this.getProperties().put("age", new DataTypeProperty("age", Prefixes.FOAF, XSDDataTypes.integer_typed));
		this.getProperties().put("birthday",new DataTypeProperty("birthday", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("familyName",new DataTypeProperty("familyName", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("firstName",new DataTypeProperty("firstName", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("middleName",new DataTypeProperty("middleName", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("gender", new DataTypeProperty("gender", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("id", new DataTypeProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed));
		this.getProperties().put("title", new DataTypeProperty("title", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("userName",new DataTypeProperty("userName", Prefixes.FOAF, XSDDataTypes.string_typed));
		this.getProperties().put("knows", new ObjectProperty("knows", Prefixes.FOAF, this));
		this.getProperties().put("mbox", new DataTypeProperty("mbox", Prefixes.FOAF, XSDDataTypes.string_typed));
		
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "age", "age");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "birthday", "birthday");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "familyName", "familyName");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "firstName", "firstName");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "lastName", "lastName");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "gender", "gender");
		this.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "title", "title");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "userName", "userName");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "knows", "knows");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "mbox", "mbox");
	}

}
