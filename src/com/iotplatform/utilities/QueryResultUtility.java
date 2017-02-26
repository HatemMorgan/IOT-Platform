package com.iotplatform.utilities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Property;
import com.iotplatform.validations.RequestValidation;

@Component
public class QueryResultUtility {

	RequestValidation requestValidation;

	
	@Autowired
	public QueryResultUtility(RequestValidation requestValidation) {
		this.requestValidation = requestValidation;
	}



	/*
	 * constructQueryResult method used to return results without any prefixed
	 * ontology URIs
	 */
	public Object[] constructQueryResult(String applicationName, String propertyURI, Object value, Class subjectClass) {

		Object[] res = new Object[2];

		String propertyName = subjectClass.getHtblPropUriName().get(propertyURI);

		if (propertyName == null) {

			/*
			 * update subject class properties list by loading the dynamic
			 * properties from database
			 */
			System.out.println("-----------> "+applicationName);
			requestValidation.getDynamicProperties(applicationName, subjectClass);
			propertyName = subjectClass.getHtblPropUriName().get(propertyURI);

		}
		System.out.println(propertyURI);
		System.out.println(propertyName);
		
		Property property = subjectClass.getProperties().get(propertyName);

		if (property instanceof ObjectProperty) {
			Class objectClassType = ((ObjectProperty) property).getObject();
			value = value.toString().substring(objectClassType.getPrefix().getUri().length(),
					value.toString().length());
		}

		res[0] = propertyName;
		res[1] = value;

		return res;
	}

}
