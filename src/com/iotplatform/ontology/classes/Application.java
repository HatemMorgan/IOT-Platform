package com.iotplatform.ontology.classes;

import java.util.Hashtable;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.XSDDataTypes;

@Component
public class Application extends Class {

	private Hashtable<String, Property> properties;

	public Application() {
		super("Application", "http://iot-platform#Application", Prefixes.IOT_PLATFORM);

		properties = new Hashtable<>();
		properties.put("description",
				new DataProperty("description", Prefixes.IOT_PLATFORM, XSDDataTypes.string_typed));
	}

}
