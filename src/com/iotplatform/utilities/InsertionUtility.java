package com.iotplatform.utilities;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Property;

/*
 *  InsertionUtility is utility class for insertion queries and APIS
 */

public class InsertionUtility {

	/*
	 * constructPropValueList method is used to construct a proper propertyValue
	 * list by removing arrays of values for a given property and change it to
	 * multiple propertyValue objects to be able to insert it using sparql
	 */
	public static ArrayList<PropertyValue> constructPropValueList(Hashtable<String, Object> htblPropValue,
			Class subjectClass) {

		ArrayList<PropertyValue> propValueList = new ArrayList<>();
		Iterator<String> htblPropValueIterator = htblPropValue.keySet().iterator();

		while (htblPropValueIterator.hasNext()) {

			String propertyName = htblPropValueIterator.next();

			Property property = subjectClass.getProperties().get(propertyName);

			Object value = htblPropValue.get(propertyName);

			/*
			 * multiple value and the value passed is instance of of array so It
			 * must be broken to propertyValue objects to be able to check if
			 * the values are valid in case the property is an objectProperty
			 * and to allow inserting values as triples
			 */

			if (property.isMulitpleValues() && value instanceof Object[]) {
				Object[] valueArr = (Object[]) value;
				for (int i = 0; i < valueArr.length; i++) {
					PropertyValue propertyValue = new PropertyValue(propertyName, valueArr[i]);
					propValueList.add(propertyValue);
				}
			} else {
				
				/*
				 * Its a normal property value pair so I will only create a
				 * propertyValue object to hold them and add the object to
				 * propValueList
				 */
				
				PropertyValue propertyValue = new PropertyValue(propertyName, value);
				propValueList.add(propertyValue);
				
			}

		}

		return propValueList;
	}

}
