package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;

/*
 * This class maps ssn:SurvivalRange class in the ontology
 * 
 * The conditions a sensor can be exposed to without damage: 
 * i.e., the sensor continues to operate as defined using MeasurementCapability. 
 * If, however, the SurvivalRange is exceeded, the sensor is 'damaged' and MeasurementCapability
 *  specifications may no longer hold.
 *  
 *  It is a wrapper for condition and survival property and it is used by System Class to describe that in certain
 *  condition there is a survivalProperty instance which has a value or a range
 */

@Component
public class SurvivalRange extends Property {

	private static SurvivalRange survivalRangeInstance;

	public SurvivalRange() {
		super("SurvivalRange", "http://purl.oclc.org/NET/ssnx/ssn#SurvivalRange", Prefixes.SSN, null, false);
		init();
	}

	public SurvivalRange(String nothing) {
		super("SurvivalRange", "http://purl.oclc.org/NET/ssnx/ssn#SurvivalRange", Prefixes.SSN, null, false, null);
	}

	public synchronized static SurvivalRange getSurvivalRangeInstance() {
		if (survivalRangeInstance == null) {
			survivalRangeInstance = new SurvivalRange();
			initPropertyStaticInstance(survivalRangeInstance);
			initSurvivalRangeStaticInstance(survivalRangeInstance);
		}
		return survivalRangeInstance;
	}

	private void init() {
		/*
		 * Relation from an SurvivalProperty to a Property.
		 */
		super.getProperties().put("hasSurvivalProperty", new ObjectProperty("hasSurvivalProperty", Prefixes.SSN,
				SurvivalProperty.getSurvivalPropertyInstance(), true, false));

		/*
		 * Describes the prevailing environmental conditions for
		 * MeasurementCapabilites, OperatingConditions and SurvivalRanges. Used
		 * for example to say that a sensor has a particular accuracy in
		 * particular conditions. (see also MeasurementCapability)
		 */
		super.getProperties().put("inCondition",
				new ObjectProperty("inCondition", Prefixes.SSN, Condition.getConditionInstance(), false, false));

		super.getHtblPropUriName().put(Prefixes.SSN.getUri() + "hasSurvivalProperty", "hasSurvivalProperty");
		super.getHtblPropUriName().put(Prefixes.SSN.getUri() + "inCondition", "inCondition");

		super.getSuperClassesList().add(Property.getPropertyInstance());
	}

	private static void initSurvivalRangeStaticInstance(SurvivalRange survivalRangeInstance) {
		/*
		 * Relation from an SurvivalProperty to a Property.
		 */
		survivalRangeInstance.getProperties().put("hasSurvivalProperty", new ObjectProperty("hasSurvivalProperty",
				Prefixes.SSN, SurvivalProperty.getSurvivalPropertyInstance(), true, false));

		/*
		 * Describes the prevailing environmental conditions for
		 * MeasurementCapabilites, OperatingConditions and SurvivalRanges. Used
		 * for example to say that a sensor has a particular accuracy in
		 * particular conditions. (see also MeasurementCapability)
		 */
		survivalRangeInstance.getProperties().put("inCondition",
				new ObjectProperty("inCondition", Prefixes.SSN, Condition.getConditionInstance(), false, false));

		survivalRangeInstance.getHtblPropUriName().put(Prefixes.SSN.getUri() + "hasSurvivalProperty",
				"hasSurvivalProperty");
		survivalRangeInstance.getHtblPropUriName().put(Prefixes.SSN.getUri() + "inCondition", "inCondition");

		survivalRangeInstance.getSuperClassesList().add(Property.getPropertyInstance());
	}
}
