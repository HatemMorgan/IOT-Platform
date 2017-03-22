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
		super("Condition", "http://purl.oclc.org/NET/ssnx/ssn#Condition", Prefixes.SSN, null, false);
		init();
	}

	public static Condition getConditionInstance() {
		if (conditionInstance == null)
			conditionInstance = new Condition();

		return conditionInstance;
	}

	private void init() {

		/*
		 * Condition description
		 */
		super.getProperties().put("description",
				new DataTypeProperty("description", Prefixes.IOT_PLATFORM, XSDDataTypes.string_typed, false, false));
		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "description", "description");

		super.getSuperClassesList().add(Property.getPropertyInstance());

	}

	public static void main(String[] args) {
		Condition condition = new Condition();

		System.out.println(condition.getProperties());
		System.out.println(Condition.getConditionInstance().getProperties());

		System.out.println(condition.getHtblPropUriName().size());
		System.out.println(Condition.getConditionInstance().getHtblPropUriName().size());

		System.out.println(condition.getSuperClassesList());
		System.out.println(Condition.getConditionInstance().getSuperClassesList());

		System.out.println(condition.getClassTypesList());
		System.out.println(Condition.getConditionInstance().getClassTypesList());

	}

}
