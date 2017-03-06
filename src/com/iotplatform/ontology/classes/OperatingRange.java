package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Prefixes;

/*
 * This class maps ssn:OperatingRange class in the ontology
 * 
 * The environmental conditions and characteristics of a system/sensor's normal operating environment.  
 * Can be used to specify for example the standard environmental conditions in which the sensor is expected to 
 * operate (a Condition with no OperatingProperty), or how the environmental and other operating properties relate: 
 * i.e., that the maintenance schedule or power requirements differ according to the conditions. 
 * 
 * If the Operating Range exceeded ,the system must be maintained
 */

@Component
public class OperatingRange extends Property {

	private static OperatingRange operatingRangeInstance;

	public OperatingRange() {
		super("OperatingRange", "http://purl.oclc.org/NET/ssnx/ssn#OperatingRange", Prefixes.SSN);
		init();
	}

	/*
	 * String nothing parameter is added for overloading constructor technique
	 * because I need to initialize an instance without having properties and it
	 * will be always passed by null
	 */
	public OperatingRange(String nothing) {
		super("OperatingRange", "http://purl.oclc.org/NET/ssnx/ssn#OperatingRange", Prefixes.SSN);
	}

	public synchronized static OperatingRange getOperatingRangeInstance() {
		if (operatingRangeInstance == null)
			operatingRangeInstance = new OperatingRange(null);

		return operatingRangeInstance;
	}

	private void init() {

	}
}
