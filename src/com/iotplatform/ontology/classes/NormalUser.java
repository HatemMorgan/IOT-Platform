package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;

@Component
public class NormalUser extends Person {

	public NormalUser() {
		super("NormalUser", "http://iot-platform#NormalUser", Prefixes.IOT_PLATFORM);

		super.getProperties().put("usesApplication",
				new ObjectProperty("usesApplication", Prefixes.IOT_PLATFORM, this.getApplicationClass()));
		
		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "usesApplication", "usesApplication");
		
		System.out.println("NormalUser Bean Created");
		System.out.println("propertes size = " + this.getProperties().size());
		System.out.println(this.getProperties().toString());
	}

}
