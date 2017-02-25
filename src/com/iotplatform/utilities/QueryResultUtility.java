package com.iotplatform.utilities;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Property;

public class QueryResultUtility {

	
	/*
	 * constructQueryResult method used to return results without any prefixed ontology URIs
	 */
	public static Object[] constructQueryResult(String propertyURI, Object value, Class subjectClass) {

		Object[] res = new Object[2];
		
		String propertyName = subjectClass.getHtblPropUriName().get(propertyURI);
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
