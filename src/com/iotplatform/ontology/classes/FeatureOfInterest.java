package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

/*
 *  This Class maps the ssn:FeatureOfInterest class in the ontology
 *  
 *  A feature is an abstraction of real world phenomena (thing, person, event, etc) that are the target of sensing .
 *  
 * A relation between an observation and the entity whose quality was observed. 
 * For example, in an observation of the weight of a person, the feature of interest is the person and the 
 * quality is weight. A soil is a feature of interest has property soil tempreture and qualty is tempreture .
 *
 *Features of interest can be events or objects but not qualities ex:  accuracy is not a property of a
 * temperature but the property of a sensor or an observation procedure
 */

@Component
public class FeatureOfInterest extends Class {

	public FeatureOfInterest() {
		super("FeatureOfInterest", "http://purl.oclc.org/NET/ssnx/ssn#FeatureOfInterest", Prefixes.SSN);
		init();
	}

	private void init() {

	}

}
