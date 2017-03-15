package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

/*
 *  This Class maps ssn:Property class in the ontology
 *  
 *  An observable Quality of an Event or Object.  That is, not a quality of an abstract entity as is 
 *  also allowed by DUL's Quality, but rather an aspect of an entity that is intrinsic to and cannot exist 
 *  without the entity and is observable by a sensor.
 */

@Component
public class Property extends Class {

	private static Property propertyInstance;

	public Property(String name, String uri, Prefixes prefix, boolean hasTypeClasses) {
		super(name, uri, prefix, null,hasTypeClasses);
		init();
	}

	public Property() {
		super("Property", "http://purl.oclc.org/NET/ssnx/ssn#Property", Prefixes.SSN, null);
		init();
	}

	public synchronized static Property getPropertyInstance() {
		if (propertyInstance == null)
			propertyInstance = new Property();

		return propertyInstance;
	}

	private void init() {

		/*
		 * relation between a Property and its value of type Amount
		 */
		super.getProperties().put("hasValue",
				new ObjectProperty("hasValue", Prefixes.SSN, Amount.getAmountInstance(), false, false));

		super.getProperties().put("id",
				new DataTypeProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, false));

		super.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");
		super.getHtblPropUriName().put(Prefixes.SSN.getUri() + "hasValue", "hasValue");
	}
}
