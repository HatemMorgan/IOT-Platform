package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.XSDDatatype;

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
		super("Observation", "http://purl.oclc.org/NET/ssnx/ssn#Observation", Prefix.SSN, null, false);
		init();
	}

	private Class getObservationSubjectClassInstance() {
		if (observationSubjectClassInstance == null)
			observationSubjectClassInstance = new Class("Observation", "http://purl.oclc.org/NET/ssnx/ssn#Observation",
					Prefix.SSN, null, false);

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
				"sensingMethodUsed", Prefix.SSN, Sensing.getSensingInstance(), false, false));
		/*
		 * Relation linking an Observation (i.e., a description of the context,
		 * the Situation, in which the observatioin was made) and a
		 * Result(Sensor Output), which contains a value representing the value
		 * associated with the observed Property. one to one relationship
		 */
		super.getProperties().put("observationResult", new ObjectProperty(getObservationSubjectClassInstance(),
				"observationResult", Prefix.SSN, SensorOutput.getSensorOutputInstance(), false, false));

		/*
		 * Relation linking an Observation to the Property that was observed.
		 * The observedProperty should be a Property (hasProperty) of the
		 * FeatureOfInterest (linked by featureOfInterest) of this observation.
		 * one to one relationship
		 */
		super.getProperties().put("observedProperty", new ObjectProperty(getObservationSubjectClassInstance(),
				"observedProperty", Prefix.SSN, Property.getPropertyInstance(), false, false));

		/*
		 * A relation between an observation and the entity whose quality was
		 * observed. For example, in an observation of the weight of a person,
		 * the feature of interest is the person and the quality is weight. one
		 * to one relationship
		 */
		super.getProperties().put("featureOfInterest", new ObjectProperty(getObservationSubjectClassInstance(),
				"featureOfInterest", Prefix.SSN, FeatureOfInterest.getFeatureOfInterestInstance(), false, false));

		/*
		 * Links an observation to its stimulus(Event). one to one relationship
		 */
		super.getProperties().put("includesEvent", new ObjectProperty(getObservationSubjectClassInstance(),
				"includesEvent", Prefix.DUL, Stimulus.getStimulusInstance(), false, false));

		super.getProperties().put("id", new DataTypeProperty(getObservationSubjectClassInstance(), "id",
				Prefix.IOT_LITE, XSDDatatype.string_typed, false, false));

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
				"observationResultTime", Prefix.SSN, XSDDatatype.string_typed, false, false));

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
				"observationSamplingTime", Prefix.SSN, XSDDatatype.string_typed, false, false));

		super.getProperties().put("qualityOfObservation", new DataTypeProperty(getObservationSubjectClassInstance(),
				"qualityOfObservation", Prefix.SSN, XSDDatatype.double_typed, false, false));

		super.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "id", "id");
		super.getHtblPropUriName().put(Prefix.SSN.getUri() + "sensingMethodUsed", "sensingMethodUsed");
		super.getHtblPropUriName().put(Prefix.SSN.getUri() + "observationResult", "observationResult");
		super.getHtblPropUriName().put(Prefix.SSN.getUri() + "observedProperty", "observedProperty");
		super.getHtblPropUriName().put(Prefix.SSN.getUri() + "featureOfInterest", "featureOfInterest");
		super.getHtblPropUriName().put(Prefix.DUL.getUri() + "includesEvent", "includesEvent");
		super.getHtblPropUriName().put(Prefix.SSN.getUri() + "observationResultTime", "observationResultTime");
		super.getHtblPropUriName().put(Prefix.SSN.getUri() + "observationSamplingTime", "observationSamplingTime");
		super.getHtblPropUriName().put(Prefix.SSN.getUri() + "qualityOfObservation", "qualityOfObservation");

	}

}
