package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

/*
 * This Class maps ssn:Stimulus Class in the ontology
 * 
 * An Event in the real world that 'triggers' the sensor.  The properties associated to the stimulus may
 *  be different to eventual observed property.  It is the event, not the object that triggers the sensor.
 * 
 * Based on the work of Quine, the skeleton defines stimuli as the (only) link to the physical environment.
 *  Empirical science observes these stimuli using sensors to infer information about environmental properties
 *   and construct features of interest.
 * 
 * Stimuli are detectable changes in the environment, i.e., in the physical world. They are the starting point
 *  of each measurement as they act as triggers for sensors.
 * 
 * They can also be actively produced by a sensor to perform observations about real physical world . 
 * The same types of stimulus can trigger different kinds of sensors and be used to reason about different
 *  properties. 
 * 
 * ex: In biology, a stimulus is a change in an organism's surroundings that causes the organism to change
 * its behavior in order to make the environment more satisfactory. For instance, 1- hunger motivates animals
 * to seek food, 2- predators stimulate prey to run away or hide, and 3- falling temperatures encourage creatures
 * to seek shelter or find warmth in other ways
 */

@Component
public class Stimulus extends Class {

	private static Stimulus stimulusInstance;

	public Stimulus() {
		super("Stimulus", "http://purl.oclc.org/NET/ssnx/ssn#Stimulus", Prefixes.SSN, null);
		init();
	}

	public synchronized static Stimulus getStimulusInstance() {
		if (stimulusInstance == null)
			stimulusInstance = new Stimulus();

		return stimulusInstance;
	}

	private void init() {
		super.getProperties().put("id",
				new DataTypeProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, false));

		super.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");
	}

}
