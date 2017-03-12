package com.iotplatform.ontology.classes;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

/*
 *  This class maps the agent class in the ontology
 */

public class Agent extends Class {

	private static Agent agentInstance;

	public Agent(String name, String uri, Prefixes prefix) {
		super(name, uri, prefix);
		init();
	}

	/*
	 * this constructor is used only to construct an instance of class Agent
	 * that will be used as the class type of an object so it does not need to
	 * has the associated properties of class Agent . the nothing parameter that
	 * it takes will be passed as null because it is only used to allow
	 * overloading constructor technique
	 */

	public Agent(String nothing) {
		super("Agent", "http://xmlns.com/foaf/0.1/Agent", Prefixes.FOAF);

	}

	public Agent() {
		super("Agent", "http://xmlns.com/foaf/0.1/Agent", Prefixes.FOAF);
		init();

	}

	public synchronized static Agent getAgentInstance() {

		if (agentInstance == null) {
			agentInstance = new Agent(null);

		}
		return agentInstance;
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
	}

}
