package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

/*
 *  THis class maps the ssn:Platform class in the ontology
 *  
 *  An Entity to which other Entities can be attached - particuarly Sensors and other Platforms. 
 *   For example, a post might act as the Platform, a bouy might act as a Platform,
 *   or a fish might act as a Platform for an attached sensor.
 *   
 *   So It is a place where a device is attached to it
 *   
 *   A Platform can be used for multiple sensors
 *  
 */

@Component
public class Platform extends Class {

	private static Platform platformInstance;

	
	public Platform() {
		super("Platform", "http://purl.oclc.org/NET/ssnx/ssn#Platform", Prefixes.SSN, null,false);
		init();
	}

	public synchronized static Platform getPlatformInstance() {
		if (platformInstance == null)
			platformInstance = new Platform();

		return platformInstance;
	}

	private void init() {

		/*
		 * DeviceModule id which must be unique
		 */
		super.getProperties().put("id",
				new DataTypeProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, false));

		/*
		 * Relation between Platform and its physical location described by
		 * point class
		 */
		super.getProperties().put("location",
				new ObjectProperty("location", Prefixes.GEO, Point.getPointInstacne(), false, false));

		/*
		 * Describes if the platform is moving . ie: fish (A fish is a type of
		 * platform because a sensor can be attached to it)
		 */
		super.getProperties().put("isMobile",
				new DataTypeProperty("isMobile", Prefixes.IOT_LITE, XSDDataTypes.boolean_type, false, false));

		super.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");
		super.getHtblPropUriName().put(Prefixes.GEO.getUri() + "hasLocation", "hasLocation");
		super.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "isMobile", "isMobile");

	}
}
