package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
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
	private Class sensorSubjectClassInstance;

	public Sensor() {
		super("Sensor", "http://purl.oclc.org/NET/ssnx/ssn#Sensor", Prefixes.SSN, null, false);
		init();
	}

	public Sensor(String nothing) {
		super("Sensor", "http://purl.oclc.org/NET/ssnx/ssn#Sensor", Prefixes.SSN, null, false);
	}

	public Sensor(String name, String uri, Prefixes prefix, String uniqueIdentifierPropertyName,
			boolean hasTypeClasses) {
		super(name, uri, prefix, uniqueIdentifierPropertyName, hasTypeClasses);
		init();

	}

	private Class getSensorSubjectClassInstance() {
		if (sensorSubjectClassInstance == null)
			sensorSubjectClassInstance = new Class("Sensor", "http://purl.oclc.org/NET/ssnx/ssn#Sensor", Prefixes.SSN,
					null, false);

		return sensorSubjectClassInstance;
	}

	public synchronized static Sensor getSensorInstance() {
		if (sensorInstance == null) {
			sensorInstance = new Sensor(null);
			initSensorInstance(sensorInstance);
		}
		return sensorInstance;
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
		this.getProperties().put("hasQuantityKind", new ObjectProperty(getSensorSubjectClassInstance(),
				"hasQuantityKind", Prefixes.IOT_LITE, QuantityKind.getQuantityKindInstance(), false, false));

		/*
		 * A relation between an entity that implements a method in some
		 * executable way and the description of an algorithm, procedure or
		 * method. For example, between a Sensor and the scientific measuring
		 * method that the Sensor uses to observe a Property.
		 */
		this.getProperties().put("implements", new ObjectProperty(getSensorSubjectClassInstance(), "implements",
				Prefixes.SSN, Sensing.getSensingInstance(), false, false));

		/*
		 * Relation between a producer and a produced entity: for example,
		 * between a sensor and the produced output. it is one to many
		 * relationship
		 */
		this.getProperties().put("isProducedBy", new ObjectProperty(getSensorSubjectClassInstance(), "isProducedBy",
				Prefixes.SSN, SensorOutput.getSensorOutputInstance(), true, false));

		/*
		 * Relation between a Sensor and a Property that the sensor can observe.
		 * It points to a property observed by a sensor (e.g., temperature,
		 * acceleration, wind speed). it is a one to many relationship
		 */
		this.getProperties().put("observes", new ObjectProperty(getSensorSubjectClassInstance(), "observes",
				Prefixes.SSN, Property.getPropertyInstance(), true, false));

		/*
		 * A relation from a sensor to the Stimulus that the sensor can detect.
		 * The Stimulus itself will be serving as a proxy for (see isProxyOf)
		 * some observable property. It is a one to many relationship
		 */
		this.getProperties().put("detects", new ObjectProperty(getSensorSubjectClassInstance(), "detects", Prefixes.SSN,
				Stimulus.getStimulusInstance(), true, false));

		/*
		 * Relation from a Sensor to a MeasurementCapability describing the
		 * measurement properties of the sensor. it is one to many relationship
		 */
		this.getProperties().put("hasMeasurementCapability",
				new ObjectProperty(getSensorSubjectClassInstance(), "hasMeasurementCapability", Prefixes.SSN,
						MeasurementCapability.getMeasurementCapabilityInstance(), true, false));

		/*
		 * It describes the relation that a device can has attached
		 * communicating device . ie: A module (device) has communicating device
		 * BLE (communicating device) attached to it . it is one to one
		 * relationship
		 */
		this.getProperties().put("hasCommunicatingDevice",
				new ObjectProperty(getSensorSubjectClassInstance(), "hasCommunicatingDevice", Prefixes.IOT_PLATFORM,
						CommunicatingDevice.getCommunicatingDeviceInstance(), false, true));

		/*
		 * It describes the relation that a device can has attached snesing
		 * device (sensor) . ie: A module (device) has sensing device Tempreture
		 * sensor (sensing device )attached to it .
		 */
		// this.getProperties().put("hasSensingDevice", new
		// ObjectProperty("hasSensingDevice", Prefixes.IOT_LITE,
		// SensingDevice.getSensingDeviceInstance(), false, true));

		this.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "hasQuantityKind", "hasQuantityKind");
		this.getHtblPropUriName().put(Prefixes.SSN.getUri() + "implements", "implements");
		this.getHtblPropUriName().put(Prefixes.SSN.getUri() + "isProducedBy", "isProducedBy");
		this.getHtblPropUriName().put(Prefixes.SSN.getUri() + "observes", "observes");
		this.getHtblPropUriName().put(Prefixes.SSN.getUri() + "detects", "detects");
		this.getHtblPropUriName().put(Prefixes.SSN.getUri() + "hasMeasurementCapability", "hasMeasurementCapability");
		this.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "hasCommunicatingDevice",
				"hasCommunicatingDevice");
		// this.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() +
		// "hasSensingDevice", "hasSensingDevice");

	}

	public static void initSensorInstance(Sensor sensorInstance) {
		/*
		 * Add Device as superClass for sensor in superClassList
		 */

		sensorInstance.getSuperClassesList().add(Device.getDeviceInstance());

		// Add properties of Sensor Class

		/*
		 * Links a sensor or an attribute with the quantity kind it measures
		 * (e.g. A sensor -sensor1- measures temperature: sensor1
		 * hasQuantityKind temperature). It is one to one relationship because a
		 * sensor has only one qunatityKind
		 */
		sensorInstance.getProperties().put("hasQuantityKind",
				new ObjectProperty(sensorInstance.getSensorSubjectClassInstance(), "hasQuantityKind", Prefixes.IOT_LITE,
						QuantityKind.getQuantityKindInstance(), false, false));

		/*
		 * A relation between an entity that implements a method in some
		 * executable way and the description of an algorithm, procedure or
		 * method. For example, between a Sensor and the scientific measuring
		 * method that the Sensor uses to observe a Property.
		 */
		sensorInstance.getProperties().put("implements",
				new ObjectProperty(sensorInstance.getSensorSubjectClassInstance(), "implements", Prefixes.SSN,
						Sensing.getSensingInstance(), false, false));

		/*
		 * Relation between a producer and a produced entity: for example,
		 * between a sensor and the produced output. it is one to many
		 * relationship
		 */
		sensorInstance.getProperties().put("isProducedBy",
				new ObjectProperty(sensorInstance.getSensorSubjectClassInstance(), "isProducedBy", Prefixes.SSN,
						SensorOutput.getSensorOutputInstance(), true, false));

		/*
		 * Relation between a Sensor and a Property that the sensor can observe.
		 * It points to a property observed by a sensor (e.g., temperature,
		 * acceleration, wind speed). it is a one to many relationship
		 */
		sensorInstance.getProperties().put("observes",
				new ObjectProperty(sensorInstance.getSensorSubjectClassInstance(), "observes", Prefixes.SSN,
						Property.getPropertyInstance(), true, false));

		/*
		 * A relation from a sensor to the Stimulus that the sensor can detect.
		 * The Stimulus itself will be serving as a proxy for (see isProxyOf)
		 * some observable property. It is a one to many relationship
		 */
		sensorInstance.getProperties().put("detects", new ObjectProperty(sensorInstance.getSensorSubjectClassInstance(),
				"detects", Prefixes.SSN, Stimulus.getStimulusInstance(), true, false));

		/*
		 * Relation from a Sensor to a MeasurementCapability describing the
		 * measurement properties of the sensor. it is one to many relationship
		 */
		sensorInstance.getProperties().put("hasMeasurementCapability",
				new ObjectProperty(sensorInstance.getSensorSubjectClassInstance(), "hasMeasurementCapability",
						Prefixes.SSN, MeasurementCapability.getMeasurementCapabilityInstance(), true, false));

		/*
		 * It describes the relation that a device can has attached
		 * communicating device . ie: A module (device) has communicating device
		 * BLE (communicating device) attached to it . it is one to one
		 * relationship
		 */
		sensorInstance.getProperties().put("hasCommunicatingDevice",
				new ObjectProperty(sensorInstance.getSensorSubjectClassInstance(), "hasCommunicatingDevice",
						Prefixes.IOT_PLATFORM, CommunicatingDevice.getCommunicatingDeviceInstance(), false, true));

		/*
		 * It describes the relation that a device can has attached snesing
		 * device (sensor) . ie: A module (device) has sensing device Tempreture
		 * sensor (sensing device )attached to it .
		 */
		// this.getProperties().put("hasSensingDevice", new
		// ObjectProperty("hasSensingDevice", Prefixes.IOT_LITE,
		// SensingDevice.getSensingDeviceInstance(), false, true));

		sensorInstance.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "hasQuantityKind", "hasQuantityKind");
		sensorInstance.getHtblPropUriName().put(Prefixes.SSN.getUri() + "implements", "implements");
		sensorInstance.getHtblPropUriName().put(Prefixes.SSN.getUri() + "isProducedBy", "isProducedBy");
		sensorInstance.getHtblPropUriName().put(Prefixes.SSN.getUri() + "observes", "observes");
		sensorInstance.getHtblPropUriName().put(Prefixes.SSN.getUri() + "detects", "detects");
		sensorInstance.getHtblPropUriName().put(Prefixes.SSN.getUri() + "hasMeasurementCapability",
				"hasMeasurementCapability");
		sensorInstance.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "hasCommunicatingDevice",
				"hasCommunicatingDevice");
		// sensorInstance.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() +
		// "hasSensingDevice", "hasSensingDevice");

	}

	public static void main(String[] args) {
		Sensor sensor = new Sensor();

		System.out.println(sensor.getProperties().size());
		System.out.println(Sensor.getSensorInstance().getProperties().size());

		System.out.println(sensor.getHtblPropUriName().size());
		System.out.println(Sensor.getSensorInstance().getHtblPropUriName().size());

		System.out.println(sensor.getSuperClassesList());
		System.out.println(Sensor.getSensorInstance().getSuperClassesList());

		System.out.println(sensor.getClassTypesList());
		System.out.println(Sensor.getSensorInstance().getClassTypesList());

	}

}
