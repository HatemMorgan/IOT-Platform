package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.XSDDatatype;

/*
 * This class maps the output class in the ontology
 * 
 * Any information that is reported from a process. [MMI OntDev]
 */

@Component
public class Output extends Class {

	private static Output outputInstance;
	private Class outputSubjectClassInstance;

	public Output() {

		super("Output", "http://purl.oclc.org/NET/ssnx/ssn#Output", Prefix.SSN, null, false);
		init();
	}

	private Class getOutputSubjectClassInstance() {
		if (outputSubjectClassInstance == null)
			outputSubjectClassInstance = new Class("Output", "http://purl.oclc.org/NET/ssnx/ssn#Output", Prefix.SSN,
					null, false);

		return outputSubjectClassInstance;
	}

	public synchronized static Output getOutputInstance() {
		if (outputInstance == null) {
			outputInstance = new Output();
		}

		return outputInstance;
	}

	private void init() {
		super.getProperties().put("id", new DataTypeProperty(getOutputSubjectClassInstance(), "id", Prefix.IOT_LITE,
				XSDDatatype.string_typed, false, false));

		super.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "id", "id");
	}

}
