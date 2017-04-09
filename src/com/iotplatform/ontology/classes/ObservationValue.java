package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.XSDDatatype;

/*
 * This Class maps ObservationValue class in the ontology
 * 
 * The value of the result of an Observation.  An Observation has a result which is the output of some sensor,
 *  the result is an information object that encodes some value for a Feature.
 */

@Component
public class ObservationValue extends Class {

	private static ObservationValue observationValueInstance;
	private Class observationValueSubjectClassInstance;

	public ObservationValue() {
		super("ObservationValue", "http://purl.oclc.org/NET/ssnx/ssn#ObservationValue", Prefix.SSN, null, false);
		init();

	}

	private Class getObservationValueSubjectClassInstance() {
		if (observationValueSubjectClassInstance == null)
			observationValueSubjectClassInstance = new Class("ObservationValue",
					"http://purl.oclc.org/NET/ssnx/ssn#ObservationValue", Prefix.SSN, null, false);

		return observationValueSubjectClassInstance;
	}

	public synchronized static ObservationValue getObservationValueInstance() {
		if (observationValueInstance == null) {
			observationValueInstance = new ObservationValue();
		}

		return observationValueInstance;
	}

	private void init() {
		super.getProperties().put("id", new DataTypeProperty(getObservationValueSubjectClassInstance(), "id",
				Prefix.IOT_LITE, XSDDatatype.string_typed, false, false));

		super.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "id", "id");
	}

}
