package com.iotplatform.ontology.classes;

import java.util.Hashtable;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.XSDDataTypes;

/*
 *  this class maps the Person Class in the ontology 
 */

public class Person extends Agent {

	private static Person personInstance;
	private Class personSubjectClassInstance;

	public Person() {
		super("Person", "http://xmlns.com/foaf/0.1/Person", Prefix.FOAF, "userName", true);
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
		super("Person", "http://xmlns.com/foaf/0.1/Person", Prefix.FOAF, "userName", null, true);
	}

	public Person(String name, String uri, Prefix prefix, String uniqueIdentifierPropertyName,
			boolean hasTypeClasses) {
		super(name, uri, prefix, uniqueIdentifierPropertyName, hasTypeClasses);
		init();

	}

	private Class getPersonSubjectClassInstance() {
		if (personSubjectClassInstance == null) {
			personSubjectClassInstance = new Class("Person", "http://xmlns.com/foaf/0.1/Person", Prefix.FOAF,
					"userName", true);
		}
		return personSubjectClassInstance;
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

		this.getProperties().put("age", new DataTypeProperty(getPersonSubjectClassInstance(), "age", Prefix.FOAF,
				XSDDataTypes.integer_typed, false, false));
		this.getProperties().put("birthday", new DataTypeProperty(getPersonSubjectClassInstance(), "birthday",
				Prefix.FOAF, XSDDataTypes.string_typed, false, false));
		this.getProperties().put("familyName", new DataTypeProperty(getPersonSubjectClassInstance(), "familyName",
				Prefix.FOAF, XSDDataTypes.string_typed, false, false));
		this.getProperties().put("firstName", new DataTypeProperty(getPersonSubjectClassInstance(), "firstName",
				Prefix.FOAF, XSDDataTypes.string_typed, false, false));
		this.getProperties().put("middleName", new DataTypeProperty(getPersonSubjectClassInstance(), "middleName",
				Prefix.FOAF, XSDDataTypes.string_typed, false, false));
		this.getProperties().put("gender", new DataTypeProperty(getPersonSubjectClassInstance(), "gender",
				Prefix.FOAF, XSDDataTypes.string_typed, false, false));
		this.getProperties().put("title", new DataTypeProperty(getPersonSubjectClassInstance(), "title", Prefix.FOAF,
				XSDDataTypes.string_typed, false, false));
		this.getProperties().put("userName", new DataTypeProperty(getPersonSubjectClassInstance(), "userName",
				Prefix.FOAF, XSDDataTypes.string_typed, false, true));
		this.getProperties().put("knows", new ObjectProperty(getPersonSubjectClassInstance(), "knows", Prefix.FOAF,
				Person.getPersonInstance(), true, false));

		this.getHtblPropUriName().put(Prefix.FOAF.getUri() + "age", "age");
		this.getHtblPropUriName().put(Prefix.FOAF.getUri() + "birthday", "birthday");
		this.getHtblPropUriName().put(Prefix.FOAF.getUri() + "familyName", "familyName");
		this.getHtblPropUriName().put(Prefix.FOAF.getUri() + "firstName", "firstName");
		this.getHtblPropUriName().put(Prefix.FOAF.getUri() + "middleName", "middleName");
		this.getHtblPropUriName().put(Prefix.FOAF.getUri() + "gender", "gender");
		this.getHtblPropUriName().put(Prefix.FOAF.getUri() + "title", "title");
		this.getHtblPropUriName().put(Prefix.FOAF.getUri() + "userName", "userName");
		this.getHtblPropUriName().put(Prefix.FOAF.getUri() + "knows", "knows");

		this.getSuperClassesList().add(Agent.getAgentInstance());

		if (this.isHasTypeClasses()) {
			this.setClassTypesList(new Hashtable<>());
			initPersonTypeClasses();
		}

	}

	private void initPersonTypeClasses() {

		this.getClassTypesList().put("Developer", Developer.getDeveloperInstance());
		this.getClassTypesList().put("Admin", Admin.getAdminInstance());
		this.getClassTypesList().put("NormalUser", NormalUser.getNormalUserInstance());

	}

	public static void initPersonStaticInstanceTypeClasses(Class personInstance) {

		personInstance.getClassTypesList().put("Developer", Developer.getDeveloperInstance());
		personInstance.getClassTypesList().put("Admin", Admin.getAdminInstance());
		personInstance.getClassTypesList().put("NormalUser", NormalUser.getNormalUserInstance());

	}

	public static void initPersonStaticInstance(Person personInstance) {
		personInstance.getProperties().put("age", new DataTypeProperty(personInstance.getPersonSubjectClassInstance(),
				"age", Prefix.FOAF, XSDDataTypes.integer_typed, false, false));
		personInstance.getProperties().put("birthday",
				new DataTypeProperty(personInstance.getPersonSubjectClassInstance(), "birthday", Prefix.FOAF,
						XSDDataTypes.string_typed, false, false));
		personInstance.getProperties().put("familyName",
				new DataTypeProperty(personInstance.getPersonSubjectClassInstance(), "familyName", Prefix.FOAF,
						XSDDataTypes.string_typed, false, false));
		personInstance.getProperties().put("firstName",
				new DataTypeProperty(personInstance.getPersonSubjectClassInstance(), "firstName", Prefix.FOAF,
						XSDDataTypes.string_typed, false, false));
		personInstance.getProperties().put("middleName",
				new DataTypeProperty(personInstance.getPersonSubjectClassInstance(), "middleName", Prefix.FOAF,
						XSDDataTypes.string_typed, false, false));
		personInstance.getProperties().put("gender",
				new DataTypeProperty(personInstance.getPersonSubjectClassInstance(), "gender", Prefix.FOAF,
						XSDDataTypes.string_typed, false, false));
		personInstance.getProperties().put("title", new DataTypeProperty(personInstance.getPersonSubjectClassInstance(),
				"title", Prefix.FOAF, XSDDataTypes.string_typed, false, false));
		personInstance.getProperties().put("userName",
				new DataTypeProperty(personInstance.getPersonSubjectClassInstance(), "userName", Prefix.FOAF,
						XSDDataTypes.string_typed, false, true));
		personInstance.getProperties().put("knows", new ObjectProperty(personInstance.getPersonSubjectClassInstance(),
				"knows", Prefix.FOAF, Person.getPersonInstance(), true, false));

		personInstance.getHtblPropUriName().put(Prefix.FOAF.getUri() + "age", "age");
		personInstance.getHtblPropUriName().put(Prefix.FOAF.getUri() + "birthday", "birthday");
		personInstance.getHtblPropUriName().put(Prefix.FOAF.getUri() + "familyName", "familyName");
		personInstance.getHtblPropUriName().put(Prefix.FOAF.getUri() + "firstName", "firstName");
		personInstance.getHtblPropUriName().put(Prefix.FOAF.getUri() + "middleName", "middleName");
		personInstance.getHtblPropUriName().put(Prefix.FOAF.getUri() + "gender", "gender");
		personInstance.getHtblPropUriName().put(Prefix.FOAF.getUri() + "title", "title");
		personInstance.getHtblPropUriName().put(Prefix.FOAF.getUri() + "userName", "userName");
		personInstance.getHtblPropUriName().put(Prefix.FOAF.getUri() + "knows", "knows");

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
