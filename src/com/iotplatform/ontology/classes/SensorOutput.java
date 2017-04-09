package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.XSDDatatype;

/*
 *  This Class maps ssn:SensorOutput class in the ontology
 *  
 *  A sensor outputs a piece of information (an observed value), the value itself being represented
 *  by an ObservationValue.
 */

@Component
public class SensorOutput extends Class {

	private static SensorOutput sensorOutputInstance;
	private Class sensorOutputSubjectClassInstance;

	public SensorOutput() {

		super("SensorOutput", " http://purl.oclc.org/NET/ssnx/ssn#SensorOutput", Prefix.SSN, null, false);
		init();
	}

	private Class getSensorOutputSubjectClassInstance() {
		if (sensorOutputSubjectClassInstance == null)
			sensorOutputSubjectClassInstance = new Class("SensorOutput",
					" http://purl.oclc.org/NET/ssnx/ssn#SensorOutput", Prefix.SSN, null, false);

		return sensorOutputSubjectClassInstance;
	}

	public synchronized static SensorOutput getSensorOutputInstance() {
		if (sensorOutputInstance == null)
			sensorOutputInstance = new SensorOutput();

		return sensorOutputInstance;
	}

	private void init() {
		this.getProperties().put("id", new DataTypeProperty(getSensorOutputSubjectClassInstance(), "id",
				Prefix.IOT_LITE, XSDDatatype.string_typed, false, false));

		this.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "id", "id");

		this.getProperties().put("hasValue", new ObjectProperty(getSensorOutputSubjectClassInstance(), "hasValue",
				Prefix.SSN, ObservationValue.getObservationValueInstance(), false, false));

		this.getHtblPropUriName().put(Prefix.SSN.getUri() + "hasValue", "hasValue");
	}

}
