package com.iotplatform.ontology.classes;

import java.util.Hashtable;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefix;

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
	private Class measurementCapabilitySubjectClassInstance;

	public MeasurementCapability() {
		super("MeasurementCapability", "http://purl.oclc.org/NET/ssnx/ssn#MeasurementCapability", Prefix.SSN, null,
				false);
		init();
	}

	private Class getMeasurementCapabilitySubjectClassInstance() {
		if (measurementCapabilitySubjectClassInstance == null)
			measurementCapabilitySubjectClassInstance = new Class("MeasurementCapability",
					"http://purl.oclc.org/NET/ssnx/ssn#MeasurementCapability", Prefix.SSN, null, false);

		return measurementCapabilitySubjectClassInstance;
	}

	public MeasurementCapability(String nothing) {
		super("MeasurementCapability", "http://purl.oclc.org/NET/ssnx/ssn#MeasurementCapability", Prefix.SSN, null,
				false, null);
	}

	public synchronized static MeasurementCapability getMeasurementCapabilityInstance() {
		if (measurementCapabilityInstance == null) {
			measurementCapabilityInstance = new MeasurementCapability(null);
			Property.initPropertyStaticInstance(measurementCapabilityInstance);
			initMeasurementCapabilityStaticInstance(measurementCapabilityInstance);
		}
		return measurementCapabilityInstance;
	}

	private void init() {
		this.getSuperClassesList().add(Property.getPropertyInstance());

		/*
		 * A relation between some aspect of a sensing entity and a property.
		 * For example, from a sensor to the properties it can observe, or from
		 * a deployment to the properties it was installed to observe. Also from
		 * a measurement capability to the property the capability is described
		 * for. (Used in conjunction with ofFeature).
		 * 
		 * A ssn:MeasurementCapability is something that is a ssn:Property and
		 * has a ssn:hasMeasurementProperty property who must be a
		 * ssn:MeasurementProperty .
		 * 
		 * So ssn:MeasurementCapability instance will have a ssn:forProperty
		 * property who must be a ssn:Property and has a ssn:inCondition
		 * property who must be a ssn:Condition
		 */
		this.getProperties().put("forProperty", new ObjectProperty(getMeasurementCapabilitySubjectClassInstance(),
				"forProperty", Prefix.SSN, getPropertyInstance(), true, false));

		this.getHtblPropUriName().put(Prefix.SSN.getUri() + "forProperty", "forProperty");

		/*
		 * Relation from a MeasurementCapability to a MeasurementProperty. For
		 * example, to an accuracy (see notes at MeasurementCapability).
		 */
		this.getProperties().put("hasMeasurementProperty",
				new ObjectProperty(getMeasurementCapabilitySubjectClassInstance(), "hasMeasurementProperty",
						Prefix.SSN, MeasurementProperty.getMeasurementPropertyInstance(), true, false));

		this.getHtblPropUriName().put(Prefix.SSN.getUri() + "hasMeasurementProperty", "hasMeasurementProperty");

		/*
		 * Describes the prevailing environmental conditions for
		 * MeasurementCapabilites, OperatingConditions and SurvivalRanges. Used
		 * for example to say that a sensor has a particular accuracy in
		 * particular conditions. (see also MeasurementCapability)
		 */
		this.getProperties().put("inCondition", new ObjectProperty(getMeasurementCapabilitySubjectClassInstance(),
				"inCondition", Prefix.SSN, Condition.getConditionInstance(), false, false));
		this.getHtblPropUriName().put(Prefix.SSN.getUri() + "inCondition", "inCondition");

	}

	private static void initMeasurementCapabilityStaticInstance(MeasurementCapability measurementCapabilityInstance) {
		measurementCapabilityInstance.getSuperClassesList().add(Property.getPropertyInstance());

		/*
		 * emptying classTypelist
		 */
		measurementCapabilityInstance.setClassTypesList(new Hashtable<>());

		/*
		 * A relation between some aspect of a sensing entity and a property.
		 * For example, from a sensor to the properties it can observe, or from
		 * a deployment to the properties it was installed to observe. Also from
		 * a measurement capability to the property the capability is described
		 * for. (Used in conjunction with ofFeature).
		 * 
		 * A ssn:MeasurementCapability is something that is a ssn:Property and
		 * has a ssn:hasMeasurementProperty property who must be a
		 * ssn:MeasurementProperty .
		 * 
		 * So ssn:MeasurementCapability instance will have a ssn:forProperty
		 * property who must be a ssn:Property and has a ssn:inCondition
		 * property who must be a ssn:Condition
		 */
		measurementCapabilityInstance.getProperties().put("forProperty",
				new ObjectProperty(measurementCapabilityInstance.getMeasurementCapabilitySubjectClassInstance(),
						"forProperty", Prefix.SSN, getPropertyInstance(), true, false));

		measurementCapabilityInstance.getHtblPropUriName().put(Prefix.SSN.getUri() + "forProperty", "forProperty");

		/*
		 * Relation from a MeasurementCapability to a MeasurementProperty. For
		 * example, to an accuracy (see notes at MeasurementCapability).
		 */
		measurementCapabilityInstance.getProperties().put("hasMeasurementProperty",
				new ObjectProperty(measurementCapabilityInstance.getMeasurementCapabilitySubjectClassInstance(),
						"hasMeasurementProperty", Prefix.SSN, MeasurementProperty.getMeasurementPropertyInstance(),
						true, false));

		measurementCapabilityInstance.getHtblPropUriName().put(Prefix.SSN.getUri() + "hasMeasurementProperty",
				"hasMeasurementProperty");

		/*
		 * Describes the prevailing environmental conditions for
		 * MeasurementCapabilites, OperatingConditions and SurvivalRanges. Used
		 * for example to say that a sensor has a particular accuracy in
		 * particular conditions. (see also MeasurementCapability)
		 */
		measurementCapabilityInstance.getProperties().put("inCondition",
				new ObjectProperty(measurementCapabilityInstance.getMeasurementCapabilitySubjectClassInstance(),
						"inCondition", Prefix.SSN, Condition.getConditionInstance(), false, false));
		measurementCapabilityInstance.getHtblPropUriName().put(Prefix.SSN.getUri() + "inCondition", "inCondition");

	}
}
