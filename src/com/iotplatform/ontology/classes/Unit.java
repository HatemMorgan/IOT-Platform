package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefix;

/*
 * This Class maps  qu:Unit class in the ontology
 * 
 * Unit of measurement . ex: microAmpere
 */

@Component
public class Unit extends Class {

	private static Unit unitInstance;

	public Unit() {
		super("Unit", "http://purl.org/NET/ssnx/qu/qu#Unit", Prefix.QU, null, false);
	}

	public synchronized static Unit getUnitInstance() {
		if (unitInstance == null)
			unitInstance = new Unit();

		return unitInstance;
	}

}
