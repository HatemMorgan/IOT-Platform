package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

/*
 *  This Class maps MetaData Class in the ontology
 *  
 *  Class used to describe properties that cannot be described by QuantityKind and Units.
 *   i.e. the resolution of a sensor.
 */

@Component
public class Metadata extends Class {

	
	private static Metadata metadataInstance;

	public Metadata() {
		super("Metadata", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Metadata", Prefixes.IOT_LITE, null,false);
		init();
	}

	public synchronized static Metadata getMetadataInstance() {
		if (metadataInstance == null)
			metadataInstance = new Metadata();

		return metadataInstance;
	}

	private void init() {

		/*
		 * Defines the type pf the metadata value (e.g. resolution of the
		 * sensor). It must be unique to uniquely identify a metadata
		 */
		super.getProperties().put("metadataType",
				new DataTypeProperty("metadataType", Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, true));

		/*
		 * Value of the metadata
		 */
		super.getProperties().put("metadataValue",
				new DataTypeProperty("metadataValue", Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, false));

		super.getProperties().put("id",
				new DataTypeProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, false));

		super.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");
		super.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "metadataType", "metadataType");
		super.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "metadataValue", "metadataValue");

	}

}
