package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Prefixes;

/*
 * This class maps the iot-lite:TagDevice class in the ontology
 * 
 * Tag Device such as QR code or bar code.
 */

@Component
public class TagDevice extends Device {

	private static TagDevice tagDeviceInstance;

	public TagDevice() {
		super("TagDevice", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#TagDevice", Prefixes.IOT_LITE, null, false);
		super.getSuperClassesList().add(Device.getDeviceInstance());
	}

	public synchronized static TagDevice getTagDeviceInstance() {
		if (tagDeviceInstance == null) {
			tagDeviceInstance = new TagDevice();
		}
		return tagDeviceInstance;
	}

	public static void main(String[] args) {
		TagDevice tagDevice = new TagDevice();

		System.out.println(tagDevice.getProperties().size());
		System.out.println(TagDevice.getTagDeviceInstance().getProperties().size());

		System.out.println(tagDevice.getHtblPropUriName().size());
		System.out.println(TagDevice.getTagDeviceInstance().getHtblPropUriName().size());

		System.out.println(tagDevice.getSuperClassesList());
		System.out.println(TagDevice.getTagDeviceInstance().getSuperClassesList());

		System.out.println(tagDevice.getClassTypesList());
		System.out.println(TagDevice.getTagDeviceInstance().getClassTypesList());

	}

}
