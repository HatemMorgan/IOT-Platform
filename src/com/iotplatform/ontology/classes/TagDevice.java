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
		super("TagDevice", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#TagDevice", Prefixes.IOT_LITE);
	}

	public synchronized static TagDevice getTagDeviceInstance() {
		if (tagDeviceInstance == null) {
			tagDeviceInstance = new TagDevice();
		}
		return tagDeviceInstance;
	}

}
