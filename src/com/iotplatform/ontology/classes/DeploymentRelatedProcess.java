package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.ObjectProperty;
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

	private static DeploymentRelatedProcess deploymentRelatedProcessInstance;

	public DeploymentRelatedProcess(String name, String uri, Prefixes prefix) {
		super(name, uri, prefix);
		init();
	}

	public DeploymentRelatedProcess() {
		super("DeploymentRelatedProcess", "http://purl.oclc.org/NET/ssnx/ssn#DeploymentRelatedProcess", Prefixes.SSN);
		init();
	}

	public DeploymentRelatedProcess(String nothing) {
		super("DeploymentRelatedProcess", "http://purl.oclc.org/NET/ssnx/ssn#DeploymentRelatedProcess", Prefixes.SSN);
	}

	public synchronized static DeploymentRelatedProcess getDeploymentRelatedProcessInstance() {
		if (deploymentRelatedProcessInstance == null)
			deploymentRelatedProcessInstance = new DeploymentRelatedProcess(null);

		return deploymentRelatedProcessInstance;
	}

	private void init() {

		/*
		 * relation between a deployment process and its constituent processes.
		 * 
		 * It says that a ssn:deploymentProcess is part of another
		 * ssn:deploymentProcess. A DeploymentRelatedProcess or its subclasses
		 * can have many deploymentProcessParts so multipleValues is enabled
		 * (one to many relation)
		 * 
		 */

		this.getProperties().put("deploymentProcessPart", new ObjectProperty("deploymentProcessPart", Prefixes.SSN,
				DeploymentRelatedProcess.getDeploymentRelatedProcessInstance(), true, false));

		super.getHtblPropUriName().put(Prefixes.SSN.getUri() + "deploymentProcessPart", "deploymentProcessPart");
	}
}
