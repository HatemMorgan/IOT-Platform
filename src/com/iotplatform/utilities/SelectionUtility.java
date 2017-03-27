package com.iotplatform.utilities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.XSDDataTypes;

@Component
public class SelectionUtility {

	private DynamicPropertiesUtility dynamicPropertiesUtility;

	@Autowired
	public SelectionUtility(DynamicPropertiesUtility dynamicPropertiesUtility) {
		this.dynamicPropertiesUtility = dynamicPropertiesUtility;
	}

	/*
	 * constructQueryResult method used to return results without any prefixed
	 * ontology URIs
	 */

	private Object[] constructSinglePropertyValuePair(String applicationName, String propertyURI, Object value,
			Class subjectClass) {
		Object[] res = new Object[2];

		String propertyName = subjectClass.getHtblPropUriName().get(propertyURI);

		if (propertyName == null) {

			/*
			 * update subject class properties list by loading the dynamic
			 * properties from database
			 */

			dynamicPropertiesUtility.getDynamicProperties(applicationName, subjectClass);
			propertyName = subjectClass.getHtblPropUriName().get(propertyURI);

		}
		Property property = subjectClass.getProperties().get(propertyName);

		if (property instanceof ObjectProperty) {
			value = value.toString().substring(Prefixes.IOT_PLATFORM.getUri().length(), value.toString().length());
		} else {
			/*
			 * datatype property
			 */
			value = typeCastValueToItsDataType((DataTypeProperty) property, value);
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
	/*
	 * constractResponeJsonObject method is responsible take two inputs
	 * 
	 * 1-The ResultSet returned from querying the application model and take the
	 * 2-subjectClass to check if the property returned has multiple values or
	 * not. If it has multiple value it should be returned as an arrayList
	 */

	public List<Hashtable<String, Object>> constractResponeJsonObjectForListSelection(String applicationName,
			ResultSet results, Class subjectClass) throws SQLException {

		List<Hashtable<String, Object>> responseJson = new ArrayList<>();

		Hashtable<Object, Hashtable<String, Object>> temp = new Hashtable<>();

		Hashtable<String, Property> subjectClassProperties = subjectClass.getProperties();

		while (results.next()) {

			Object subject = results.getObject(1);

			/*
			 * create a new hashtable to hold subject's property and value
			 */

			if (temp.size() == 0) {
				Hashtable<String, Object> htblSubjectPropVal = new Hashtable<>();
				temp.put(subject, htblSubjectPropVal);
				responseJson.add(htblSubjectPropVal);
			}

			// skip rdf:type property

			if (results.getString(2).equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
				continue;
			}

			Object[] preparedPropVal = constructSinglePropertyValuePair(applicationName, results.getString(2),
					results.getString(3), subjectClass);

			String propertyName = preparedPropVal[0].toString();
			Object value = preparedPropVal[1];

			/*
			 * check if the property passed is a multiValues Property it is
			 * after calling constructSinglePropertyValuePair method because
			 * constructSinglePropertyValuePair checks if the property was
			 * cached or need to be cached
			 */

			Property property = subjectClassProperties.get(propertyName);

			if (property.isMulitpleValues()) {

				/*
				 * check if the property was added before to use the previous
				 * created value array
				 */

				if (temp.containsKey(subject)) {

					if (temp.get(subject).containsKey(propertyName)) {

						((ArrayList) temp.get(subject).get(propertyName)).add(value);
					} else {

						/*
						 * property was not added before so create a new
						 * arraylist of objects to hold values and add it to
						 * multipleValuePropNameValueArr hashtable
						 */

						ArrayList<Object> valueList = new ArrayList<>();
						valueList.add(value);
						temp.get(subject).put(propertyName, valueList);
					}
				} else {
					Hashtable<String, Object> htblAdminPropVal = new Hashtable<>();
					temp.put(subject, htblAdminPropVal);

					if (temp.get(subject).containsKey(propertyName)) {

						((ArrayList) temp.get(subject).get(propertyName)).add(value);
					} else {

						/*
						 * property was not added before so create a new
						 * arraylist of objects to hold values and add it to
						 * multipleValuePropNameValueArr hashtable
						 */

						ArrayList<Object> valueList = new ArrayList<>();
						valueList.add(value);
						temp.get(subject).put(propertyName, valueList);
					}
					responseJson.add(htblAdminPropVal);
				}

			} else {

				/*
				 * as long as the current subject equal to subject got from the
				 * results then add the property and value to the hashtable . If
				 * they are not the same this means that this is a new subject
				 * so we have to construct a new hashtable to hold its data
				 */

				if (temp.containsKey(subject)) {
					temp.get(subject).put(propertyName, value);
				} else {

					Hashtable<String, Object> htblAdminPropVal = new Hashtable<>();
					temp.put(subject, htblAdminPropVal);
					temp.get(subject).put(propertyName, value);
					responseJson.add(htblAdminPropVal);

				}
			}
		}
		return responseJson;
	}
}