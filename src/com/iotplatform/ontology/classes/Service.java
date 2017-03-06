package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

/*
 * This Class maps iot-lite:Service Class in the ontology
 * 
 * Service provided by an IoT Device
 * 
 */

@Component
public class Service extends Class {

	private static Service serviceInstance;

	public Service() {
		super("Service", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Service", Prefixes.IOT_LITE);
		init();
	}

	/*
	 * String nothing parameter is added for overloading constructor technique
	 * because I need to initialize an instance without having properties and it
	 * will be always passed by null
	 */
	public Service(String nothing) {
		super("Service", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Service", Prefixes.IOT_LITE);
	}

	public synchronized static Service getServiceInstance() {
		if (serviceInstance == null)
			serviceInstance = new Service(null);

		return serviceInstance;
	}

	private void init() {

	}
}
