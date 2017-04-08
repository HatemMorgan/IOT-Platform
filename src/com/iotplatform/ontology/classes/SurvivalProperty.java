package com.iotplatform.ontology.classes;

import java.util.Hashtable;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefix;

/*
 *  This Class maps SurvivalProperty class in the ontology
 *  
 *  An identifiable characteristic that represents the extent of the sensors useful life.  
 *  Might include for example total battery life or number of recharges, or, for sensors that are used only a 
 *  fixed number of times, the number of observations that can be made before the sensing capability is depleted.
 *  
 */

@Component
public class SurvivalProperty extends Property {

	private static SurvivalProperty survivalPropertyInstance;

	public SurvivalProperty() {
		super("SurvivalProperty", "http://purl.oclc.org/NET/ssnx/ssn#SurvivalProperty", Prefix.SSN, null, true);
		init();
	}

	/*
	 * This constructor is used to perform overloading constructor technique and
	 * the parameter String nothing will be passed always with null
	 * 
	 * I have done this overloaded constructor to instantiate the static
	 * systemInstance to avoid java.lang.StackOverflowError exception that Occur
	 * when calling init() to add properties to systemInstance
	 * 
	 */
	public SurvivalProperty(String nothing) {
		super("SurvivalProperty", "http://purl.oclc.org/NET/ssnx/ssn#SurvivalProperty", Prefix.SSN, null, true, null);
	}

	public synchronized static SurvivalProperty getSurvivalPropertyInstance() {
		if (survivalPropertyInstance == null) {
			survivalPropertyInstance = new SurvivalProperty(null);
			initPropertyStaticInstance(survivalPropertyInstance);
			initSurvivalPropertyStaticInstance(survivalPropertyInstance);
		}

		return survivalPropertyInstance;
	}

	private void init() {

		this.getSuperClassesList().add(Property.getPropertyInstance());

		/*
		 * emptying classTypelist
		 */
		this.setClassTypesList(new Hashtable<>());

		/*
		 * Total useful life of a battery.
		 */
		Class batteryLifetime = new Class("BatteryLifetime", "http://purl.oclc.org/NET/ssnx/ssn#BatteryLifetime",
				Prefix.SSN, null, false);

		/*
		 * adding ssn:SurvivalProperty class to superClassesList to tell the dao
		 * to add triple that expresses that an instance of class
		 * ssn:BatteryLifetime is also an instance of class ssn:SurvivalProperty
		 */
		batteryLifetime.getSuperClassesList().addAll(SurvivalProperty.getPropertyInstance().getSuperClassesList());
		batteryLifetime.getSuperClassesList().add(SurvivalProperty.getPropertyInstance());
		batteryLifetime.setProperties(super.getProperties());
		batteryLifetime.setHtblPropUriName(super.getHtblPropUriName());
		this.getClassTypesList().put("BatteryLifetime", batteryLifetime);

		/*
		 * Total useful life of a sensor/system (expressed as total life since
		 * manufacture, time in use, number of operations, etc.).
		 */

		Class systemLifetime = new Class("SystemLifetime", "http://purl.oclc.org/NET/ssnx/ssn#SystemLifetime",
				Prefix.SSN, null, false);

		/*
		 * adding ssn:SurvivalProperty class to superClassesList to tell the dao
		 * to add triple that expresses that an instance of class
		 * ssn:SystemLifetime is also an instance of class ssn:SurvivalProperty
		 */
		systemLifetime.getSuperClassesList().addAll(SurvivalProperty.getPropertyInstance().getSuperClassesList());
		systemLifetime.getSuperClassesList().add(SurvivalProperty.getPropertyInstance());
		systemLifetime.setProperties(super.getProperties());
		systemLifetime.setHtblPropUriName(super.getHtblPropUriName());
		this.getClassTypesList().put("SystemLifetime", systemLifetime);

	}

	private static void initSurvivalPropertyStaticInstance(SurvivalProperty survivalPropertyInstance) {

		 survivalPropertyInstance.getSuperClassesList().add(Property.getPropertyInstance());

		/*
		 * emptying classTypelist
		 */
		survivalPropertyInstance.setClassTypesList(new Hashtable<>());

		/*
		 * Total useful life of a battery.
		 */
		Class batteryLifetime = new Class("BatteryLifetime", "http://purl.oclc.org/NET/ssnx/ssn#BatteryLifetime",
				Prefix.SSN, null, false);

		/*
		 * adding ssn:SurvivalProperty class to superClassesList to tell the dao
		 * to add triple that expresses that an instance of class
		 * ssn:BatteryLifetime is also an instance of class ssn:SurvivalProperty
		 */
		batteryLifetime.getSuperClassesList().addAll(SurvivalProperty.getSurvivalPropertyInstance().getSuperClassesList());
		batteryLifetime.getSuperClassesList().add(SurvivalProperty.getSurvivalPropertyInstance());
		batteryLifetime.setProperties(survivalPropertyInstance.getProperties());
		batteryLifetime.setHtblPropUriName(survivalPropertyInstance.getHtblPropUriName());
		survivalPropertyInstance.getClassTypesList().put("BatteryLifetime", batteryLifetime);

		/*
		 * Total useful life of a sensor/system (expressed as total life since
		 * manufacture, time in use, number of operations, etc.).
		 */

		Class systemLifetime = new Class("SystemLifetime", "http://purl.oclc.org/NET/ssnx/ssn#SystemLifetime",
				Prefix.SSN, null, false);

		/*
		 * adding ssn:SurvivalProperty class to superClassesList to tell the dao
		 * to add triple that expresses that an instance of class
		 * ssn:SystemLifetime is also an instance of class ssn:SurvivalProperty
		 */
		systemLifetime.getSuperClassesList().addAll(SurvivalProperty.getSurvivalPropertyInstance().getSuperClassesList());
		systemLifetime.getSuperClassesList().add(SurvivalProperty.getSurvivalPropertyInstance());
		systemLifetime.setProperties(survivalPropertyInstance.getProperties());
		systemLifetime.setHtblPropUriName(survivalPropertyInstance.getHtblPropUriName());
		survivalPropertyInstance.getClassTypesList().put("SystemLifetime", systemLifetime);

	}

	public static void main(String[] args) {
		System.out.println(SurvivalProperty.getSurvivalPropertyInstance().getSuperClassesList());
		System.out.println(SurvivalProperty.getSurvivalPropertyInstance().getClassTypesList().get("SystemLifetime")
				.getSuperClassesList());
	}

}
