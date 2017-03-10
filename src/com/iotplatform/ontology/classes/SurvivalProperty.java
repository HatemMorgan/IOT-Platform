package com.iotplatform.ontology.classes;

import java.util.Hashtable;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

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

	private Hashtable<String, Class> survivalPropertyTypesList;

	public SurvivalProperty() {
		super("SurvivalProperty", "http://purl.oclc.org/NET/ssnx/ssn#SurvivalProperty", Prefixes.SSN);
		init();
	}

	private void init() {
		survivalPropertyTypesList = new Hashtable<>();

		/*
		 * Total useful life of a battery.
		 */
		Class batteryLifetime = new Class("BatteryLifetime", "http://purl.oclc.org/NET/ssnx/ssn#BatteryLifetime",
				Prefixes.SSN);

		/*
		 * adding ssn:SurvivalProperty class to superClassesList to tell the dao
		 * to add triple that expresses that an instance of class
		 * ssn:BatteryLifetime is also an instance of class ssn:SurvivalProperty
		 */
		batteryLifetime.getSuperClassesList().add(SurvivalProperty.getPropertyInstance());
		survivalPropertyTypesList.put("BatteryLifetime", batteryLifetime);

		/*
		 * Total useful life of a sensor/system (expressed as total life since
		 * manufacture, time in use, number of operations, etc.).
		 */

		Class systemLifetime = new Class("SystemLifetime", "http://purl.oclc.org/NET/ssnx/ssn#SystemLifetime",
				Prefixes.SSN);

		/*
		 * adding ssn:SurvivalProperty class to superClassesList to tell the dao
		 * to add triple that expresses that an instance of class
		 * ssn:SystemLifetime is also an instance of class ssn:SurvivalProperty
		 */
		systemLifetime.getSuperClassesList().add(SurvivalProperty.getPropertyInstance());
		survivalPropertyTypesList.put("SystemLifetime", systemLifetime);

	}

}
