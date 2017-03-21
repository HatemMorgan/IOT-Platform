package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.ObjectProperty;
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

	private static Deployment deploymentInstance;

	public Deployment() {
		super("Deployment", "http://purl.oclc.org/NET/ssnx/ssn#Deployment", Prefixes.SSN, null, false);
		init();
	}

	public Deployment(String nothing) {
		super("Deployment", "http://purl.oclc.org/NET/ssnx/ssn#Deployment", Prefixes.SSN, null, false, null);

	}

	public synchronized static Deployment getDeploymentInstance() {
		if (deploymentInstance == null) {
			deploymentInstance = new Deployment(null);
			initDeploymentStaticInstance(deploymentInstance);
			DeploymentRelatedProcess.initDeploymentRelatedProccessStaticInstance(deploymentInstance);
		}
		return deploymentInstance;

	}

	private void init() {

		/*
		 * Relation between a deployment and the platform on which the system
		 * was deployed. one to one relation because a deployment will be in one
		 * place (which is the place of the platform)
		 */
		super.getProperties().put("deployedOnPlatform",
				new ObjectProperty("deployedOnPlatform", Prefixes.SSN, Platform.getPlatformInstance(), false, false));

		super.getHtblPropUriName().put(Prefixes.SSN.getUri() + "deployedOnPlatform", "deployedOnPlatform");

		super.getSuperClassesList().add(DeploymentRelatedProcess.getDeploymentRelatedProcessInstance());

	}

	private static void initDeploymentStaticInstance(Deployment deploymentInstance) {

		/*
		 * Relation between a deployment and the platform on which the system
		 * was deployed. one to one relation because a deployment will be in one
		 * place (which is the place of the platform)
		 */
		deploymentInstance.getProperties().put("deployedOnPlatform",
				new ObjectProperty("deployedOnPlatform", Prefixes.SSN, Platform.getPlatformInstance(), false, false));

		deploymentInstance.getHtblPropUriName().put(Prefixes.SSN.getUri() + "deployedOnPlatform", "deployedOnPlatform");

		deploymentInstance.getSuperClassesList().add(DeploymentRelatedProcess.getDeploymentRelatedProcessInstance());

	}

	public static void main(String[] args) {
		Deployment deployment = new Deployment();
		System.out.println(deployment.getProperties());
		System.out.println(deployment.getClassTypesList());
		System.out.println(Deployment.getDeploymentInstance().getProperties());
	}

}
