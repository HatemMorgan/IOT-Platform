package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

/*
 *  This Class maps ssn:Condition class in the ontology
 *  
 *  Condition class is an observable Quality of an Event or Object. 
 *   That is, not a quality of an abstract entity as is also allowed by DUL's Quality, 
 *   but rather an aspect of an entity that is intrinsic to and cannot exist without the entity 
 *   and is observable by a sensor.
 */

@Component
public class Condition extends Property {

	private static Condition conditionInstance;

	public Condition() {
		super("Condition", "http://purl.oclc.org/NET/ssnx/ssn#Condition", Prefixes.SSN);
		init();
	}

	/*
	 * String nothing parameter is added for overloading constructor technique
	 * because I need to initialize an instance without having properties and it
	 * will be always passed by null
	 */
	public Condition(String nothing) {
		super("Condition", "http://purl.oclc.org/NET/ssnx/ssn#Condition", Prefixes.SSN);
	}

	public static Condition getConditionInstance() {
		if (conditionInstance == null)
			conditionInstance = new Condition();

		return conditionInstance;
	}

	private void init() {
		/*
		 * Unique Identifier
		 */
		super.getProperties().put("id",
				new DataTypeProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, true));

		/*
		 * Condition description
		 */
		super.getProperties().put("description",
				new DataTypeProperty("description", Prefixes.IOT_PLATFORM, XSDDataTypes.string_typed, false, false));

		super.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");
		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "description", "description");

	}
}
