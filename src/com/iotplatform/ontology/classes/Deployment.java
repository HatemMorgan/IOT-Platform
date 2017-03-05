package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Prefixes;

/*
 * This Class maps the Deployment Class in the ontology
 * 
 * The ongoing Process of Entities (for the purposes of this ontology, mainly sensors) deployed for a particular 
 * purpose.  For example, a particular Sensor deployed on a Platform, or a whole network of Sensors deployed for 
 * an observation campaign. The deployment may have sub processes, such as installation, maintenance, addition, 
 * and decomissioning and removal. It expresses the purpose of deployment not the service that the device offers .
 */

@Component
public class Deployment extends DeploymentRelatedProcess {

	public Deployment() {
		super("Deployment", "http://purl.oclc.org/NET/ssnx/ssn#Deployment", Prefixes.SSN);
		init();
	}

	private void init() {

	}

}
