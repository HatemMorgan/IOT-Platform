package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

/*
 *  This Class maps the Input Class in the ontology 
 *  
 *  Any information that is provided to a process for its use [MMI OntDev]
 */

@Component
public class Input extends Class {

	private static Input inputInstance;

	public Input() {
		super("Input", "http://purl.oclc.org/NET/ssnx/ssn#Input", Prefixes.SSN, null);
		init();
	}

	public synchronized static Input getInputInstance() {
		if (inputInstance == null)
			inputInstance = new Input();

		return inputInstance;
	}

	private void init() {
		super.getProperties().put("id",
				new DataTypeProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, false));

		super.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");
	}

}
