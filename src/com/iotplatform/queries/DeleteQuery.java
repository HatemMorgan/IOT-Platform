package com.iotplatform.queries;

import java.util.ArrayList;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.utilities.DeletePropertyValueUtility;

public class DeleteQuery {

	private static String prefixesString = null;

	public static String constructDeleteQuery(ArrayList<DeletePropertyValueUtility> deletePropValueList,
			String individuleUniqueIdentifier, Class requestedSubjectClass) {

		StringBuilder deleteQueryBuilder = new StringBuilder();

		StringBuilder deletePartBuilder = new StringBuilder();

		StringBuilder wherePartBuilder = new StringBuilder();

		/*
		 * start delete part
		 */
		deletePartBuilder.append("DELETE { \n");

		/*
		 * start where part
		 */
		wherePartBuilder.append("WHERE { \n");
		wherePartBuilder.append("?subject a " + requestedSubjectClass.getPrefix().getPrefix()
				+ requestedSubjectClass.getName() + " . \n");
		wherePartBuilder.append("FILTER( ?subject = iot-platform:"
				+ individuleUniqueIdentifier.toLowerCase().replaceAll(" ", "") + " ) \n");

		boolean start = true;
		int varNum = 0;
		for (DeletePropertyValueUtility deletePropertyValue : deletePropValueList) {

			if (deletePropertyValue.isPropertyMultipleValued()) {
				if (start) {
					deletePartBuilder
							.append("?subject " + deletePropertyValue.getPropertyPrefixedName() + " ?var" + varNum);
					start = false;
				} else {
					deletePartBuilder
							.append("; \n " + deletePropertyValue.getPropertyPrefixedName() + " ?var" + varNum);
				}

				wherePartBuilder.append(
						"?subject " + deletePropertyValue.getPropertyPrefixedName() + " ?var" + varNum + " . \n");
				wherePartBuilder
						.append("FILTER( ?var" + varNum + " = " + deletePropertyValue.getValueToBeDeleted() + " ) \n");

			} else {

				if (start) {
					deletePartBuilder
							.append("?subject " + deletePropertyValue.getPropertyPrefixedName() + " ?var" + varNum);
					start = false;
				} else {
					deletePartBuilder
							.append("; \n " + deletePropertyValue.getPropertyPrefixedName() + " ?var" + varNum);
				}

				wherePartBuilder.append(
						"?subject " + deletePropertyValue.getPropertyPrefixedName() + " ?var" + varNum + " . \n");

			}

			varNum++;
		}

		/*
		 * constructs prefixes by iterating on Prefix enum values and then store
		 * it in the global static variable prefixesString to avoid looping
		 * every time i construct an update query
		 */
		if (prefixesString == null) {
			StringBuilder prefixStringBuilder = new StringBuilder();
			for (Prefix prefix : Prefix.values()) {
				prefixStringBuilder.append("PREFIX	" + prefix.getPrefix() + "	<" + prefix.getUri() + ">\n");
			}

			prefixesString = prefixStringBuilder.toString();
		}

		deleteQueryBuilder.append(prefixesString);

		/*
		 * end Delete part
		 */
		deletePartBuilder.append(". \n");
		deletePartBuilder.append("} \n");

		/*
		 * end where part
		 */
		wherePartBuilder.append(" } \n");

		/*
		 * append deletePartBuilder and wherePartBuilder to deleteQueryBuilder
		 */
		deleteQueryBuilder.append(deletePartBuilder);
		deleteQueryBuilder.append(wherePartBuilder);

		return deleteQueryBuilder.toString();

	}

	public static String constructDeleteQueryToDeleteFullIndivdual(String individualUniqueIdentifier,
			Class requestedSubjectClass) {

		StringBuilder deleteQueryBuilder = new StringBuilder();

		/*
		 * constructs prefixes by iterating on Prefix enum values and then store
		 * it in the global static variable prefixesString to avoid looping
		 * every time i construct an update query
		 */
		if (prefixesString == null) {
			StringBuilder prefixStringBuilder = new StringBuilder();
			for (Prefix prefix : Prefix.values()) {
				prefixStringBuilder.append("PREFIX	" + prefix.getPrefix() + "	<" + prefix.getUri() + ">\n");
			}

			prefixesString = prefixStringBuilder.toString();
		}

		deleteQueryBuilder.append(prefixesString);

		deleteQueryBuilder.append("DELETE { \n");
		deleteQueryBuilder.append(
				"iot-platform:" + individualUniqueIdentifier.toLowerCase().replaceAll(" ", "") + " ?prop ?val . \n");
		deleteQueryBuilder.append("?subject ?property ?value . \n");
		deleteQueryBuilder.append("} \n");

		deleteQueryBuilder.append("WHERE{ \n");

		deleteQueryBuilder.append(
				"iot-platform:" + individualUniqueIdentifier.toLowerCase().replaceAll(" ", "") + " ?prop ?val . \n");

		deleteQueryBuilder.append("OPTIONAL { \n");
		deleteQueryBuilder.append("?subject ?property ?value . \n");
		deleteQueryBuilder.append("FILTER( ?value = iot-platform:"
				+ individualUniqueIdentifier.toLowerCase().replaceAll(" ", "") + " ) \n");
		deleteQueryBuilder.append("} \n");
		deleteQueryBuilder.append("} \n");

		return deleteQueryBuilder.toString();

	}

}
