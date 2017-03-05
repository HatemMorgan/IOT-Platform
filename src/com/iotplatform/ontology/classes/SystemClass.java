package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

/*
 *  This class maps the group class in the ontology 
 *  A group can contains another group or persons or organizations (any agent)
 */

@Component
public class SystemClass extends Agent {

	public SystemClass() {
		super("Group","http://xmlns.com/foaf/0.1/Group", Prefixes.FOAF);
		
		

		
	}

	public static void main(String[] args) {
		Group group = new Group();
		System.out.println("herees");
	}
}
