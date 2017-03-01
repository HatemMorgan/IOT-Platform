package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

@Component
public class Organization extends Class {

	private static Organization organizationInstance;

	public Organization() {
		super("Organization", "http://xmlns.com/foaf/0.1/Organization", Prefixes.FOAF);

		super.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "name", "name");
		super.getProperties().put("mbox", new DataTypeProperty("mbox", Prefixes.FOAF, XSDDataTypes.string_typed));
		super.getProperties().put("description",
				new DataTypeProperty("description", Prefixes.IOT_PLATFORM, XSDDataTypes.string_typed));

	}

	/*
	 * this constructor is used only to construct an instance of class
	 * organization that will be used as the class type of an object so it does
	 * not need to has the associated properties of class organization . the
	 * nothing parameter that it takes will be passed as null because it is only
	 * used to allow overloading constructor technique
	 */
	public Organization(String nothing) {
		super("Organization", "http://xmlns.com/foaf/0.1/Organization", Prefixes.FOAF);
	}

	public synchronized static Organization getOrganizationInstance() {

		if (organizationInstance == null) {
			organizationInstance = new Organization(null);

		}
		return organizationInstance;
	}

}
