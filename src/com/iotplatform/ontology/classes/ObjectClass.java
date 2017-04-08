package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.XSDDataTypes;

/*
 *  This Class maps the Object class in the ontology
 *  
 *  An Object or IoT entity that represent the place.  (i.e. room, car, table)
 */

@Component
public class ObjectClass extends Class {

	private static ObjectClass objectClassInstance;
	private Class objectSubjectClassInstance;

	public ObjectClass() {
		super("Object", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Object", Prefix.IOT_LITE, null, false);
		init();
	}

	public synchronized static ObjectClass getObjectClassInstance() {
		if (objectClassInstance == null) {
			objectClassInstance = new ObjectClass();
		}

		return objectClassInstance;
	}

	private Class getObjectSubjectClassInstance() {
		if (objectSubjectClassInstance == null)
			objectSubjectClassInstance = new Class("Object", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Object",
					Prefix.IOT_LITE, null, false);

		return objectSubjectClassInstance;
	}

	private void init() {
		this.getProperties().put("id", new DataTypeProperty(getObjectSubjectClassInstance(), "id", Prefix.IOT_LITE,
				XSDDataTypes.string_typed, false, false));

		this.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "id", "id");

		/*
		 * Links an Object with their attributes.
		 */
		this.getProperties().put("hasAttribute", new ObjectProperty(getObjectSubjectClassInstance(), "hasAttribute",
				Prefix.IOT_LITE, Attribute.getAttributeInstance(), false, false));

		this.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "hasAttribute", "hasAttribute");
	}

}
