package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.XSDDataTypes;

/*
 * This Class maps iot-platform:Amount class in the main ontology
 * 
 * Describes the amount of of a ssn:MeasurementProperty or ssn:OperatingProperty
 */

@Component
public class Amount extends Class {

	private static Amount amountInstance;
	private Class amountSubjectClassInstance;

	public Amount() {
		super("Amount", "http://iot-platform#Amount", Prefix.IOT_PLATFORM, null, false);
		init();
	}

	private Class getAmountSubjectClassInstance() {
		if (amountSubjectClassInstance == null) {
			amountSubjectClassInstance = new Class("Amount", "http://iot-platform#Amount", Prefix.IOT_PLATFORM, null,
					false);
		}
		return amountSubjectClassInstance;
	}

	public synchronized static Amount getAmountInstance() {
		if (amountInstance == null)
			amountInstance = new Amount();

		return amountInstance;
	}

	private void init() {

		super.getProperties().put("id", new DataTypeProperty(getAmountSubjectClassInstance(), "id", Prefix.IOT_LITE,
				XSDDataTypes.string_typed, false, false));

		/*
		 * It describes the max value amount of a range of a property like
		 * operatingPowerRange
		 */
		super.getProperties().put("hasRangeMaxValue", new DataTypeProperty(getAmountSubjectClassInstance(),
				"hasRangeMaxValue", Prefix.IOT_PLATFORM, XSDDataTypes.double_typed, false, false));

		/*
		 * It describes the min value amount of a range of a property like
		 * operatingPowerRange
		 */
		super.getProperties().put("hasRangeMinValue", new DataTypeProperty(getAmountSubjectClassInstance(),
				"hasRangeMinValue", Prefix.IOT_PLATFORM, XSDDataTypes.double_typed, false, false));

		/*
		 * "Describes the value of an amount. The value of property like
		 * frequency or senstivity or batteryLifeTime. eg: 21 μA"
		 */
		super.getProperties().put("hasDataValue", new DataTypeProperty(getAmountSubjectClassInstance(), "hasDataValue",
				Prefix.IOT_PLATFORM, XSDDataTypes.double_typed, false, false));

		/*
		 * Links a thing that has a measurment unit with the units of the
		 * quantity kind it measures (e.g. A sensor -sensor1- measures
		 * temperature in Celsius: senso1 hasUnit celsius).
		 */

		super.getProperties().put("hasUnit", new ObjectProperty(getAmountSubjectClassInstance(), "hasUnit",
				Prefix.IOT_LITE, Unit.getUnitInstance(), false, false));

		super.getHtblPropUriName().put(Prefix.IOT_PLATFORM.getUri() + "hasRangeMaxValue", "hasRangeMaxValue");
		super.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "id", "id");
		super.getHtblPropUriName().put(Prefix.IOT_PLATFORM.getUri() + "hasRangeMinValue", "hasRangeMinValue");
		super.getHtblPropUriName().put(Prefix.IOT_PLATFORM.getUri() + "hasDataValue", "hasDataValue");
		super.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "hasUnit", "hasUnit");
	}

}
