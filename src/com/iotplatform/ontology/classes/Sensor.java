package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;

/*
 *   This class maps the ssn:Sensor class in the ontology
 *   
 *   A sensor can do (implements) sensing: that is, a sensor is any entity that can follow a sensing method
 *   and thus observe some Property of a FeatureOfInterest. 
 *   Sensors may be physical devices, computational methods, a laboratory setup with a person following a method,
 *   or any other thing that can follow a Sensing Method to observe a Property.
 */

@Component
public class Sensor extends Device {

	private static Sensor sensorInstance;

	public Sensor() {
		super("Sensor", "http://purl.oclc.org/NET/ssnx/ssn#Sensor", Prefixes.SSN);
		init();
	}

	public Sensor(String name, String uri, Prefixes prefix) {
		super(name, uri, prefix);
		init();

	}

	/*
	 * String nothing parameter is added for overloading constructor technique
	 * because I need to initialize an instance without having properties and it
	 * will be always passed by null
	 */
	public Sensor(String nothing) {
		super("Sensor", "http://purl.oclc.org/NET/ssnx/ssn#Sensor", Prefixes.SSN);
	}

	public synchronized static Sensor getSensorInstance() {
		if (sensorInstance == null)
			sensorInstance = new Sensor(null);

		return sensorInstance;
	}

	private void init() {
		/*
		 * Add Device as superClass for sensor in superClassList
		 */

		this.getSuperClassesList().add(Device.getDeviceInstance());

		// Add properties of Sensor Class

		this.getProperties().put("hasQuantityKind", new ObjectProperty("hasQuantityKind", Prefixes.IOT_LITE,
				QuantityKind.getQuantityKindInstance(), false, false));

		this.getProperties().put("implements",
				new ObjectProperty("implements", Prefixes.SSN, Sensing.getSensingInstance(), false, false));

		this.getProperties().put("hasCoverage",
				new ObjectProperty("hasCoverage", Prefixes.IOT_LITE, Coverage.getCoverageInstance(), false, false));

		this.getProperties().put("hasCoverage",
				new ObjectProperty("hasCoverage", Prefixes.IOT_LITE, Coverage.getCoverageInstance(), false, false));

		this.getProperties().put("hasCoverage",
				new ObjectProperty("hasCoverage", Prefixes.IOT_LITE, Coverage.getCoverageInstance(), false, false));

		this.getProperties().put("hasCoverage",
				new ObjectProperty("hasCoverage", Prefixes.IOT_LITE, Coverage.getCoverageInstance(), false, false));

		this.getProperties().put("hasCoverage",
				new ObjectProperty("hasCoverage", Prefixes.IOT_LITE, Coverage.getCoverageInstance(), false, false));

		this.getProperties().put("hasCoverage",
				new ObjectProperty("hasCoverage", Prefixes.IOT_LITE, Coverage.getCoverageInstance(), false, false));

		this.getProperties().put("hasCoverage",
				new ObjectProperty("hasCoverage", Prefixes.IOT_LITE, Coverage.getCoverageInstance(), false, false));
	}
}
