package com.iotplatform.ontology.classes;


import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

/*
 *  this class maps the Person Class in the ontology 
 */

public class Person extends Agent {


	private static Person personInstance;


	public Person() {
		super("Person", "http://xmlns.com/foaf/0.1/Person", Prefixes.FOAF);

			init();
		

	}
	
	/*
	 * this constructor is used only to construct an instance of class
	 * Person that will be used as the class type of an object so it does
	 * not need to has the associated properties of class Person . the
	 * nothing parameter that it takes will be passed as null because it is only
	 * used to allow overloading constructor technique
	 */

	public Person(String nothing) {

		super("Person", "http://xmlns.com/foaf/0.1/Person", Prefixes.FOAF);
	}

	public Person(String name, String uri, Prefixes prefix) {
		super(name, uri, prefix);
			init();
		super.getSuperClassesList().add(Agent.getAgentInstance());

	}

	public synchronized static Person getPersonInstance() {

		if (personInstance == null) {
			personInstance = new Person(null);

		}
		return personInstance;
	}

	
	private void init() {

		this.getProperties().put("age", new DataTypeProperty("age", Prefixes.FOAF, XSDDataTypes.integer_typed,false,false));
		this.getProperties().put("birthday",
				new DataTypeProperty("birthday", Prefixes.FOAF, XSDDataTypes.string_typed,false,false));
		this.getProperties().put("familyName",
				new DataTypeProperty("familyName", Prefixes.FOAF, XSDDataTypes.string_typed,false,false));
		this.getProperties().put("firstName",
				new DataTypeProperty("firstName", Prefixes.FOAF, XSDDataTypes.string_typed,false,false));
		this.getProperties().put("middleName",
				new DataTypeProperty("middleName", Prefixes.FOAF, XSDDataTypes.string_typed,false,false));
		this.getProperties().put("gender", new DataTypeProperty("gender", Prefixes.FOAF, XSDDataTypes.string_typed,false,false));
		this.getProperties().put("title", new DataTypeProperty("title", Prefixes.FOAF, XSDDataTypes.string_typed,false,false));
		this.getProperties().put("userName",
				new DataTypeProperty("userName", Prefixes.FOAF, XSDDataTypes.string_typed,false,true));
		this.getProperties().put("knows", new ObjectProperty("knows", Prefixes.FOAF, Person.getPersonInstance(),true,false));

		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "age", "age");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "birthday", "birthday");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "familyName", "familyName");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "firstName", "firstName");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "middleName", "middleName");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "gender", "gender");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "title", "title");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "userName", "userName");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "knows", "knows");

	}
public static void main(String[] args) {
	SystemClass.out.println("heree");
}
}
