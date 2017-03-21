package com.iotplatform.ontology.classes;

import java.util.Hashtable;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.XSDDataTypes;

/*
 *  this class maps the Person Class in the ontology 
 */

public class Person extends Agent {

	private static Person personInstance;

	public Person() {
		super("Person", "http://xmlns.com/foaf/0.1/Person", Prefixes.FOAF,
				new DataTypeProperty("userName", Prefixes.FOAF, XSDDataTypes.string_typed, false, true), true);
		init();
	}

	/*
	 * This constructor is used to perform overloading constructor technique and
	 * the parameter String nothing will be passed always with null
	 * 
	 * I have done this overloaded constructor to instantiate the static
	 * personInstance to avoid java.lang.StackOverflowError exception that Occur
	 * when calling init() to add properties to PersonInstance (because knows
	 * property has domain Person and range Person also)
	 * 
	 * I added null to tell the agentSuperClass constructor to not call its init
	 * method I will make person class instance inherit properties of agentClass
	 * by calling Agnet.initAgentStaticInstanc(Class agentInstance) method to
	 * add agentProperties to it
	 */
	public Person(String nothing) {
		super("Person", "http://xmlns.com/foaf/0.1/Person", Prefixes.FOAF,
				new DataTypeProperty("userName", Prefixes.FOAF, XSDDataTypes.string_typed, false, true), null, true);
	}

	public Person(String name, String uri, Prefixes prefix, Property uniqueIdentifierProperty, boolean hasTypeClasses) {
		super(name, uri, prefix, uniqueIdentifierProperty, hasTypeClasses);
		init();

	}

	public synchronized static Person getPersonInstance() {

		if (personInstance == null) {
			personInstance = new Person(null);
			initPersonStaticInstance(personInstance);
			initPersonStaticInstanceTypeClasses(personInstance);
			Agent.initAgentStaticInstanc(personInstance);
		}

		return personInstance;
	}

	private void init() {

		this.getProperties().put("age",
				new DataTypeProperty("age", Prefixes.FOAF, XSDDataTypes.integer_typed, false, false));
		this.getProperties().put("birthday",
				new DataTypeProperty("birthday", Prefixes.FOAF, XSDDataTypes.string_typed, false, false));
		this.getProperties().put("familyName",
				new DataTypeProperty("familyName", Prefixes.FOAF, XSDDataTypes.string_typed, false, false));
		this.getProperties().put("firstName",
				new DataTypeProperty("firstName", Prefixes.FOAF, XSDDataTypes.string_typed, false, false));
		this.getProperties().put("middleName",
				new DataTypeProperty("middleName", Prefixes.FOAF, XSDDataTypes.string_typed, false, false));
		this.getProperties().put("gender",
				new DataTypeProperty("gender", Prefixes.FOAF, XSDDataTypes.string_typed, false, false));
		this.getProperties().put("title",
				new DataTypeProperty("title", Prefixes.FOAF, XSDDataTypes.string_typed, false, false));
		this.getProperties().put("userName",
				new DataTypeProperty("userName", Prefixes.FOAF, XSDDataTypes.string_typed, false, true));
		this.getProperties().put("knows",
				new ObjectProperty("knows", Prefixes.FOAF, Person.getPersonInstance(), true, false));

		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "age", "age");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "birthday", "birthday");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "familyName", "familyName");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "firstName", "firstName");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "middleName", "middleName");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "gender", "gender");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "title", "title");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "userName", "userName");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "knows", "knows");

		this.getSuperClassesList().add(Agent.getAgentInstance());

		if (this.isHasTypeClasses()) {
			this.setClassTypesList(new Hashtable<>());
			initPersonTypeClasses();
		}

	}

	private void initPersonTypeClasses() {
		Class admin = new Class("Admin", "http://iot-platform#Admin", Prefixes.IOT_PLATFORM,
				new DataTypeProperty("userName", Prefixes.FOAF, XSDDataTypes.string_typed, false, true), false);
		Admin.initAdminStaticInstance(admin);
		Person.initPersonStaticInstance(admin);
		Agent.initAgentStaticInstanc(admin);

		Class developer = new Class("Developer", "http://iot-platform#Developer", Prefixes.IOT_PLATFORM,
				new DataTypeProperty("userName", Prefixes.FOAF, XSDDataTypes.string_typed, false, true), false);
		Developer.initDeveloperStaticInstance(developer);
		Person.initPersonStaticInstance(developer);
		Agent.initAgentStaticInstanc(developer);

		Class normalUser = new Class("NormalUser", "http://iot-platform#NormalUser", Prefixes.IOT_PLATFORM,
				new DataTypeProperty("userName", Prefixes.FOAF, XSDDataTypes.string_typed, false, true), false);
		NormalUser.initNormalUserStaticInstance(normalUser);
		Person.initPersonStaticInstance(normalUser);
		Agent.initAgentStaticInstanc(normalUser);

		this.getClassTypesList().put("Developer", developer);
		this.getClassTypesList().put("Admin", admin);
		this.getClassTypesList().put("NormalUser", normalUser);

	}

	public static void initPersonStaticInstanceTypeClasses(Class personInstance) {
		Class admin = new Class("Admin", "http://iot-platform#Admin", Prefixes.IOT_PLATFORM,
				new DataTypeProperty("userName", Prefixes.FOAF, XSDDataTypes.string_typed, false, true), false);
		Admin.initAdminStaticInstance(admin);
		Person.initPersonStaticInstance(admin);
		Agent.initAgentStaticInstanc(admin);

		Class developer = new Class("Developer", "http://iot-platform#Developer", Prefixes.IOT_PLATFORM,
				new DataTypeProperty("userName", Prefixes.FOAF, XSDDataTypes.string_typed, false, true), false);
		Developer.initDeveloperStaticInstance(developer);
		Person.initPersonStaticInstance(developer);
		Agent.initAgentStaticInstanc(developer);

		Class normalUser = new Class("NormalUser", "http://iot-platform#NormalUser", Prefixes.IOT_PLATFORM,
				new DataTypeProperty("userName", Prefixes.FOAF, XSDDataTypes.string_typed, false, true), false);
		NormalUser.initNormalUserStaticInstance(normalUser);
		Person.initPersonStaticInstance(normalUser);
		Agent.initAgentStaticInstanc(normalUser);

		personInstance.getClassTypesList().put("Developer", developer);
		personInstance.getClassTypesList().put("Admin", admin);
		personInstance.getClassTypesList().put("NormalUser", normalUser);

	}

	public static void initPersonStaticInstance(Class personInstance) {
		personInstance.getProperties().put("age",
				new DataTypeProperty("age", Prefixes.FOAF, XSDDataTypes.integer_typed, false, false));
		personInstance.getProperties().put("birthday",
				new DataTypeProperty("birthday", Prefixes.FOAF, XSDDataTypes.string_typed, false, false));
		personInstance.getProperties().put("familyName",
				new DataTypeProperty("familyName", Prefixes.FOAF, XSDDataTypes.string_typed, false, false));
		personInstance.getProperties().put("firstName",
				new DataTypeProperty("firstName", Prefixes.FOAF, XSDDataTypes.string_typed, false, false));
		personInstance.getProperties().put("middleName",
				new DataTypeProperty("middleName", Prefixes.FOAF, XSDDataTypes.string_typed, false, false));
		personInstance.getProperties().put("gender",
				new DataTypeProperty("gender", Prefixes.FOAF, XSDDataTypes.string_typed, false, false));
		personInstance.getProperties().put("title",
				new DataTypeProperty("title", Prefixes.FOAF, XSDDataTypes.string_typed, false, false));
		personInstance.getProperties().put("userName",
				new DataTypeProperty("userName", Prefixes.FOAF, XSDDataTypes.string_typed, false, true));
		personInstance.getProperties().put("knows",
				new ObjectProperty("knows", Prefixes.FOAF, Person.getPersonInstance(), true, false));

		personInstance.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "age", "age");
		personInstance.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "birthday", "birthday");
		personInstance.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "familyName", "familyName");
		personInstance.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "firstName", "firstName");
		personInstance.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "middleName", "middleName");
		personInstance.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "gender", "gender");
		personInstance.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "title", "title");
		personInstance.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "userName", "userName");
		personInstance.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "knows", "knows");

		personInstance.getSuperClassesList().add(Agent.getAgentInstance());
	}

	public static void main(String[] args) {
		Person person = new Person();
		System.out.println(person.getClassTypesList());
		// System.out.println(Person.getPersonInstance().getProperties().size());
		 System.out.println(person.getProperties().size());

		// System.out.println(person.getClassTypesList().get("NormalUser").getProperties().size());
		// System.out.println(Person.getPersonInstance().getClassTypesList().get("NormalUser").getProperties().size());
	}
}
