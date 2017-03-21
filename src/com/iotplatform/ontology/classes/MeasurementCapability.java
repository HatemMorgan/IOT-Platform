package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Prefixes;

/*
 * This Class maps MeasurementCapability Class in the ontology
 * 
 * Collects together measurement properties (accuracy, range, precision, etc) and the environmental conditions 
 * in which those properties hold, representing a specification of a sensor's capability in those conditions.
 * 
 * One instance of ssn:MeasurementCapability can describe a set of measurement properties linked by the
 *  property ssn:hasMeasurementProperty and connected to a property using ssn:forProperty
 * 
 * A sensor can observe a number of PROPERTIES and this allows measurement capabilities to be defined for
 *  EACH property
 * 
 * The conditions, in which these measurement properties are valid, are specified using the property 
 * ssn:inCondition and expressed using an instance of the class ssn:Condition (i.e. observable conditions 
 * that affect the operation of the sensor)
 * 
 * ex: The sensor that measure acceleration has acceleration measurment capability
 * 
 */

@Component
public class MeasurementCapability extends Property {

	private static MeasurementCapability measurementCapabilityInstance;

	public MeasurementCapability() {
		super("MeasurementCapability", "http://purl.oclc.org/NET/ssnx/ssn#MeasurementCapability", Prefixes.SSN, null,
				false);
		init();
	}

	public synchronized static MeasurementCapability getMeasurementCapabilityInstance() {
		if (measurementCapabilityInstance == null)
			measurementCapabilityInstance = new MeasurementCapability();

		return measurementCapabilityInstance;
	}

	private void init() {

	}
}
