package com.iotplatform.utilities;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

public class QueryUtility {

	/*
	 * constructInsertQuery method construct an insert query and return it as a
	 * string
	 */

	public static String constructInsertQuery(ArrayList<Prefixes> prefixes, String subject, Class SubjectClass,
			Hashtable<String, Object> htblPropValue) {
		StringBuilder stringBuilder = new StringBuilder();

		for (Prefixes prefix : prefixes) {
			stringBuilder.append("PREFIX	" + prefix.getPrefix() + "	<" + prefix.getUri() + ">\n");
		}

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

		stringBuilder.append("SELECT developer, property,value FROM TABLE(SEM_MATCH('SELECT ?developer ?property ?value WHERE { \n");
		stringBuilder.append("?developer	a	" + "<" + SubjectClass.getUri() + "> ; \n");
		stringBuilder.append("?property ?value . }'\n , sem_models('" + modelName + "'),null,");
		stringBuilder.append("SEM_ALIASES(" + prefixStringBuilder.toString() + "),null))");

		return stringBuilder.toString();

	}

}
