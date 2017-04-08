package com.iotplatform.ontology.classes;

import java.util.Hashtable;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefix;

/*
 * This class maps ssn:OperatingRange class in the ontology
 * 
 * The environmental conditions and characteristics of a system/sensor's normal operating environment.  
 * Can be used to specify for example the standard environmental conditions in which the sensor is expected to 
 * operate (a Condition with no OperatingProperty), or how the environmental and other operating properties relate: 
 * i.e., that the maintenance schedule or power requirements differ according to the conditions. 
 * 
 * If the Operating Range exceeded ,the system must be maintained
 * 
 *  It is a wrapper for condition and operating property and it is used by System Class to describe that in certain
 *  condition there is an OperatingProperty instance which has a value or a range
 *  
 */

@Component
public class OperatingRange extends Property {

	private static OperatingRange operatingRangeInstance;
	private Class operatingRangeSubjectClassInstance;

	public OperatingRange() {
		super("OperatingRange", "http://purl.oclc.org/NET/ssnx/ssn#OperatingRange", Prefix.SSN, null, false);
		init();
	}

	public OperatingRange(String nothing) {
		super("OperatingRange", "http://purl.oclc.org/NET/ssnx/ssn#OperatingRange", Prefix.SSN, null, false, null);
	}

	private Class getOperatingRangeSubjectClassInstance() {
		if (operatingRangeSubjectClassInstance == null)
			operatingRangeSubjectClassInstance = new Class("OperatingRange",
					"http://purl.oclc.org/NET/ssnx/ssn#OperatingRange", Prefix.SSN, null, false);

		return operatingRangeSubjectClassInstance;
	}

	public synchronized static OperatingRange getOperatingRangeInstance() {
		if (operatingRangeInstance == null) {
			operatingRangeInstance = new OperatingRange();
			initOperatingRangeStaticInstance(operatingRangeInstance);
			initPropertyStaticInstance(operatingRangeInstance);
		}
		return operatingRangeInstance;
	}

	private void init() {

		/*
		 * Relation from an OperatingRange to a Property. For example, to a
		 * battery lifetime.
		 */
		this.getProperties().put("hasOperatingProperty", new ObjectProperty(getOperatingRangeSubjectClassInstance(),
				"hasOperatingProperty", Prefix.SSN, OperatingProperty.getOperatingPropertyInstance(), false, false));

		/*
		 * Describes the prevailing environmental conditions for
		 * MeasurementCapabilites, OperatingConditions and SurvivalRanges. Used
		 * for example to say that a sensor has a particular accuracy in
		 * particular conditions. (see also MeasurementCapability)
		 */
		this.getProperties().put("inCondition", new ObjectProperty(getOperatingRangeSubjectClassInstance(),
				"inCondition", Prefix.SSN, Condition.getConditionInstance(), false, false));

		this.getHtblPropUriName().put(Prefix.SSN.getUri() + "hasOperatingProperty", "hasOperatingProperty");
		this.getHtblPropUriName().put(Prefix.SSN.getUri() + "inCondition", "inCondition");

		this.getSuperClassesList().add(Property.getPropertyInstance());
	}

	private static void initOperatingRangeStaticInstance(OperatingRange operatingRangeInstance) {

		/*
		 * emptying classTypelist
		 */
		operatingRangeInstance.setClassTypesList(new Hashtable<>());
		
		/*
		 * Relation from an OperatingRange to a Property. For example, to a
		 * battery lifetime.
		 */
		operatingRangeInstance.getProperties().put("hasOperatingProperty",
				new ObjectProperty(operatingRangeInstance.getOperatingRangeSubjectClassInstance(),
						"hasOperatingProperty", Prefix.SSN, OperatingProperty.getOperatingPropertyInstance(), false,
						false));

		/*
		 * Describes the prevailing environmental conditions for
		 * MeasurementCapabilites, OperatingConditions and SurvivalRanges. Used
		 * for example to say that a sensor has a particular accuracy in
		 * particular conditions. (see also MeasurementCapability)
		 */
		operatingRangeInstance.getProperties().put("inCondition",
				new ObjectProperty(operatingRangeInstance.getOperatingRangeSubjectClassInstance(), "inCondition",
						Prefix.SSN, Condition.getConditionInstance(), false, false));

		operatingRangeInstance.getHtblPropUriName().put(Prefix.SSN.getUri() + "hasOperatingProperty",
				"hasOperatingProperty");
		operatingRangeInstance.getHtblPropUriName().put(Prefix.SSN.getUri() + "inCondition", "inCondition");

		operatingRangeInstance.getSuperClassesList().add(Property.getPropertyInstance());
	}

	public static void main(String[] args) {
		OperatingRange operatingRange = new OperatingRange();

		System.out.println(operatingRange.getProperties());
		System.out.println(OperatingRange.getOperatingRangeInstance().getProperties());

		System.out.println(operatingRange.getHtblPropUriName().size());
		System.out.println(OperatingRange.getOperatingRangeInstance().getHtblPropUriName().size());

		System.out.println(operatingRange.getSuperClassesList());
		System.out.println(OperatingRange.getOperatingRangeInstance().getSuperClassesList());

		System.out.println(operatingRange.getClassTypesList());
		System.out.println(OperatingRange.getOperatingRangeInstance().getClassTypesList());

	}
}
