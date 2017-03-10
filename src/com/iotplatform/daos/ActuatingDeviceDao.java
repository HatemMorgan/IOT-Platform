package com.iotplatform.daos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.iotplatform.ontology.classes.ActuatingDevice;
import com.iotplatform.utilities.SelectionUtility;

import oracle.spatial.rdf.client.jena.Oracle;

@Repository("actuatingDeviceDao")
public class ActuatingDeviceDao {

	private Oracle oracle;
	private SelectionUtility selectionUtility;
	private ActuatingDevice actuatingDevice;
	 

	
	

}
