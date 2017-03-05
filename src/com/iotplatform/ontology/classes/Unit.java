package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

/*
 * This Class maps  qu:Unit class in the ontology
 * 
 * Unit of measurement . ex: microAmpere
 */

@Component
public class Unit extends Class {

	public Unit() {
		super("Unit", "http://purl.org/NET/ssnx/qu/qu#Unit", Prefixes.QU);
		init();

	}

	private void init() {

	}

}
