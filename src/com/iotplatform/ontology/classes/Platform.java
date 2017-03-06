package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;

/*
 *  THis class maps the ssn:Platform class in the ontology
 *  
 *  An Entity to which other Entities can be attached - particuarly Sensors and other Platforms. 
 *   For example, a post might act as the Platform, a bouy might act as a Platform,
 *   or a fish might act as a Platform for an attached sensor.
 *  
 */

@Component
public class Platform extends Class {

	private static Platform platformInstance;

	public Platform() {
		super("Platform", "http://purl.oclc.org/NET/ssnx/ssn#Platform", Prefixes.SSN);
		init();
	}

	/*
	 * String nothing parameter is added for overloading constructor technique
	 * because I need to initialize an instance without having properties and it
	 * will be always passed by null
	 */
	public Platform(String nothing) {
		super("Platform", "http://purl.oclc.org/NET/ssnx/ssn#Platform", Prefixes.SSN);

	}

	public synchronized static Platform getPlatformInstance() {
		if (platformInstance == null)
			platformInstance = new Platform(null);

		return platformInstance;
	}

	private void init() {
		/*
		 * Relation between Platform and its physical location described by
		 * point class
		 */
		super.getProperties().put("hasLocation",
				new ObjectProperty("hasLocation", Prefixes.GEO, Point.getPointInstacne(), false, false));
	}
}
