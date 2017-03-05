package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

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

@Component
public class QuantityKind extends Class {

	public QuantityKind() {
		super("QuantityKind", "http://purl.org/NET/ssnx/qu/qu#QuantityKind", Prefixes.QU);

		init();
	}

	private void init() {

	}

}
