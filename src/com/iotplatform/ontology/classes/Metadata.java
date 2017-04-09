package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.XSDDatatype;

/*
 *  This Class maps MetaData Class in the ontology
 *  
 *  Class used to describe properties that cannot be described by QuantityKind and Units.
 *   i.e. the resolution of a sensor.
 */

@Component
public class Metadata extends Class {

	private static Metadata metadataInstance;
	private Class metadataSubjectClassInstance;

	public Metadata() {
		super("Metadata", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Metadata", Prefix.IOT_LITE, null, false);
		init();
	}

	public synchronized static Metadata getMetadataInstance() {
		if (metadataInstance == null)
			metadataInstance = new Metadata();

		return metadataInstance;
	}

	private Class getMetadataSubjectClassInstance() {
		if (metadataSubjectClassInstance == null)
			metadataSubjectClassInstance = new Class("Metadata",
					"http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Metadata", Prefix.IOT_LITE, null, false);

		return metadataSubjectClassInstance;
	}

	private void init() {

		/*
		 * Defines the type pf the metadata value (e.g. resolution of the
		 * sensor). It must be unique to uniquely identify a metadata
		 */
		super.getProperties().put("metadataType", new DataTypeProperty(getMetadataSubjectClassInstance(),
				"metadataType", Prefix.IOT_LITE, XSDDatatype.string_typed, false, true));

		/*
		 * Value of the metadata
		 */
		super.getProperties().put("metadataValue", new DataTypeProperty(getMetadataSubjectClassInstance(),
				"metadataValue", Prefix.IOT_LITE, XSDDatatype.string_typed, false, false));

		super.getProperties().put("id", new DataTypeProperty(getMetadataSubjectClassInstance(), "id", Prefix.IOT_LITE,
				XSDDatatype.string_typed, false, false));

		super.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "id", "id");
		super.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "metadataType", "metadataType");
		super.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "metadataValue", "metadataValue");

	}

}
