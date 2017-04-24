package com.iotplatform.queries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class DynamicOntologyQuery {

	public static String constructDynamicOntologyInsertQuery(
			LinkedHashMap<String, LinkedHashMap<String, Object>> validationResult) {

		StringBuilder insertQueryBuilder = new StringBuilder();

		/*
		 * Iterate over validationResult
		 */
		Iterator<String> validationResultIter = validationResult.keySet().iterator();
		while (validationResultIter.hasNext()) {

			String classPrefixedName = validationResultIter.next();
			LinkedHashMap<String, Object> htblClassMap = validationResult.get(classPrefixedName);

			/*
			 * iterate over htblClassMap
			 */
			Iterator<String> htblClassMapIter = htblClassMap.keySet().iterator();
			while (htblClassMapIter.hasNext()) {
				String key = htblClassMapIter.next();

				/*
				 * check that key is prefixedClassName where its value is the
				 * prefixedClassName of the newClass and also check that this
				 * class is a new one by checking that its map does not have
				 * exist key
				 */
				if (key.equals("prefixedClassName") && !htblClassMap.containsKey("exist")) {
					insertQueryBuilder.append(htblClassMap.get("prefixedClassName") + " a owl:Class ");
				}

				/*
				 * if key equals uniqueIdentiferProperty so this class has a
				 * uniqueIdentifierProperty
				 */
				if (key.equals("uniqueIdentiferProperty") && !htblClassMap.containsKey("exist")) {
					insertQueryBuilder.append(
							";  \n iot-platform:hasUniqueIdentifier " + htblClassMap.get("uniqueIdentiferProperty"));
				}

				if (key.equals("subClassList")) {

				}

			}
		}

		return insertQueryBuilder.toString();
	}

	/*
	 * constructQueryPartForSubClass is used to construct insert query patterns
	 * that defines subclasses
	 * 
	 * It takes subClassesList and insertQueryBuilder from
	 * constructDynamicOntologyInsertQuery method that call this method
	 * 
	 * It also takes a boolean isFirstPattern that tells if subClassesList is
	 * the firstPattern
	 * 
	 * if it is the first pattern. the first pattern must have prefixedClassName
	 * as the subject eg. ssn:Device rdfs:sub
	 * 
	 */
	private static void constructQueryPartForSubClass(ArrayList<String> subClassesList,
			StringBuilder insertQueryBuilder, boolean isFirstPattern, String prefixedClassName) {

		for (String subClassPefixedName : subClassesList) {

			if (isFirstPattern) {
				insertQueryBuilder.append("rdfs:subClassOf " + subClassPefixedName);
			}

		}

	}

}
