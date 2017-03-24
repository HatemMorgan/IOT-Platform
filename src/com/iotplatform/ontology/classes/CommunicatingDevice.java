package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

/*
 *  This class maps the iot-platform:CommunicatingDevice class in the ontology
 *
 *  Describes communicating device which allow communication of a device with the outer world . 
 *  ie: Zegbee, BLE and WIFI
 */

@Component
public class CommunicatingDevice extends Device {

	private static CommunicatingDevice communicatingDeviceInstance;
	private Class communicatingDeviceSubjectClassInstance;

	public CommunicatingDevice() {
		super("CommunicatingDevice", "http://iot-platform#CommunicatingDevice", Prefixes.IOT_PLATFORM, null, false);
		init();
	}

	private Class getCommunicatingDeviceSubjectClassInstance() {
		if (communicatingDeviceSubjectClassInstance == null)
			communicatingDeviceSubjectClassInstance = new Class("CommunicatingDevice",
					"http://iot-platform#CommunicatingDevice", Prefixes.IOT_PLATFORM, null, false);

		return communicatingDeviceSubjectClassInstance;
	}

	public synchronized static CommunicatingDevice getCommunicatingDeviceInstance() {
		if (communicatingDeviceInstance == null)
			communicatingDeviceInstance = new CommunicatingDevice();

		return communicatingDeviceInstance;
	}

	private void init() {

		/*
		 * Describes the bandwidth of a communicating device.
		 */
		super.getProperties().put("hasBandwidth", new DataTypeProperty(getCommunicatingDeviceSubjectClassInstance(),
				"hasBandwidth", Prefixes.IOT_PLATFORM, XSDDataTypes.double_typed, false, false));

		/*
		 * Describes the frequency of transmission of a communictating device
		 */
		super.getProperties().put("hasFrequency", new DataTypeProperty(getCommunicatingDeviceSubjectClassInstance(),
				"hasFrequency", Prefixes.IOT_PLATFORM, XSDDataTypes.double_typed, false, false));

		/*
		 * Describes the network topology of a communicating device
		 */
		super.getProperties().put("hasNetworkTopology",
				new DataTypeProperty(getCommunicatingDeviceSubjectClassInstance(), "hasNetworkTopology",
						Prefixes.IOT_PLATFORM, XSDDataTypes.string_typed, false, false));

		/*
		 * Describes the transmission power of a communicating device
		 */
		super.getProperties().put("hasTransmissionPower",
				new DataTypeProperty(getCommunicatingDeviceSubjectClassInstance(), "hasTransmissionPower",
						Prefixes.IOT_PLATFORM, XSDDataTypes.double_typed, false, false));

		/*
		 * Describes the type of a communicating device ie: BLE .
		 */
		super.getProperties().put("hasType", new DataTypeProperty(getCommunicatingDeviceSubjectClassInstance(),
				"hasType", Prefixes.IOT_PLATFORM, XSDDataTypes.string_typed, false, false));

		/*
		 * Describes the range of transmission of a communicating device.
		 */
		super.getProperties().put("rangeOfTransmission",
				new DataTypeProperty(getCommunicatingDeviceSubjectClassInstance(), "rangeOfTransmission",
						Prefixes.IOT_PLATFORM, XSDDataTypes.string_typed, false, false));

		/*
		 * Describes the duty cycle of a communicating device.
		 */
		super.getProperties().put("dutyCycle", new DataTypeProperty(getCommunicatingDeviceSubjectClassInstance(),
				"dutyCycle", Prefixes.IOT_PLATFORM, XSDDataTypes.string_typed, false, false));

		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "hasBandwidth", "hasBandwidth");
		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "hasFrequency", "hasFrequency");
		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "hasNetworkTopology", "hasNetworkTopology");
		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "hasTransmissionPower", "hasTransmissionPower");
		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "hasType", "hasType");
		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "rangeOfTransmission", "rangeOfTransmission");
		super.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri() + "dutyCycle", "dutyCycle");

		super.getSuperClassesList().add(Device.getDeviceInstance());

	}

	public static void main(String[] args) {
		CommunicatingDevice communicatingDevice = new CommunicatingDevice();

		System.out.println(communicatingDevice.getProperties().size());
		System.out.println(CommunicatingDevice.getCommunicatingDeviceInstance().getProperties().size());

		System.out.println(communicatingDevice.getHtblPropUriName().size());
		System.out.println(CommunicatingDevice.getCommunicatingDeviceInstance().getHtblPropUriName().size());

		System.out.println(communicatingDevice.getSuperClassesList());
		System.out.println(CommunicatingDevice.getCommunicatingDeviceInstance().getSuperClassesList());

		System.out.println(communicatingDevice.getClassTypesList());
		System.out.println(CommunicatingDevice.getCommunicatingDeviceInstance().getClassTypesList());

	}
}
