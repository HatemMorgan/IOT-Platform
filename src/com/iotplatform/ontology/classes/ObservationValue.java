package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

/*
 * This Class maps ObservationValue class in the ontology
 * 
 * The value of the result of an Observation.  An Observation has a result which is the output of some sensor,
 *  the result is an information object that encodes some value for a Feature.
 */

@Component
public class ObservationValue extends Class {

	public ObservationValue() {
		super("ObservationValue", "http://purl.oclc.org/NET/ssnx/ssn#ObservationValue", Prefixes.SSN, null);
		init();

	}

	private void init() {
		super.getProperties().put("id",
				new DataTypeProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, false));

		super.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");
	}

}
