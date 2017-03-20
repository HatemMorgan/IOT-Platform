package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;

/*
 *  This class maps the Admin class in the ontology
 */

@Component
public class Admin extends Person {

	private static Admin adminInstance;

	public Admin() {
		super("Admin", "http://iot-platform#Admin", Prefixes.IOT_PLATFORM);
		init();
	}

	/*
	 * This constructor is used to perform overloading constructor technique and
	 * the parameter String nothing will be passed always with null
	 * 
	 * I have done this overloaded constructor to instantiate the static
	 * adminInstance to avoid java.lang.StackOverflowError exception that Occur
	 * when calling init() to add properties to adminInstance
	 * 
	 */
	public Admin(String nothing) {
		super("Admin", "http://iot-platform#Admin", Prefixes.IOT_PLATFORM);
	}

	public synchronized static Admin getAdminInstance() {
		if (adminInstance == null) {
			adminInstance = new Admin(null);
			initAdminStaticInstance(adminInstance);
		}

		return adminInstance;
	}

	public static void initAdminStaticInstance(Class adminInstance) {
		adminInstance.getProperties().put("adminOf", new ObjectProperty("adminOf", Prefixes.IOT_PLATFORM,
				Application.getApplicationInstance(), false, false));

		adminInstance.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "adminOf", "adminOf");

		adminInstance.getSuperClassesList().add(Person.getPersonInstance());
	}

	private void init() {
		super.getProperties().put("adminOf", new ObjectProperty("adminOf", Prefixes.IOT_PLATFORM,
				Application.getApplicationInstance(), false, false));

		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "adminOf", "adminOf");

		super.getSuperClassesList().add(Person.getPersonInstance());
	}

	public static void main(String[] args) {
		Admin admin = new Admin();
		System.out.println(admin.getProperties().size());
		System.out.println(Admin.getAdminInstance().getProperties().size());
	}

}
