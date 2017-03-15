package com.iotplatform.ontology.classes;

import java.util.Hashtable;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

/*
 *  This class maps MeasurementProperty Class in the ontology
 *  
 *  An identifiable and observable characteristic of a sensor's observations or ability to make observations.
 */

@Component
public class MeasurementProperty extends Property {

	private Hashtable<String, Class> measurementPropertyTypesList;
	private static MeasurementProperty measurementPropertyInstance;

	public MeasurementProperty() {
		super("MeasurementProperty", "http://purl.oclc.org/NET/ssnx/ssn#MeasurementProperty", Prefixes.SSN);
		measurementPropertyTypesList = new Hashtable<>();
		init();
	}

	/*
	 * This constructor is used to perform overloading constructor technique and
	 * the parameter String nothing will be passed always with null
	 * 
	 * I have done this overloaded constructor to instantiate the static
	 * systemInstance to avoid java.lang.StackOverflowError exception that Occur
	 * when calling init() to add properties to systemInstance
	 * 
	 */
	public MeasurementProperty(String nothing) {
		super("MeasurementProperty", "http://purl.oclc.org/NET/ssnx/ssn#MeasurementProperty", Prefixes.SSN);
		measurementPropertyTypesList = new Hashtable<>();
	}

	public synchronized static MeasurementProperty getMeasurementPropertyInstance() {
		if (measurementPropertyInstance == null) {
			measurementPropertyInstance = new MeasurementProperty(null);
			initMeasurmentPropertyStaticInstance(measurementPropertyInstance);
		}

		return measurementPropertyInstance;
	}

	private void init() {

		/*
		 * The closeness of agreement between the value of an observation and
		 * the true value of the observed quality.
		 */
		Class accuracy = new Class("Accuracy", "http://purl.oclc.org/NET/ssnx/ssn#Accuracy", Prefixes.SSN, null);
		accuracy.getSuperClassesList().add(MeasurementProperty.getMeasurementPropertyInstance());
		measurementPropertyTypesList.put("Accuracy", accuracy);

		/*
		 * An observed value for which the probability of falsely claiming the
		 * absence of a component in a material is Î², given a probability Î± of
		 * falsely claiming its presence.
		 */

		Class detectionLimit = new Class("DetectionLimit", "http://purl.oclc.org/NET/ssnx/ssn#DetectionLimit",
				Prefixes.SSN, null);
		detectionLimit.getSuperClassesList().add(MeasurementProperty.getMeasurementPropertyInstance());
		measurementPropertyTypesList.put("DetectionLimit", detectionLimit);

		/*
		 * A, continuous or incremental, change in the reported values of
		 * observations over time for an unchanging quality.
		 */

		Class drift = new Class("Drift", "http://purl.oclc.org/NET/ssnx/ssn#Drift", Prefixes.SSN, null);
		drift.getSuperClassesList().add(MeasurementProperty.getMeasurementPropertyInstance());
		measurementPropertyTypesList.put("Drift", drift);

		/*
		 * The smallest possible time between one observation and the next.
		 */

		Class frequency = new Class("Frequency", "http://purl.oclc.org/NET/ssnx/ssn#Frequency", Prefixes.SSN, null);
		frequency.getSuperClassesList().add(MeasurementProperty.getMeasurementPropertyInstance());
		measurementPropertyTypesList.put("Frequency", frequency);

		/*
		 * The time between a request for an observation and the sensor
		 * providing a result.
		 */

		Class latency = new Class("Latency", "http://purl.oclc.org/NET/ssnx/ssn#Latency", Prefixes.SSN, null);
		latency.getSuperClassesList().add(MeasurementProperty.getMeasurementPropertyInstance());
		measurementPropertyTypesList.put("Latency", latency);

		/*
		 * The set of values that the sensor can return as the result of an
		 * observation under the defined conditions with the defined measurement
		 * properties. (If no conditions are specified or the conditions do not
		 * specify a range for the observed qualities, the measurement range is
		 * to be taken as the condition for the observed qualities.)
		 */

		Class measurmentRange = new Class("MeasurementRange", "http://purl.oclc.org/NET/ssnx/ssn#MeasurementRange",
				Prefixes.SSN, null);
		measurmentRange.getSuperClassesList().add(MeasurementProperty.getMeasurementPropertyInstance());
		measurementPropertyTypesList.put("MeasurementRange", measurmentRange);

		/*
		 * The closeness of agreement between replicate observations on an
		 * unchanged or similar quality value: i.e., a measure of a sensor's
		 * ability to consitently reproduce an observation.
		 */

		Class precision = new Class("Precision", "http://purl.oclc.org/NET/ssnx/ssn#Precision", Prefixes.SSN, null);
		precision.getSuperClassesList().add(MeasurementProperty.getMeasurementPropertyInstance());
		measurementPropertyTypesList.put("Precision", precision);

		/*
		 * The smallest difference in the value of a quality being observed that
		 * would result in perceptably different values of observation results.
		 */

		Class resolution = new Class("Resolution", "http://purl.oclc.org/NET/ssnx/ssn#Resolution", Prefixes.SSN, null);
		resolution.getSuperClassesList().add(MeasurementProperty.getMeasurementPropertyInstance());
		measurementPropertyTypesList.put("Resolution", resolution);

		/*
		 * The time between a (step) change inthe value of an observed quality
		 * and a sensor (possibly with specified error) 'settling' on an
		 * observed value.
		 */

		Class responseTime = new Class("ResponseTime", "http://purl.oclc.org/NET/ssnx/ssn#ResponseTime", Prefixes.SSN,
				null);
		responseTime.getSuperClassesList().add(MeasurementProperty.getMeasurementPropertyInstance());
		measurementPropertyTypesList.put("ResponseTime", responseTime);

		/*
		 * Selectivity is a property of a sensor whereby it provides observed
		 * values for one or more qualities such that the values of each quality
		 * are independent of other qualities in the phenomenon, body, or
		 * substance being investigated.
		 */

		Class selectivity = new Class("Selectivity", "http://purl.oclc.org/NET/ssnx/ssn#Selectivity", Prefixes.SSN,
				null);
		selectivity.getSuperClassesList().add(MeasurementProperty.getMeasurementPropertyInstance());
		measurementPropertyTypesList.put("Selectivity", selectivity);

		/*
		 * Sensitivity is the quotient of the change in a result of sensor and
		 * the corresponding change in a value of a quality being observed.
		 */

		Class sensitivity = new Class("Sensitivity", "http://purl.oclc.org/NET/ssnx/ssn#Sensitivity", Prefixes.SSN,
				null);
		sensitivity.getSuperClassesList().add(MeasurementProperty.getMeasurementPropertyInstance());
		measurementPropertyTypesList.put("Sensitivity", sensitivity);

	}

	private static void initMeasurmentPropertyStaticInstance(MeasurementProperty measurementPropertyInstance) {
		/*
		 * The closeness of agreement between the value of an observation and
		 * the true value of the observed quality.
		 */
		Class accuracy = new Class("Accuracy", "http://purl.oclc.org/NET/ssnx/ssn#Accuracy", Prefixes.SSN, null);
		accuracy.getSuperClassesList().add(MeasurementProperty.getMeasurementPropertyInstance());
		measurementPropertyInstance.getMeasurementPropertyTypesList().put("Accuracy", accuracy);

		/*
		 * An observed value for which the probability of falsely claiming the
		 * absence of a component in a material is Î², given a probability Î± of
		 * falsely claiming its presence.
		 */

		Class detectionLimit = new Class("DetectionLimit", "http://purl.oclc.org/NET/ssnx/ssn#DetectionLimit",
				Prefixes.SSN, null);
		detectionLimit.getSuperClassesList().add(MeasurementProperty.getMeasurementPropertyInstance());
		measurementPropertyInstance.getMeasurementPropertyTypesList().put("DetectionLimit", detectionLimit);

		/*
		 * A, continuous or incremental, change in the reported values of
		 * observations over time for an unchanging quality.
		 */

		Class drift = new Class("Drift", "http://purl.oclc.org/NET/ssnx/ssn#Drift", Prefixes.SSN, null);
		drift.getSuperClassesList().add(MeasurementProperty.getMeasurementPropertyInstance());
		measurementPropertyInstance.getMeasurementPropertyTypesList().put("Drift", drift);

		/*
		 * The smallest possible time between one observation and the next.
		 */

		Class frequency = new Class("Frequency", "http://purl.oclc.org/NET/ssnx/ssn#Frequency", Prefixes.SSN, null);
		frequency.getSuperClassesList().add(MeasurementProperty.getMeasurementPropertyInstance());
		measurementPropertyInstance.getMeasurementPropertyTypesList().put("Frequency", frequency);

		/*
		 * The time between a request for an observation and the sensor
		 * providing a result.
		 */

		Class latency = new Class("Latency", "http://purl.oclc.org/NET/ssnx/ssn#Latency", Prefixes.SSN, null);
		latency.getSuperClassesList().add(MeasurementProperty.getMeasurementPropertyInstance());
		measurementPropertyInstance.getMeasurementPropertyTypesList().put("Latency", latency);

		/*
		 * The set of values that the sensor can return as the result of an
		 * observation under the defined conditions with the defined measurement
		 * properties. (If no conditions are specified or the conditions do not
		 * specify a range for the observed qualities, the measurement range is
		 * to be taken as the condition for the observed qualities.)
		 */

		Class measurmentRange = new Class("MeasurementRange", "http://purl.oclc.org/NET/ssnx/ssn#MeasurementRange",
				Prefixes.SSN, null);
		measurmentRange.getSuperClassesList().add(MeasurementProperty.getMeasurementPropertyInstance());
		measurementPropertyInstance.getMeasurementPropertyTypesList().put("MeasurementRange", measurmentRange);

		/*
		 * The closeness of agreement between replicate observations on an
		 * unchanged or similar quality value: i.e., a measure of a sensor's
		 * ability to consitently reproduce an observation.
		 */

		Class precision = new Class("Precision", "http://purl.oclc.org/NET/ssnx/ssn#Precision", Prefixes.SSN, null);
		precision.getSuperClassesList().add(MeasurementProperty.getMeasurementPropertyInstance());
		measurementPropertyInstance.getMeasurementPropertyTypesList().put("Precision", precision);

		/*
		 * The smallest difference in the value of a quality being observed that
		 * would result in perceptably different values of observation results.
		 */

		Class resolution = new Class("Resolution", "http://purl.oclc.org/NET/ssnx/ssn#Resolution", Prefixes.SSN, null);
		resolution.getSuperClassesList().add(MeasurementProperty.getMeasurementPropertyInstance());
		measurementPropertyInstance.getMeasurementPropertyTypesList().put("Resolution", resolution);

		/*
		 * The time between a (step) change inthe value of an observed quality
		 * and a sensor (possibly with specified error) 'settling' on an
		 * observed value.
		 */

		Class responseTime = new Class("ResponseTime", "http://purl.oclc.org/NET/ssnx/ssn#ResponseTime", Prefixes.SSN,
				null);
		responseTime.getSuperClassesList().add(MeasurementProperty.getMeasurementPropertyInstance());
		measurementPropertyInstance.getMeasurementPropertyTypesList().put("ResponseTime", responseTime);

		/*
		 * Selectivity is a property of a sensor whereby it provides observed
		 * values for one or more qualities such that the values of each quality
		 * are independent of other qualities in the phenomenon, body, or
		 * substance being investigated.
		 */

		Class selectivity = new Class("Selectivity", "http://purl.oclc.org/NET/ssnx/ssn#Selectivity", Prefixes.SSN,
				null);
		selectivity.getSuperClassesList().add(MeasurementProperty.getMeasurementPropertyInstance());
		measurementPropertyInstance.getMeasurementPropertyTypesList().put("Selectivity", selectivity);

		/*
		 * Sensitivity is the quotient of the change in a result of sensor and
		 * the corresponding change in a value of a quality being observed.
		 */

		Class sensitivity = new Class("Sensitivity", "http://purl.oclc.org/NET/ssnx/ssn#Sensitivity", Prefixes.SSN,
				null);
		sensitivity.getSuperClassesList().add(MeasurementProperty.getMeasurementPropertyInstance());
		measurementPropertyInstance.getMeasurementPropertyTypesList().put("Sensitivity", sensitivity);
	}

	public Hashtable<String, Class> getMeasurementPropertyTypesList() {
		return measurementPropertyTypesList;
	}

}
