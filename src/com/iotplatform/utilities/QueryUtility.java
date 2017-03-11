package com.iotplatform.utilities;

import java.util.ArrayList;
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
	 * constructInsertQuery method takes a hashtable with class as key and lits
	 * of prefixed property values as the value of hashtable and it also takes a
	 * hashtable of subject names with class as the key and subject as the value
	 * 
	 * returns constructed insert query
	 */
	public static String constructInsertQuery(Hashtable<Class, ArrayList<PropertyValue>> htblClassPrefixedPropertyValue,
			Hashtable<Class, String> htblClassTypeSubjectName) {

		/*
		 * The two hashtables will have the same length
		 */
		Iterator<Class> htblClassPrefixedPropertyValueIterator = htblClassPrefixedPropertyValue.keySet().iterator();
		Iterator<Class> htblClassTypeSubjectNameIterator = htblClassTypeSubjectName.keySet().iterator();

		StringBuilder stringBuilder = new StringBuilder();

		/*
		 * add Prefixes and start of SPARQL insert query
		 */
		if (prefixesString == null) {
			StringBuilder prefixStringBuilder = new StringBuilder();
			for (Prefixes prefix : Prefixes.values()) {
				prefixStringBuilder.append("PREFIX	" + prefix.getPrefix() + "	<" + prefix.getUri() + ">\n");
			}

			prefixesString = prefixStringBuilder.toString();
		}

		stringBuilder.append(prefixesString);
		stringBuilder.append("INSERT DATA { \n");

		/*
		 * Add insert query body by looping on hashtables to add an instance of
		 * each class and also adding properties passed to instances of that
		 * class
		 */
		while (htblClassPrefixedPropertyValueIterator.hasNext()) {

			Class subjectClass = htblClassPrefixedPropertyValueIterator.next();
			ArrayList<PropertyValue> propValueList = htblClassPrefixedPropertyValue.get(subjectClass);

			htblClassTypeSubjectNameIterator.next();
			String subject = htblClassTypeSubjectName.get(subjectClass);

			stringBuilder.append(subject + "	a	" + "<" + subjectClass.getUri() + "> ; \n");

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
					stringBuilder.append(prefixedPropStr + "	" + value + " . \n ");
				}

				counter++;
			}
		}
		stringBuilder.append(" } ");
		return stringBuilder.toString();

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
