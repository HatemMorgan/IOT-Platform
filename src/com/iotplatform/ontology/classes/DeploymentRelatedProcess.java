package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

/*
 * This Class maps the ssn:DeploymentRelatedProcess class in the ontology
 * 
 * Place to group all the various Processes related to Deployment.  
 * For example, as well as Deplyment, installation, maintenance, deployment of further sensors and the like 
 * would all be classified under DeploymentRelatedProcess and it can link to deploymentRelatedProcess together 
 * using deploymentPorcessPart property .
 */

@Component
public class DeploymentRelatedProcess extends Class {

	public DeploymentRelatedProcess(String name, String uri, Prefixes prefix) {
		super("DeploymentRelatedProcess", "http://purl.oclc.org/NET/ssnx/ssn#DeploymentRelatedProcess", Prefixes.SSN);
		init();
	}

	public DeploymentRelatedProcess() {
		super("DeploymentRelatedProcess", "http://purl.oclc.org/NET/ssnx/ssn#DeploymentRelatedProcess", Prefixes.SSN);
		init();
	}

	private void init() {

	}
}
