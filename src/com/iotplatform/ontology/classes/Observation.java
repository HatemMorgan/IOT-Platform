package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;

/*
 * This Class maps the Observation Class in the ontology
 * 
 * An Observation is made by a sensor and it is a Situation in which a Sensing method has been used to estimate or calculate 
 * a value of a Property of a FeatureOfInterest.  Links to Sensing and Sensor describe what made the Observation
 *  and how; links to Property and Feature detail what was sensed; the result is the output of a Sensor; 
 *  other metadata details times etc.
 */

@Component
public class Observation extends Class {

	public Observation() {
		super("Observation", "http://purl.oclc.org/NET/ssnx/ssn#Observation", Prefixes.SSN);
		init();
	}

	private void init() {

		/*
		 * A (measurement) procedure is a detailed description of a measurement
		 * according to one or more measurement principles and to a given
		 * measurement method, based on a measurement model and including any
		 * calculation to obtain a measurement result [VIM 2.6] . one to one
		 * relationship
		 */
		this.getProperties().put("sensingMethodUsed",
				new ObjectProperty("sensingMethodUsed", Prefixes.SSN, Sensing.getSensingInstance(), false, false));
		/*
		 * Relation linking an Observation (i.e., a description of the context,
		 * the Situation, in which the observatioin was made) and a
		 * Result(Sensor Output), which contains a value representing the value
		 * associated with the observed Property. one to one relationship
		 */
		this.getProperties().put("observationResult", new ObjectProperty("observationResult", Prefixes.SSN,
				SensorOutput.getSensorOutputInstance(), false, false));

		/*
		 * Relation linking an Observation to the Property that was observed.
		 * The observedProperty should be a Property (hasProperty) of the
		 * FeatureOfInterest (linked by featureOfInterest) of this observation.
		 * one to one relationship
		 */
		this.getProperties().put("observedProperty",
				new ObjectProperty("observedProperty", Prefixes.SSN, Property.getPropertyInstance(), false, false));

		/*
		 * A relation between an observation and the entity whose quality was
		 * observed. For example, in an observation of the weight of a person,
		 * the feature of interest is the person and the quality is weight. one
		 * to one relationship
		 */
		this.getProperties().put("featureOfInterest", new ObjectProperty("featureOfInterest", Prefixes.SSN,
				FeatureOfInterest.getFeatureOfInterestInstance(), false, false));

		/*
		 * Links an observation to its stimulus(Event). one to one relationship
		 */
		this.getProperties().put("includesEvent",
				new ObjectProperty("includesEvent", Prefixes.DUL, Stimulus.getStimulusInstance(), false, false));

	}

}
