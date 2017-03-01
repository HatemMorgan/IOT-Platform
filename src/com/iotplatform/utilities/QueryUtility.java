package com.iotplatform.utilities;

import java.util.Hashtable;
import java.util.Iterator;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

public class QueryUtility {

	static String prefixesString = null;

	/*
	 * constructInsertQuery method construct an insert query and return it as a
	 * string
	 */

	public static String constructInsertQuery( String subject, Class SubjectClass,
			Hashtable<String, Object> htblPropValue) {
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

		Iterator<String> htblPropValueIterator = htblPropValue.keySet().iterator();

		while (htblPropValueIterator.hasNext()) {
			String prefixedPropStr = htblPropValueIterator.next();
			Object value = htblPropValue.get(prefixedPropStr);

			/*
			 * check if it is the last property value to end the query
			 */
			if (htblPropValueIterator.hasNext()) {
				stringBuilder.append(prefixedPropStr + "	" + value + " ;\n");
			} else {
				stringBuilder.append(prefixedPropStr + "	" + value + " . \n }");
			}

		}

		return stringBuilder.toString();
	}

	public static String constructSelectAllQueryNoFilters(Class SubjectClass, String modelName) {

		StringBuilder stringBuilder = new StringBuilder();

		StringBuilder prefixStringBuilder = new StringBuilder();
		int counter = 0;

		for (Prefixes prefix : Prefixes.values()) {
			if (counter == 8) {
				prefixStringBuilder.append("SEM_ALIAS('" + prefix.getPrefix() + "','" + prefix.getUri() + "')");
			} else {
				prefixStringBuilder.append("SEM_ALIAS('" + prefix.getPrefix() + "','" + prefix.getUri() + "'),");
			}

			counter++;
		}
		System.out.println(SubjectClass.getUri());
		stringBuilder.append(
				"SELECT subject, property,value FROM TABLE(SEM_MATCH('SELECT ?subject ?property ?value WHERE { ");
		stringBuilder.append("?subject	a	" + "<" + SubjectClass.getUri() + "> ; ");
		stringBuilder.append("?property ?value . }'  , sem_models('" + modelName + "'),null,");
		stringBuilder.append("SEM_ALIASES(" + prefixStringBuilder.toString() + "),null))");

		return stringBuilder.toString();

	}

}
