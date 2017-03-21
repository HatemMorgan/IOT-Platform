package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

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

	private static Deployment deploymentInstance;

	public Deployment() {
		super("Deployment", "http://purl.oclc.org/NET/ssnx/ssn#Deployment", Prefixes.SSN,null,false);
		init();
	}

	public synchronized static Deployment getDeploymentInstance() {
		if (deploymentInstance == null) {
			deploymentInstance = new Deployment();
		}

		return deploymentInstance;

	}

	private void init() {

		/*
		 * Relation between a deployment and the platform on which the system
		 * was deployed. one to one relation because a deployment will be in one
		 * place (which is the place of the platform)
		 */
		this.getProperties().put("deployedOnPlatform",
				new ObjectProperty("deployedOnPlatform", Prefixes.SSN, Platform.getPlatformInstance(), false, false));

		super.getHtblPropUriName().put(Prefixes.SSN.getUri() + "deployedOnPlatform", "deployedOnPlatform");

	}

}
