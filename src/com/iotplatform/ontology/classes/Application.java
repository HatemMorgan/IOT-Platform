package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

/*
 * This class maps the application class in the ontology
 */

@Component
public class Application extends Class {

	private static Application applicationInstance;

	public Application() {
		super("Application", "http://iot-platform#Application", Prefixes.IOT_PLATFORM);

		super.getProperties().put("description",
				new DataTypeProperty("description", Prefixes.IOT_PLATFORM, XSDDataTypes.string_typed,false,false));
		super.getProperties().put("name", new DataTypeProperty("name", Prefixes.FOAF, XSDDataTypes.string_typed,false,false));

		super.getProperties().put("fundedBy",
				new ObjectProperty("fundedBy", Prefixes.FOAF, Organization.getOrganizationInstance(),true,false));

		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "description", "description");
		super.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "name", "name");

		
	}

	/*
	 * this constructor is used only to construct an instance of class
	 * application that will be used as the class type of an object so it does
	 * not need to has the associated properties of class application . the
	 * nothing parameter that it takes will be passed as null because it is only
	 * used to allow overloading constructor technique
	 */
	public Application(String nothing) {
		super("Application", "http://iot-platform#Application", Prefixes.IOT_PLATFORM);
	}

	public synchronized static Application getApplicationInstance() {

		if (applicationInstance == null) {
			applicationInstance = new Application(null);

		}
		return applicationInstance;
	}

}
