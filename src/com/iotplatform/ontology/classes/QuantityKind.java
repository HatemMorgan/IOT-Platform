package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

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

	private static QuantityKind quantityKindInstance;
	private Class quantityKindSubjectClassInstance;

	public QuantityKind() {
		super("QuantityKind", "http://purl.org/NET/ssnx/qu/qu#QuantityKind", Prefixes.QU, null, false);
		init();
	}

	private Class getQuantityKindSubjectClassInstance() {
		if (quantityKindSubjectClassInstance == null)
			quantityKindSubjectClassInstance = new Class("QuantityKind", "http://purl.org/NET/ssnx/qu/qu#QuantityKind",
					Prefixes.QU, null, false);

		return quantityKindSubjectClassInstance;
	}

	public synchronized static QuantityKind getQuantityKindInstance() {
		if (quantityKindInstance == null)
			quantityKindInstance = new QuantityKind();

		return quantityKindInstance;
	}

	private void init() {
		super.getProperties().put("id", new DataTypeProperty(getQuantityKindSubjectClassInstance(), "id",
				Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, false));

		super.getHtblPropUriName().put(Prefixes.IOT_LITE.getUri() + "id", "id");
	}

}
