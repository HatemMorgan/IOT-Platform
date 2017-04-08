package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.XSDDataTypes;

/*
 * This Class maps ssn:SensorDataSheet class in the ontology
 * 
 * A data sheet records properties of a sensor.  A data sheet might describe for example the accuracy in various
 *  conditions, the power use, the types of connectors that the sensor has, etc.  
 * 
 * Generally a sensor's properties are recorded directly (with hasMeasurementCapability, for example),
 * but the data sheet can be used for example to record the manufacturers specifications verses observed capabilites,
 * or if more is known than the manufacturer specifies, etc.  The data sheet is an information object
 * about the sensor's properties, rather than a direct link to the actual properties themselves.
 * 
 */

@Component
public class SensorDataSheet extends Class {

	private static SensorDataSheet sensorDataSheetInstance;
	private Class sensorDataSheetSubjectClassInstance;

	public SensorDataSheet() {
		super("SensorDataSheet", "http://purl.oclc.org/NET/ssnx/ssn#SensorDataSheet", Prefix.SSN, null, false);
		init();
	}

	private Class getSensorDataSheetSubjectClassInstance() {
		if (sensorDataSheetSubjectClassInstance == null)
			sensorDataSheetSubjectClassInstance = new Class("SensorDataSheet",
					"http://purl.oclc.org/NET/ssnx/ssn#SensorDataSheet", Prefix.SSN, null, false);

		return sensorDataSheetSubjectClassInstance;
	}

	public synchronized static SensorDataSheet getSensorDataSheetInstance() {
		if (sensorDataSheetInstance == null) {
			sensorDataSheetInstance = new SensorDataSheet();
		}
		return sensorDataSheetInstance;
	}

	private void init() {
		super.getProperties().put("id", new DataTypeProperty(getSensorDataSheetSubjectClassInstance(), "id",
				Prefix.IOT_LITE, XSDDataTypes.string_typed, false, false));

		super.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "id", "id");
	}

}
