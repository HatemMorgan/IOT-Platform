package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.XSDDatatype;

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
	private Class platformSubjectClassInstance;

	public Platform() {
		super("Platform", "http://purl.oclc.org/NET/ssnx/ssn#Platform", Prefix.SSN, null, false);
		init();
	}

	private Class getPlatformSubjectClassInstance() {
		if (platformSubjectClassInstance == null)
			platformSubjectClassInstance = new Class("Platform", "http://purl.oclc.org/NET/ssnx/ssn#Platform",
					Prefix.SSN, null, false);

		return platformSubjectClassInstance;
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
		super.getProperties().put("id", new DataTypeProperty(getPlatformSubjectClassInstance(), "id", Prefix.IOT_LITE,
				XSDDatatype.string_typed, false, false));

		/*
		 * Relation between Platform and its physical location described by
		 * point class
		 */
		super.getProperties().put("location", new ObjectProperty(getPlatformSubjectClassInstance(), "location",
				Prefix.GEO, Point.getPointInstacne(), false, false));

		/*
		 * Describes if the platform is moving . ie: fish (A fish is a type of
		 * platform because a sensor can be attached to it)
		 */
		super.getProperties().put("isMobile", new DataTypeProperty(getPlatformSubjectClassInstance(), "isMobile",
				Prefix.IOT_LITE, XSDDatatype.boolean_type, false, false));

		super.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "id", "id");
		super.getHtblPropUriName().put(Prefix.GEO.getUri() + "hasLocation", "hasLocation");
		super.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "isMobile", "isMobile");

	}
}
