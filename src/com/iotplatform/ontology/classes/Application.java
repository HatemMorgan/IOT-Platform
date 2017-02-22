package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

@Component
public class Application extends Class {

	public Application() {
		super("Application", "http://iot-platform#Application", Prefixes.IOT_PLATFORM);

		this.getProperties().put("description",
				new DataTypeProperty("description", Prefixes.IOT_PLATFORM, XSDDataTypes.string_typed));
		this.getProperties().put("name", new DataTypeProperty("name", Prefixes.FOAF, XSDDataTypes.string_typed));

		System.out.println("Applicatoin Bean Created");
		System.out.println("propertes size = " + this.getProperties().size());
		System.out.println(this.getProperties().toString());
	}

}
