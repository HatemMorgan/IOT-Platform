package com.iotplatform.ontology.classes;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

/*
 * This class maps qu:QuantityKind Class in the ontology
 * 
 * A QuantityKind is an abstract classifier that represents the [VIM] concept of "kind of quantity" that is defined
 *  as "aspect common to mutually comparable quantities." A QuantityKind represents the essence of a quantity without
 *   any numerical value or unit.  
 * 
 * eg: Tempreture Sensor hasQuantityKind tempreture(instance from QuantityKind Class)
 * 	
 */

public class QuantityKind extends Class {

	public QuantityKind(String name, String uri, Prefixes prefix) {
		super("QuantityKind", "http://purl.org/NET/ssnx/qu/qu#QuantityKind", Prefixes.QU);

		init();
	}

	private void init() {

	}

}
