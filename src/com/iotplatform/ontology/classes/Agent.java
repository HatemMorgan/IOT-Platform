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

	public Agent() {
		super("Agent", "http://xmlns.com/foaf/0.1/Agent", Prefixes.FOAF);
		init();

	}

	public synchronized static Agent getAgentInstance() {

		if (agentInstance == null) {
			agentInstance = new Agent();

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
