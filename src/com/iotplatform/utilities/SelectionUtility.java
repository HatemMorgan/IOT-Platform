package com.iotplatform.utilities;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.XSDDataTypes;
import com.iotplatform.validations.RequestValidation;

@Component
public class SelectionUtility {

	RequestValidation requestValidation;

	@Autowired
	public SelectionUtility(RequestValidation requestValidation) {
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

			requestValidation.getDynamicProperties(applicationName, subjectClass);
			propertyName = subjectClass.getHtblPropUriName().get(propertyURI);
			System.out.println(propertyName);
			System.out.println(propertyURI);
		}
		Property property = subjectClass.getProperties().get(propertyName);

		if (property instanceof ObjectProperty) {
			Class objectClassType = ((ObjectProperty) property).getObject();
			value = value.toString().substring(Prefixes.IOT_PLATFORM.getUri().length(),
					value.toString().length());
		} else {
			/*
			 * datatype property
			 */
			value = typeCastValueToItsDataType((DataTypeProperty)property,value);
		}

		res[0] = propertyName;
		res[1] = value;

		return res;
	}

	private static Object typeCastValueToItsDataType(DataTypeProperty dataTypeProperty, Object value) {
		if (XSDDataTypes.boolean_type.equals(dataTypeProperty.getDataType())) {
			return Boolean.parseBoolean(value.toString());
		}

		if (XSDDataTypes.decimal_typed.equals(dataTypeProperty.getDataType())) {
			return Double.parseDouble(value.toString());
		}

		if (XSDDataTypes.float_typed.equals(dataTypeProperty.getDataType())) {
			return Float.parseFloat(value.toString());
		}

		if (XSDDataTypes.integer_typed.equals(dataTypeProperty.getDataType())) {
			return Integer.parseInt(value.toString());
		}

		if (XSDDataTypes.string_typed.equals(dataTypeProperty.getDataType())) {
			return value.toString();
		}

		if (XSDDataTypes.dateTime_typed.equals(dataTypeProperty.getDataType())) {
			return Date.parse(value.toString());
		}

		if (XSDDataTypes.double_typed.equals(dataTypeProperty.getDataType())) {
			return Double.parseDouble(value.toString());
		}
		return null;
	}

}
