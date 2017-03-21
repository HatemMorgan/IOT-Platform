package com.iotplatform.ontology.classes;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.XSDDataTypes;

/*
 *  This class maps the agent class in the ontology
 */

public class Agent extends Class {

	private static Agent agentInstance;

	public Agent(String name, String uri, Prefixes prefix, Property uniqueIdentifierProperty, boolean hasTypeClasses) {
		super(name, uri, prefix, uniqueIdentifierProperty, hasTypeClasses);
		init();
	}

	/*
	 * This constructor is used to perform overloading constructor technique and
	 * the parameter String nothing will be passed always with null and this
	 * constructor is used to initialize staticInstance for Person class
	 * 
	 * I have done this overloaded constructor to instantiate the static
	 * personStaticInstance to avoid java.lang.StackOverflowError exception that
	 * Occur when calling init() to add properties to personStaticInstance
	 * because person class has knows property which has range
	 * personClassInstance so this cause infinitely calling constructor
	 * Person(String nothing) because every time this constructor is called init
	 * method in the above constructor is called so by changing
	 * personStaticInstanceConstructor to call this constructor by only passing
	 * null as value of String nothing (so init will not be called)
	 * 
	 */
	public Agent(String name, String uri, Prefixes prefix, Property uniqueIdentifierProperty, String nothing,
			boolean hasTypeClasses) {
		super(name, uri, prefix, uniqueIdentifierProperty, hasTypeClasses);
	}

	public Agent() {
		super("Agent", "http://xmlns.com/foaf/0.1/Agent", Prefixes.FOAF, null, true);
		init();

	}

	/*
	 * This constructor is used to perform overloading constructor technique and
	 * the parameter String nothing will be passed always with null
	 * 
	 * I have done this overloaded constructor to instantiate the static
	 * agentInstance to avoid java.lang.StackOverflowError exception that Occur
	 * when calling init() to add properties to agentInstance
	 * 
	 */
	public Agent(String nothing) {
		super("Agent", "http://xmlns.com/foaf/0.1/Agent", Prefixes.FOAF, null, true);
	}

	public synchronized static Agent getAgentInstance() {

		if (agentInstance == null) {
			agentInstance = new Agent(null);
			initAgentStaticInstance(agentInstance);
			initAgentStaticInstanceTypeClasses(agentInstance);
		}
		return agentInstance;
	}

	private static void initAgentStaticInstance(Agent agentInstance) {
		agentInstance.getProperties().put("mbox",
				new DataTypeProperty("mbox", Prefixes.FOAF, XSDDataTypes.string_typed, true, true));

		agentInstance.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "mbox", "mbox");

		/*
		 * A thing of interest to this person.
		 */

		agentInstance.getProperties().put("topic_interest",
				new DataTypeProperty("topic_interest", Prefixes.FOAF, XSDDataTypes.string_typed, true, false));

		agentInstance.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "topic_interest", "topic_interest");

	}

	private void initAgentTypeClasses() {
		Class person = Person.getPersonInstance();

		Class group = Group.getGroupInstance();

		Class organization = Organization.getOrganizationInstance();

		this.getClassTypesList().put("Person", person);
		this.getClassTypesList().put("Group", group);
		this.getClassTypesList().put("Organization", organization);

	}

	private static void initAgentStaticInstanceTypeClasses(Class agentInstance) {

		Class person = Person.getPersonInstance();

		Class group = Group.getGroupInstance();

		Class organization = Organization.getOrganizationInstance();

		agentInstance.getClassTypesList().put("Person", person);
		agentInstance.getClassTypesList().put("Group", group);
		agentInstance.getClassTypesList().put("Organization", organization);

	}

	public static void initAgentStaticInstanc(Class agentInstance) {
		agentInstance.getProperties().put("mbox",
				new DataTypeProperty("mbox", Prefixes.FOAF, XSDDataTypes.string_typed, true, true));

		agentInstance.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "mbox", "mbox");

		/*
		 * A thing of interest to this person.
		 */

		agentInstance.getProperties().put("topic_interest",
				new DataTypeProperty("topic_interest", Prefixes.FOAF, XSDDataTypes.string_typed, true, false));

		agentInstance.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "topic_interest", "topic_interest");
	}

	private void init() {

		super.getProperties().put("mbox",
				new DataTypeProperty("mbox", Prefixes.FOAF, XSDDataTypes.string_typed, true, true));

		super.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "mbox", "mbox");

		/*
		 * A thing of interest to this person.
		 */

		super.getProperties().put("topic_interest",
				new DataTypeProperty("topic_interest", Prefixes.FOAF, XSDDataTypes.string_typed, true, false));

		super.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "topic_interest", "topic_interest");

		if (this.isHasTypeClasses()) {
			initAgentTypeClasses();
		}
	}

	public static void main(String[] args) {
		Agent agent = new Agent();
		System.out.println(agent.getProperties().size());
		
		System.out.println(agent.getClassTypesList().get("Person").getClassTypesList().get("Developer").getProperties()
				.toString());
		System.out.println(
				agent.getClassTypesList().get("Person").getClassTypesList().get("Admin").getProperties().toString());
		System.out.println(agent.getClassTypesList().get("Person").getClassTypesList().get("NormalUser").getProperties()
				.toString());
		System.out.println(agent.getClassTypesList().get("Person").getProperties().toString());
		System.out.println(agent.getClassTypesList().get("Group").getProperties().toString());
		System.out.println(agent.getClassTypesList().get("Organization").getProperties().toString());

		System.out.println("=========================================================================");

		System.out.println(Agent.getAgentInstance().getClassTypesList().get("Person").getClassTypesList()
				.get("Developer").getProperties().toString());
		System.out.println(Agent.getAgentInstance().getClassTypesList().get("Person").getClassTypesList().get("Admin")
				.getProperties().toString());
		System.out.println(Agent.getAgentInstance().getClassTypesList().get("Person").getClassTypesList()
				.get("NormalUser").getProperties().toString());
		System.out.println(Agent.getAgentInstance().getClassTypesList().get("Person").getProperties().toString());
		System.out.println(Agent.getAgentInstance().getClassTypesList().get("Group").getProperties().toString());
		System.out.println(Agent.getAgentInstance().getClassTypesList().get("Organization").getProperties().toString());
	}

}
