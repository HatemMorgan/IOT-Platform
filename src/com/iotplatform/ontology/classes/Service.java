package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.XSDDataTypes;

/*
 * This Class maps iot-lite:Service Class in the ontology
 * 
 * Service provided by an IoT Device
 * 
 */

@Component
public class Service extends Class {

	private static Service serviceInstance;
	private Class serviceSubjectClassInstance;

	public Service() {
		super("Service", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Service", Prefix.IOT_LITE, null, false);
		init();
	}

	private Class getServiceSubjectClassInstance() {
		if (serviceSubjectClassInstance == null)
			serviceSubjectClassInstance = new Class("Service", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Service",
					Prefix.IOT_LITE, null, false);

		return serviceSubjectClassInstance;
	}

	public synchronized static Service getServiceInstance() {
		if (serviceInstance == null)
			serviceInstance = new Service();

		return serviceInstance;
	}

	private void init() {

		/*
		 * Endpoint of a service (i.e. URL that provides a RESTful interface
		 * (website fo example to give more information about the service ) to
		 * access a service) ,
		 */
		super.getProperties().put("endpoint", new DataTypeProperty(getServiceSubjectClassInstance(), "endpoint",
				Prefix.IOT_LITE, XSDDataTypes.string_typed, false, false));

		/*
		 * Description of the service.
		 */
		super.getProperties().put("interfaceDescription", new DataTypeProperty(getServiceSubjectClassInstance(),
				"interfaceDescription", Prefix.IOT_LITE, XSDDataTypes.string_typed, false, false));

		/*
		 * Defines the type of interface of the service endpoint.
		 */
		super.getProperties().put("interfaceType", new DataTypeProperty(getServiceSubjectClassInstance(),
				"interfaceType", Prefix.IOT_LITE, XSDDataTypes.string_typed, false, false));

		super.getProperties().put("id", new DataTypeProperty(getServiceSubjectClassInstance(), "id", Prefix.IOT_LITE,
				XSDDataTypes.string_typed, false, false));

		super.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "id", "id");
		super.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "endpoint", "endpoint");
		super.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "interfaceDescription", "interfaceDescription");
		super.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "interfaceType", "interfaceType");

	}
}
