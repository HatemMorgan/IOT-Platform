package com.iotplatform.utilities;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import com.iotplatform.ontology.Class;

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
		
		while(htblPropValueIterator.hasNext()){
			
			String propertyName = htblPropValueIterator.next();
			
			
			
		}
		
		return null;
	}

}
