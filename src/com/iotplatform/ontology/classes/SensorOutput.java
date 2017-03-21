package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

/*
 *  This Class maps ssn:SensorOutput class in the ontology
 *  
 *  A sensor outputs a piece of information (an observed value), the value itself being represented
 *  by an ObservationValue.
 */

@Component
public class SensorOutput extends Class {

	private static SensorOutput sensorOutputInstance;

	public SensorOutput() {

		super("SensorOutput", " http://purl.oclc.org/NET/ssnx/ssn#SensorOutput", Prefixes.SSN, null);
		init();
	}

	public synchronized static SensorOutput getSensorOutputInstance() {
		if (sensorOutputInstance == null)
			sensorOutputInstance = new SensorOutput();

		return sensorOutputInstance;
	}

	private void init() {
		super.getProperties().put("id",
				new DataTypeProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, false));

		super.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");
	}

}
