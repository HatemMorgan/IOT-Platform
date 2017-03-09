package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;

/*
 *  This class maps the ssn:SensingDevice class in the ontology
 *  
 *  A sensing device is a device that implements sensing. 
 */

@Component
public class SensingDevice extends Device {

	private static SensingDevice sensingDeviceInstance;

	public SensingDevice() {
		super("SensingDevice", "http://purl.oclc.org/NET/ssnx/ssn#SensingDevice", Prefixes.SSN);
		init();
	}

	public SensingDevice(String nothing) {
		super("SensingDevice", "http://purl.oclc.org/NET/ssnx/ssn#SensingDevice", Prefixes.SSN);
	}

	public synchronized static SensingDevice getSensingDeviceInstance() {
		if (sensingDeviceInstance == null) {
			System.out.println("hreee");
			sensingDeviceInstance = new SensingDevice(null);
		}

		return sensingDeviceInstance;
	}

	private void init() {
		/*
		 * Add Device as superClass for sensor in superClassList
		 */

		this.getSuperClassesList().add(Device.getDeviceInstance());

		// Add properties of Sensor Class

		/*
		 * Links a sensor or an attribute with the quantity kind it measures
		 * (e.g. A sensor -sensor1- measures temperature: sensor1
		 * hasQuantityKind temperature). It is one to one relationship because a
		 * sensor has only one qunatityKind
		 */
		this.getProperties().put("hasQuantityKind", new ObjectProperty("hasQuantityKind", Prefixes.IOT_LITE,
				QuantityKind.getQuantityKindInstance(), false, false));

		/*
		 * A relation between an entity that implements a method in some
		 * executable way and the description of an algorithm, procedure or
		 * method. For example, between a Sensor and the scientific measuring
		 * method that the Sensor uses to observe a Property.
		 */
		this.getProperties().put("implements",
				new ObjectProperty("implements", Prefixes.SSN, Sensing.getSensingInstance(), false, false));

		/*
		 * Relation between a producer and a produced entity: for example,
		 * between a sensor and the produced output. it is one to many
		 * relationship
		 */
		this.getProperties().put("isProducedBy",
				new ObjectProperty("isProducedBy", Prefixes.SSN, SensorOutput.getSensorOutputInstance(), true, false));

		/*
		 * Relation between a Sensor and a Property that the sensor can observe.
		 * It points to a property observed by a sensor (e.g., temperature,
		 * acceleration, wind speed). it is a one to many relationship
		 */
		this.getProperties().put("observes",
				new ObjectProperty("observes", Prefixes.SSN, Property.getPropertyInstance(), true, false));

		/*
		 * A relation from a sensor to the Stimulus that the sensor can detect.
		 * The Stimulus itself will be serving as a proxy for (see isProxyOf)
		 * some observable property. It is a one to many relationship
		 */
		this.getProperties().put("detects",
				new ObjectProperty("detects", Prefixes.SSN, Stimulus.getStimulusInstance(), true, false));

		/*
		 * Relation from a Sensor to a MeasurementCapability describing the
		 * measurement properties of the sensor. it is one to many relationship
		 */
		this.getProperties().put("hasMeasurementCapability", new ObjectProperty("hasMeasurementCapability",
				Prefixes.SSN, MeasurementCapability.getMeasurementCapabilityInstance(), true, false));

		/*
		 * It describes the relation that a device can has attached
		 * communicating device . ie: A module (device) has communicating device
		 * BLE (communicating device) attached to it . it is one to one
		 * relationship
		 */
		this.getProperties().put("hasCommunicatingDevice", new ObjectProperty("hasCommunicatingDevice",
				Prefixes.IOT_PLATFORM, CommunicatingDevice.getCommunicatingDeviceInstance(), false, true));

		this.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "hasQuantityKind", "hasQuantityKind");
		this.getHtblPropUriName().put(Prefixes.SSN.getUri() + "implements", "implements");
		this.getHtblPropUriName().put(Prefixes.SSN.getUri() + "isProducedBy", "isProducedBy");
		this.getHtblPropUriName().put(Prefixes.SSN.getUri() + "observes", "observes");
		this.getHtblPropUriName().put(Prefixes.SSN.getUri() + "detects", "detects");
		this.getHtblPropUriName().put(Prefixes.SSN.getUri() + "hasMeasurementCapability", "hasMeasurementCapability");
		this.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "hasCommunicatingDevice",
				"hasCommunicatingDevice");
	}

}
