package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.XSDDatatype;

/*
 * Organization class maps the organization class in the ontology. 
 */

@Component
public class Organization extends Agent {

	private static Organization organizationInstance;
	private Class organizationSubjectClassInstance;

	public Organization() {
		super("Organization", "http://xmlns.com/foaf/0.1/Organization", Prefix.FOAF, "name", false);

		init();
	}

	/*
	 * This constructor is used to perform overloading constructor technique and
	 * the parameter String nothing will be passed always with null
	 * 
	 * I have done this overloaded constructor to instantiate the static
	 * organizationInstance to avoid java.lang.StackOverflowError exception that
	 * Occur when calling init() to add properties to organizationInstance
	 * 
	 */
	public Organization(String nothing) {
		super("Organization", "http://xmlns.com/foaf/0.1/Organization", Prefix.FOAF, "name", null, false);
	}

	private Class getOrganizationSubjectClassInstance() {
		if (organizationSubjectClassInstance == null)
			organizationSubjectClassInstance = new Class("Organization", "http://xmlns.com/foaf/0.1/Organization",
					Prefix.FOAF, "name", false);

		return organizationSubjectClassInstance;
	}

	public synchronized static Organization getOrganizationInstance() {

		if (organizationInstance == null) {
			organizationInstance = new Organization(null);
			initOrganizationStaticInstance(organizationInstance);
			initAgentStaticInstanc(organizationInstance);
		}
		return organizationInstance;
	}

	public static void initOrganizationStaticInstance(Organization organizationInstance) {
		organizationInstance.getProperties().put("name",
				new DataTypeProperty(organizationInstance.getOrganizationSubjectClassInstance(), "name", Prefix.FOAF,
						XSDDatatype.string_typed, false, true));
		organizationInstance.getProperties().put("description",
				new DataTypeProperty(organizationInstance.getOrganizationSubjectClassInstance(), "description",
						Prefix.IOT_PLATFORM, XSDDatatype.string_typed, false, false));

		organizationInstance.getHtblPropUriName().put(Prefix.FOAF.getUri() + "name", "name");
		organizationInstance.getHtblPropUriName().put(Prefix.IOT_PLATFORM.getUri() + "description", "description");

		organizationInstance.getSuperClassesList().add(Agent.getAgentInstance());

	}

	private void init() {
		super.getProperties().put("name", new DataTypeProperty(getOrganizationSubjectClassInstance(), "name",
				Prefix.FOAF, XSDDatatype.string_typed, false, true));
		super.getProperties().put("description", new DataTypeProperty(getOrganizationSubjectClassInstance(),
				"description", Prefix.IOT_PLATFORM, XSDDatatype.string_typed, false, false));

		super.getHtblPropUriName().put(Prefix.FOAF.getUri() + "name", "name");
		super.getHtblPropUriName().put(Prefix.IOT_PLATFORM.getUri() + "description", "description");

		super.getSuperClassesList().add(Agent.getAgentInstance());
	}

	public static void main(String[] args) {
		Organization organization = new Organization();
		System.out.println(organization.getProperties().size());
		System.out.println(Organization.getOrganizationInstance().getProperties().size());
	}
}
