package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;

@Component
public class Admin extends Person {

	public Admin() {
		super("Admin", "http://iot-platform#Admin", Prefixes.IOT_PLATFORM);

		this.getProperties().put("adminOf",
				new ObjectProperty("adminOf", Prefixes.IOT_PLATFORM, this.getApplicationClass()));
		
		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "adminOf", "adminOf");
		
		System.out.println("Admin Bean Created");
		System.out.println("propertes size = " + this.getProperties().size());
		System.out.println(this.getProperties().toString());
	}

}
