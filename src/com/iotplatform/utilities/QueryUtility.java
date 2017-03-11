package com.iotplatform.utilities;

import java.util.ArrayList;
import java.util.Hashtable;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

public class QueryUtility {

	static String prefixesString = null;

	/*
	 * constructInsertQuery method construct an insert query and return it as a
	 * string
	 */

	public static String constructInsertQuery(String subject, Class SubjectClass,
			ArrayList<PropertyValue> propValueList) {

		StringBuilder stringBuilder = new StringBuilder();

		if (prefixesString == null) {
			StringBuilder prefixStringBuilder = new StringBuilder();
			for (Prefixes prefix : Prefixes.values()) {
				prefixStringBuilder.append("PREFIX	" + prefix.getPrefix() + "	<" + prefix.getUri() + ">\n");
			}

			prefixesString = prefixStringBuilder.toString();
		}

		stringBuilder.append(prefixesString);
		stringBuilder.append("INSERT DATA { \n");
		stringBuilder.append(subject + "	a	" + "<" + SubjectClass.getUri() + "> ; \n");

		int counter = 0;
		int size = propValueList.size();

		for (PropertyValue propertyValue : propValueList) {

			String prefixedPropStr = propertyValue.getPropertyName();
			Object value = propertyValue.getValue();

			/*
			 * check if it is the last property value to end the query
			 */
			if (counter < size - 1) {
				stringBuilder.append(prefixedPropStr + "	" + value + " ;\n");
			} else {
				stringBuilder.append(prefixedPropStr + "	" + value + " . \n }");
			}

			counter++;
		}

		return stringBuilder.toString();
	}
	

	/*
	 * constructInsertQuery method takes a hashtable with class as key and
	 * lits of prefixed property values as the value of hashtable and returns constructed insert query
	 */
	public static String constructInsertQuery(Hashtable<Class, ArrayList<PropertyValue>> classPrefixedPropertyValue) {
		
		
		
		return null;
	}

	public static String constructSelectAllQueryNoFilters(Class SubjectClass, String modelName) {

		StringBuilder stringBuilder = new StringBuilder();

		StringBuilder prefixStringBuilder = new StringBuilder();
		int counter = 0;
		int stop = Prefixes.values().length - 1;
		for (Prefixes prefix : Prefixes.values()) {
			if (counter == stop) {
				prefixStringBuilder.append("SEM_ALIAS('" + prefix.getPrefixName() + "','" + prefix.getUri() + "')");
			} else {
				prefixStringBuilder.append("SEM_ALIAS('" + prefix.getPrefixName() + "','" + prefix.getUri() + "'),");
			}

			counter++;
		}
		stringBuilder.append(
				"SELECT subject, property,value FROM TABLE(SEM_MATCH('SELECT ?subject ?property ?value WHERE { ");
		stringBuilder.append("?subject	a	" + "<" + SubjectClass.getUri() + "> ; ");
		stringBuilder.append("?property ?value . }'  , sem_models('" + modelName + "'),null,");
		stringBuilder.append("SEM_ALIASES(" + prefixStringBuilder.toString() + "),null))");

		return stringBuilder.toString();

	}

	public static void main(String[] args) {
		System.out.println(Prefixes.values().length);
	}

}
