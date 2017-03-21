package com.iotplatform.ontology.classes;

import java.util.Hashtable;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.XSDDataTypes;

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

	public DeploymentRelatedProcess(String name, String uri, Prefixes prefix, Property uniqueIdentifierProperty,
			boolean hasTypeClasses) {
		super(name, uri, prefix, uniqueIdentifierProperty, hasTypeClasses);
		init();
	}

	/*
	 * This constructor is used to perform overloading constructor technique and
	 * the parameter String nothing will be passed always with null
	 * 
	 * I use this constructor to create any subClassStaticInstance of
	 * DeploymentRelatedProcess. This constructor does not call init method so
	 * by this way I will be able to create a static instance from any of
	 * subClasses of DeploymentRelatedProcess and avoid throwing
	 * java.lang.StackOverflowError exception
	 * 
	 * I will use subClassesStaticInstances to add them to typeClassesList of
	 * DeploymentRelatedProcess
	 */
	public DeploymentRelatedProcess(String name, String uri, Prefixes prefix, Property uniqueIdentifierProperty,
			boolean hasTypeClasses, String nothing) {
		super(name, uri, prefix, uniqueIdentifierProperty, hasTypeClasses);
	}

	public DeploymentRelatedProcess() {
		super("DeploymentRelatedProcess", "http://purl.oclc.org/NET/ssnx/ssn#DeploymentRelatedProcess", Prefixes.SSN,
				null, true);
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
	public DeploymentRelatedProcess(String nothing) {
		super("DeploymentRelatedProcess", "http://purl.oclc.org/NET/ssnx/ssn#DeploymentRelatedProcess", Prefixes.SSN,
				null, true);
	}

	public synchronized static DeploymentRelatedProcess getDeploymentRelatedProcessInstance() {
		if (deploymentRelatedProcessInstance == null) {
			deploymentRelatedProcessInstance = new DeploymentRelatedProcess(null);
			initDeploymentRelatedProccessStaticInstance(deploymentRelatedProcessInstance);
			inittDeploymentRelatedProcessStaticInstanceTypeClasses(deploymentRelatedProcessInstance);
		}
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

		/*
		 * id and it must be unique
		 */
		super.getProperties().put("id",
				new DataTypeProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, false));

		super.getHtblPropUriName().put(Prefixes.SSN.getUri() + "deploymentProcessPart", "deploymentProcessPart");
		super.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");

		initDeploymentRelatedProcessTypeClasses();

	}

	private void initDeploymentRelatedProcessTypeClasses() {
		this.getClassTypesList().put("Deployment", Deployment.getDeploymentInstance());
	}

	private static void inittDeploymentRelatedProcessStaticInstanceTypeClasses(
			DeploymentRelatedProcess deploymentRelatedProcessInstance) {
		deploymentRelatedProcessInstance.getClassTypesList().put("Deployment", Deployment.getDeploymentInstance());
	}

	public static void initDeploymentRelatedProccessStaticInstance(
			DeploymentRelatedProcess deploymentRelatedProcessInstance) {
		/*
		 * relation between a deployment process and its constituent processes.
		 * 
		 * It says that a ssn:deploymentProcess is part of another
		 * ssn:deploymentProcess. A DeploymentRelatedProcess or its subclasses
		 * can have many deploymentProcessParts so multipleValues is enabled
		 * (one to many relation)
		 * 
		 */

		deploymentRelatedProcessInstance.getProperties().put("deploymentProcessPart",
				new ObjectProperty("deploymentProcessPart", Prefixes.SSN,
						DeploymentRelatedProcess.getDeploymentRelatedProcessInstance(), true, false));

		/*
		 * id and it must be unique
		 */
		deploymentRelatedProcessInstance.getProperties().put("id",
				new DataTypeProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, false));

		deploymentRelatedProcessInstance.getHtblPropUriName().put(Prefixes.SSN.getUri() + "deploymentProcessPart",
				"deploymentProcessPart");
		deploymentRelatedProcessInstance.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");
	}

	public static void main(String[] args) {
		DeploymentRelatedProcess deploymentRelatedProcess = new DeploymentRelatedProcess();
		System.out.println(deploymentRelatedProcess.getClassTypesList().get("Deployment").getProperties());
	}
}
