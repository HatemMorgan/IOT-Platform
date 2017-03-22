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

	public Property(String name, String uri, Prefixes prefix,
			com.iotplatform.ontology.Property uniqueIdentifierProperty, boolean hasTypeClasses) {
		super(name, uri, prefix, uniqueIdentifierProperty, hasTypeClasses);
		init();
	}

	public Property() {
		super("Property", "http://purl.oclc.org/NET/ssnx/ssn#Property", Prefixes.SSN, null, true);
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
		this.getProperties().put("hasValue",
				new ObjectProperty("hasValue", Prefixes.SSN, Amount.getAmountInstance(), false, false));

		this.getProperties().put("id",
				new DataTypeProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, false));

		this.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");
		this.getHtblPropUriName().put(Prefixes.SSN.getUri() + "hasValue", "hasValue");

		if (this.isHasTypeClasses()) {

			this.getClassTypesList().put("Condition", Condition.getConditionInstance());
			this.getClassTypesList().putAll(Condition.getConditionInstance().getClassTypesList());

			this.getClassTypesList().put("MeasurementProperty", MeasurementProperty.getMeasurementPropertyInstance());
			this.getClassTypesList().putAll(MeasurementProperty.getMeasurementPropertyInstance().getClassTypesList());

			this.getClassTypesList().put("MeasurementCapability",
					MeasurementCapability.getMeasurementCapabilityInstance());
			this.getClassTypesList()
					.putAll(MeasurementCapability.getMeasurementCapabilityInstance().getClassTypesList());

			this.getClassTypesList().put("OperatingRange", OperatingRange.getOperatingRangeInstance());
			this.getClassTypesList().putAll(OperatingRange.getOperatingRangeInstance().getClassTypesList());

			this.getClassTypesList().put("OperatingProperty", OperatingProperty.getOperatingPropertyInstance());
			this.getClassTypesList().putAll(OperatingProperty.getOperatingPropertyInstance().getClassTypesList());

			this.getClassTypesList().put("SurvivalProperty", SurvivalProperty.getSurvivalPropertyInstance());
			this.getClassTypesList().putAll(SurvivalProperty.getSurvivalPropertyInstance().getClassTypesList());

			this.getClassTypesList().put("SurvivalRange", SurvivalRange.getSurvivalRangeInstance());
			this.getClassTypesList().putAll(SurvivalRange.getSurvivalRangeInstance().getClassTypesList());
		}

	}
}
