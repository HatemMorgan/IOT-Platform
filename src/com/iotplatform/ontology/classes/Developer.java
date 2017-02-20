package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;

@Component
public class Developer extends Person {

	public Developer() {
		super("Developer", "http://iot-platform#Developer", Prefixes.IOT_PLATFORM);
		System.out.println(super.getProperties());
		super.getProperties().put("developedApplication",
				new ObjectProperty("developedApplication", Prefixes.IOT_PLATFORM, this.getApplicationClass()));
		System.out.println("Developer Bean Created");
		System.out.println("propertes size = " + super.getProperties().size());
		System.out.println(super.getProperties().toString());
	}

	// public static void main(String[] args) {
	// Developer developer = new Developer();
	// System.out.println(developer.getProperties().toString());
	// Admin admin = new Admin();
	// System.out.println(admin.getProperties().toString());
	// }
}
