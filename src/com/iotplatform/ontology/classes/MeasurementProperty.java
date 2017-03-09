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

	public MeasurementProperty() {
		super("MeasurementProperty", "http://purl.oclc.org/NET/ssnx/ssn#MeasurementProperty", Prefixes.SSN);
		init();
	}

	private void init() {
		measurementPropertyTypesList = new Hashtable<>();

		/*
		 * The closeness of agreement between the value of an observation and
		 * the true value of the observed quality.
		 */
		measurementPropertyTypesList.put("Accuracy",
				new Class("Accuracy", "http://purl.oclc.org/NET/ssnx/ssn#Accuracy", Prefixes.SSN));

		/*
		 * An observed value for which the probability of falsely claiming the
		 * absence of a component in a material is Î², given a probability Î± of
		 * falsely claiming its presence.
		 */
		measurementPropertyTypesList.put("DetectionLimit",
				new Class("DetectionLimit", "http://purl.oclc.org/NET/ssnx/ssn#DetectionLimit", Prefixes.SSN));

		/*
		 * A, continuous or incremental, change in the reported values of
		 * observations over time for an unchanging quality.
		 */
		measurementPropertyTypesList.put("Drift",
				new Class("Drift", "http://purl.oclc.org/NET/ssnx/ssn#Drift", Prefixes.SSN));

		/*
		 * The smallest possible time between one observation and the next.
		 */
		measurementPropertyTypesList.put("Frequency",
				new Class("Frequency", "http://purl.oclc.org/NET/ssnx/ssn#Frequency", Prefixes.SSN));

		/*
		 * The time between a request for an observation and the sensor
		 * providing a result.
		 */
		measurementPropertyTypesList.put("Latency",
				new Class("Latency", "http://purl.oclc.org/NET/ssnx/ssn#Latency", Prefixes.SSN));

		/*
		 * The set of values that the sensor can return as the result of an
		 * observation under the defined conditions with the defined measurement
		 * properties. (If no conditions are specified or the conditions do not
		 * specify a range for the observed qualities, the measurement range is
		 * to be taken as the condition for the observed qualities.)
		 */
		measurementPropertyTypesList.put("MeasurementRange",
				new Class("MeasurementRange", "http://purl.oclc.org/NET/ssnx/ssn#MeasurementRange", Prefixes.SSN));

		/*
		 * The closeness of agreement between replicate observations on an
		 * unchanged or similar quality value: i.e., a measure of a sensor's
		 * ability to consitently reproduce an observation.
		 */
		measurementPropertyTypesList.put("Precision",
				new Class("Precision", "http://purl.oclc.org/NET/ssnx/ssn#Precision", Prefixes.SSN));

		/*
		 * The smallest difference in the value of a quality being observed that
		 * would result in perceptably different values of observation results.
		 */
		measurementPropertyTypesList.put("Resolution",
				new Class("Resolution", "http://purl.oclc.org/NET/ssnx/ssn#Resolution", Prefixes.SSN));

		/*
		 * The time between a (step) change inthe value of an observed quality
		 * and a sensor (possibly with specified error) 'settling' on an
		 * observed value.
		 */
		measurementPropertyTypesList.put("ResponseTime",
				new Class("ResponseTime", "http://purl.oclc.org/NET/ssnx/ssn#ResponseTime", Prefixes.SSN));

		/*
		 * Selectivity is a property of a sensor whereby it provides observed
		 * values for one or more qualities such that the values of each quality
		 * are independent of other qualities in the phenomenon, body, or
		 * substance being investigated.
		 */
		measurementPropertyTypesList.put("Selectivity",
				new Class("Selectivity", "http://purl.oclc.org/NET/ssnx/ssn#Selectivity", Prefixes.SSN));
		/*
		 * Sensitivity is the quotient of the change in a result of sensor and
		 * the corresponding change in a value of a quality being observed.
		 */
		measurementPropertyTypesList.put("Sensitivity",
				new Class("Sensitivity", "http://purl.oclc.org/NET/ssnx/ssn#Sensitivity", Prefixes.SSN));

	}
}
