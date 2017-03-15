package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

/*
 *  This Class maps the Object class in the ontology
 *  
 *  An Object or IoT entity that represent the place.  (i.e. room, car, table)
 */

@Component
public class ObjectClass extends Class {

	public ObjectClass() {
		super("Object", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Object", Prefixes.IOT_LITE, null);
		init();
	}

	private void init() {
		super.getProperties().put("id",
				new DataTypeProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, false));

		super.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");
	}

}
