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

	public Agent(String name, String uri, Prefixes prefix, Property uniqueIdentifierProperty) {
		super(name, uri, prefix, uniqueIdentifierProperty);
//		init();
	}

	public Agent() {
		super("Agent", "http://xmlns.com/foaf/0.1/Agent", Prefixes.FOAF, null);
//		init();

	}

	public Agent(String nothing) {
		super("Agent", "http://xmlns.com/foaf/0.1/Agent", Prefixes.FOAF, null);
	}

	public synchronized static Agent getAgentInstance() {

		if (agentInstance == null) {
			agentInstance = new Agent(null);
			initAgentStaticInstance(agentInstance);

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

		agentInstance.getClassTypesList().put("Person", Person.getPersonInstance());
		// agentInstance.getClassTypesList().put("Group",
		// Group.getGroupInstance());
		// agentInstance.getClassTypesList().put("Organization",
		// Organization.getOrganizationInstance());
	}

	public void init() {

		super.getProperties().put("mbox",
				new DataTypeProperty("mbox", Prefixes.FOAF, XSDDataTypes.string_typed, true, true));

		super.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "mbox", "mbox");

		/*
		 * A thing of interest to this person.
		 */

		super.getProperties().put("topic_interest",
				new DataTypeProperty("topic_interest", Prefixes.FOAF, XSDDataTypes.string_typed, true, false));

		super.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "topic_interest", "topic_interest");

		super.getClassTypesList().put("Person", Person.getPersonInstance());
		// super.getClassTypesList().put("Group", Group.getGroupInstance());
		// super.getClassTypesList().put("Organization",
		// Organization.getOrganizationInstance());
	}

	public static void main(String[] args) {
		Agent agent = new Agent();
		agent.init();
		System.out.println(agent.getClassTypesList().toString());
	}

}
