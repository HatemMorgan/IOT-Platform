package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

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

	private static Observation observationInstance;
	private Class observationSubjectClassInstance;

	public Observation() {
		super("Observation", "http://purl.oclc.org/NET/ssnx/ssn#Observation", Prefixes.SSN, null, false);
		init();
	}

	private Class getObservationSubjectClassInstance() {
		if (observationSubjectClassInstance == null)
			observationSubjectClassInstance = new Class("Observation", "http://purl.oclc.org/NET/ssnx/ssn#Observation",
					Prefixes.SSN, null, false);

		return observationSubjectClassInstance;
	}

	public synchronized static Observation getObservationInstance() {
		if (observationInstance == null) {
			observationInstance = new Observation();
		}
		return observationInstance;
	}

	private void init() {

		/*
		 * A (measurement) procedure is a detailed description of a measurement
		 * according to one or more measurement principles and to a given
		 * measurement method, based on a measurement model and including any
		 * calculation to obtain a measurement result [VIM 2.6] . one to one
		 * relationship
		 */
		super.getProperties().put("sensingMethodUsed", new ObjectProperty(getObservationSubjectClassInstance(),
				"sensingMethodUsed", Prefixes.SSN, Sensing.getSensingInstance(), false, false));
		/*
		 * Relation linking an Observation (i.e., a description of the context,
		 * the Situation, in which the observatioin was made) and a
		 * Result(Sensor Output), which contains a value representing the value
		 * associated with the observed Property. one to one relationship
		 */
		super.getProperties().put("observationResult", new ObjectProperty(getObservationSubjectClassInstance(),
				"observationResult", Prefixes.SSN, SensorOutput.getSensorOutputInstance(), false, false));

		/*
		 * Relation linking an Observation to the Property that was observed.
		 * The observedProperty should be a Property (hasProperty) of the
		 * FeatureOfInterest (linked by featureOfInterest) of this observation.
		 * one to one relationship
		 */
		super.getProperties().put("observedProperty", new ObjectProperty(getObservationSubjectClassInstance(),
				"observedProperty", Prefixes.SSN, Property.getPropertyInstance(), false, false));

		/*
		 * A relation between an observation and the entity whose quality was
		 * observed. For example, in an observation of the weight of a person,
		 * the feature of interest is the person and the quality is weight. one
		 * to one relationship
		 */
		super.getProperties().put("featureOfInterest", new ObjectProperty(getObservationSubjectClassInstance(),
				"featureOfInterest", Prefixes.SSN, FeatureOfInterest.getFeatureOfInterestInstance(), false, false));

		/*
		 * Links an observation to its stimulus(Event). one to one relationship
		 */
		super.getProperties().put("includesEvent", new ObjectProperty(getObservationSubjectClassInstance(),
				"includesEvent", Prefixes.DUL, Stimulus.getStimulusInstance(), false, false));

		super.getProperties().put("id", new DataTypeProperty(getObservationSubjectClassInstance(), "id",
				Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, false));

		/*
		 * The result time is the time when the procedure associated with the
		 * observation act was applied.
		 * 
		 * The result time shall describe the time when the result became
		 * available, typically when the procedure associated with the
		 * observation was completed For some observations this is identical to
		 * the phenomenonTime. However, there are important cases where they
		 * differ.[O&M]
		 */
		super.getProperties().put("observationResultTime", new DataTypeProperty(getObservationSubjectClassInstance(),
				"observationResultTime", Prefixes.SSN, XSDDataTypes.string_typed, false, false));

		/*
		 * Rebadged as phenomenon time in [O&M]. The phenomenon time shall
		 * describe the time that the result applies to the property of the
		 * feature-of-interest. This is often the time of interaction by a
		 * sampling procedure or observation procedure with a real-world
		 * feature.
		 * 
		 * The sampling time is the time that the result applies to the
		 * feature-of-interest. This is the time usually required for geospatial
		 * analysis of the result.
		 */
		super.getProperties().put("observationSamplingTime", new DataTypeProperty(getObservationSubjectClassInstance(),
				"observationSamplingTime", Prefixes.SSN, XSDDataTypes.string_typed, false, false));

		super.getProperties().put("qualityOfObservation", new DataTypeProperty(getObservationSubjectClassInstance(),
				"qualityOfObservation", Prefixes.SSN, XSDDataTypes.double_typed, false, false));

		super.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");
		super.getHtblPropUriName().put(Prefixes.SSN.getUri() + "sensingMethodUsed", "sensingMethodUsed");
		super.getHtblPropUriName().put(Prefixes.SSN.getUri() + "observationResult", "observationResult");
		super.getHtblPropUriName().put(Prefixes.SSN.getUri() + "observedProperty", "observedProperty");
		super.getHtblPropUriName().put(Prefixes.SSN.getUri() + "featureOfInterest", "featureOfInterest");
		super.getHtblPropUriName().put(Prefixes.DUL.getUri() + "includesEvent", "includesEvent");

	}

}
