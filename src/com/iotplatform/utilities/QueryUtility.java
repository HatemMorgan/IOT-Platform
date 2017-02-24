package com.iotplatform.utilities;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;


import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;

public class QueryUtility {


	/*
	 * constructInsertQuery method construct an insert query and return it as a string
	 */

	public static String constructInsertQuery(ArrayList<Prefixes> prefixes, String subject, Class SubjectClass,
			Hashtable<String, Object> htblPropValue) {
		StringBuilder stringBuilder = new StringBuilder();
		System.out.println(prefixes.size());
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

}
