package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Prefixes;

/*
 * This class maps the iot-lite:ActuatingDevice class in the ontology
 * 
 * Device that can actuate over an object or QuantityKind.
 */

@Component
public class ActuatingDevice extends Device {

	private static ActuatingDevice actuatingDeviceInstance;

	public ActuatingDevice() {
		super("ActuatingDevice", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#ActuatingDevice", Prefixes.IOT_LITE,
				null, false);

		super.getSuperClassesList().add(Device.getDeviceInstance());
	}

	public synchronized static ActuatingDevice getActuatingDeviceInstance() {
		if (actuatingDeviceInstance == null) {
			actuatingDeviceInstance = new ActuatingDevice();
		}
		return actuatingDeviceInstance;
	}

	public static void main(String[] args) {
		ActuatingDevice actuatingDevice = new ActuatingDevice();

		System.out.println(actuatingDevice.getProperties().size());
		System.out.println(ActuatingDevice.getActuatingDeviceInstance().getProperties().size());

		System.out.println(actuatingDevice.getHtblPropUriName().size());
		System.out.println(ActuatingDevice.getActuatingDeviceInstance().getHtblPropUriName().size());

		System.out.println(actuatingDevice.getSuperClassesList());
		System.out.println(ActuatingDevice.getActuatingDeviceInstance().getSuperClassesList());

		System.out.println(actuatingDevice.getClassTypesList());
		System.out.println(ActuatingDevice.getActuatingDeviceInstance().getClassTypesList());

	}

}
