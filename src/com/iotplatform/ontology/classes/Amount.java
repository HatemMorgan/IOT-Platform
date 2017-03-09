package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

/*
 * This Class maps iot-platform:Amount class in the main ontology
 * 
 * Describes the amount of of a ssn:MeasurementProperty or ssn:OperatingProperty
 */

@Component
public class Amount extends Class {

	private static Amount amountInstance;

	public Amount() {
		super("Amount", "http://iot-platform#Amount", Prefixes.IOT_PLATFORM);
		init();
	}

	public Amount(String nothing) {
		super("Amount", "http://iot-platform#Amount", Prefixes.IOT_PLATFORM);
	}

	public synchronized static Amount getAmountInstance() {
		if (amountInstance == null)
			amountInstance = new Amount(null);

		return amountInstance;
	}

	private void init() {
		/*
		 * unique ID
		 */
		super.getProperties().put("id",
				new DataTypeProperty("id", Prefixes.IOT_LITE, XSDDataTypes.string_typed, false, true));

		/*
		 * It describes the max value amount of a range of a property like
		 * operatingPowerRange
		 */
		super.getProperties().put("hasRangeMaxValue", new DataTypeProperty("hasRangeMaxValue", Prefixes.IOT_PLATFORM,
				XSDDataTypes.double_typed, false, false));

		/*
		 * It describes the min value amount of a range of a property like
		 * operatingPowerRange
		 */
		super.getProperties().put("hasRangeMinValue", new DataTypeProperty("hasRangeMinValue", Prefixes.IOT_PLATFORM,
				XSDDataTypes.double_typed, false, false));

		/*
		 * "Describes the value of an amount. The value of property like
		 * frequency or senstivity or batteryLifeTime. eg: 21 μA"
		 */
		super.getProperties().put("hasDataValue",
				new DataTypeProperty("hasDataValue", Prefixes.IOT_PLATFORM, XSDDataTypes.double_typed, false, false));

		/*
		 * Links a thing that has a measurment unit with the units of the quantity kind it measures
		 * (e.g. A sensor -sensor1- measures temperature in Celsius: senso1
		 * hasUnit celsius).
		 */

		super.getProperties().put("hasUnit",
				new ObjectProperty("hasUnit", Prefixes.IOT_LITE, Unit.getUnitInstance(), false, false));
	}

}
