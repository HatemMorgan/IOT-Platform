package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.OntologyClass;
import com.iotplatform.ontology.Prefixes;

@Component
public class NormalUser extends Person {

	public NormalUser() {
		super("NormalUser", "http://iot-platform#NormalUser", Prefixes.IOT_PLATFORM);

		this.getProperties().put("usesApplication",
				new ObjectProperty("usesApplication", Prefixes.IOT_PLATFORM, OntologyClass.Application));
		System.out.println("NormalUser Bean Created");
		System.out.println("propertes size = " + this.getProperties().size());
		System.out.println(this.getProperties().toString());
	}

}
