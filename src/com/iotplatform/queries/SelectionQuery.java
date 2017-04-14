package com.iotplatform.queries;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.utilities.QueryField;
import com.iotplatform.utilities.QueryVariable;

public class SelectionQuery {

	/*
	 * constructSelectQuery is used to construct a select query and it returns
	 * an object array of size = 2
	 * 
	 * It contains 1- stringQuery 2- Hashtable<String, QueryVariable>
	 * htblSubjectVariables which holds the projected variables as key and
	 * queryVariable as value of the key this hashtable is used when constructed
	 * the results from database
	 */
	public static Object[] constructSelectQuery(
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<QueryField>>> htblClassNameProperty,
			String mainClassPrefixedName, String mainInstanceUniqueIdentifier, String applicationModelName) {

		/*
		 * main builder for dynamically building the query
		 */
		StringBuilder queryBuilder = new StringBuilder();

		/*
		 * filterConditionsBuilder is for building filter conditions
		 */
		StringBuilder filterConditionsBuilder = new StringBuilder();

		/*
		 * endGraphPatternBuilder is for building other graph nodes and patterns
		 * and add them to the end of the main graph pattern
		 */
		StringBuilder endGraphPatternBuilder = new StringBuilder();

		/*
		 * sparqlProjectedFieldsBuilder is for building selection field part for
		 * injected SPARQL query
		 */
		StringBuilder sparqlProjectedFieldsBuilder = new StringBuilder();

		/*
		 * sqlProjectedFieldsBuilder is for building selection field part for
		 * SQL query that contains the injected SPARQL query
		 */
		StringBuilder sqlProjectedFieldsBuilder = new StringBuilder();

		/*
		 * Getting propertyList of the main subject(this list constructs the
		 * main graph pattern)
		 */
		ArrayList<QueryField> currentClassPropertyValueList = htblClassNameProperty.get(mainClassPrefixedName)
				.get(mainInstanceUniqueIdentifier);

		/*
		 * htblPropValue has key prefiexedPropertyName and value
		 * valueOfProperty.
		 *
		 * It is used to avoid duplicating same triple patterns eg: mbox when
		 * person has more the one email so I want to add foaf:mbox ?var only
		 * one time for this instance
		 */
		Hashtable<String, Object> htblPropValue = new Hashtable<>();

		/*
		 * htblSubjectVariablePropertyName holds subjectVariable as key and its
		 * associated property as value
		 */
		Hashtable<String, QueryVariable> htblSubjectVariables = new Hashtable<>();

		/*
		 * htblIndividualIDSubjVarName is used to hold uniqueIdentifier of an
		 * Individual as key and it subjectVariableName to be used after that to
		 * reference that a property of the individual with uniqueIdentifier has
		 * a subjectVariableName which is stored here. This will be used in
		 * construction of htblSubjectVariables that is used in selectionUtility
		 * to construct the queryResults
		 * 
		 * I used this data structure to solve the problem of storing that a
		 * nested object has a subject with SubjectVariableName because I
		 * increment a counter and recursively using to stringBuilders to keep
		 * the order of graphPatterns correct. so as the input become complex
		 * with many nested objects I cannot keep track of the correct
		 * SubjectVariableName
		 */
		Hashtable<String, String> htblIndividualIDSubjVarName = new Hashtable<>();
		/*
		 * start of the query graph patterns (this triple pattern minimize
		 * search area because of specifing that the first subject variable
		 * (?subject0) is of type certin class )
		 */
		queryBuilder.append("?subject0  " + " a " + "<" + mainClassPrefixedName + ">");
		sparqlProjectedFieldsBuilder.append(" ?subject0");
		sqlProjectedFieldsBuilder.append(" subject0");

		/*
		 * counters that are used to assign different variables .
		 *
		 * They must be arrays of integers in order to be able to pass by
		 * reference
		 */
		int[] vairableNum = { 0 };
		int[] subjectNum = { 1 };

		/*
		 * iterating on the propertyList of the main subject to construct the
		 * main graph pattern and break through values recursively to construct
		 * other graph nodes and patterns using endGraphPatternBuilder
		 */
		for (QueryField queryField : currentClassPropertyValueList) {

			constructSelectQueryHelper(htblClassNameProperty, htblSubjectVariables, htblIndividualIDSubjVarName,
					queryBuilder, filterConditionsBuilder, endGraphPatternBuilder, sparqlProjectedFieldsBuilder,
					sqlProjectedFieldsBuilder, mainClassPrefixedName, mainInstanceUniqueIdentifier, queryField,
					htblPropValue, vairableNum, subjectNum);

		}

		/*
		 * construct prefixes
		 */
		StringBuilder prefixStringBuilder = new StringBuilder();
		int counter = 0;
		int stop = Prefix.values().length - 1;
		for (Prefix prefix : Prefix.values()) {
			if (counter == stop) {
				prefixStringBuilder.append("SEM_ALIAS('" + prefix.getPrefixName() + "','" + prefix.getUri() + "')");
			} else {
				prefixStringBuilder.append("SEM_ALIAS('" + prefix.getPrefixName() + "','" + prefix.getUri() + "'),");
			}

			counter++;
		}

		/*
		 * Appending the endGraphPatternBuilder to the end of the queryBuilder
		 */
		queryBuilder.append(endGraphPatternBuilder.toString());

		/*
		 * complete query by appending projection field and graph patterns
		 */
		StringBuilder mainBuilder = new StringBuilder();

		mainBuilder.append("SELECT " + sqlProjectedFieldsBuilder.toString() + "\n FROM TABLE( SEM_MATCH ( ' SELECT "
				+ sparqlProjectedFieldsBuilder.toString() + " \n WHERE { \n " + queryBuilder.toString()
				+ " }' , \n sem_models('" + applicationModelName + "'),null, \n SEM_ALIASES("
				+ prefixStringBuilder.toString() + "),null))");

		// filterConditionsBuilder.append(" )");

		/*
		 * complete end of the subquery structure
		 */
		// queryBuilder.append(" " + filterConditionsBuilder.toString() + " \n
		// }}");

//		System.out.println(htblSubjectVariables);
		Object[] returnObject = { mainBuilder.toString(), htblSubjectVariables };
		return returnObject;
	}

	/*
	 * A recursive method that construct a select query
	 *
	 * it takes the reference of
	 * htblClassNameProperty,queryBuilder,filterConditionsBuilder and
	 * endGraphPatternBuilder to construct query
	 *
	 * It also recursively take propertyValue and currentClassPrefixedName to
	 * breakdown all values and construct a proper graph patterns and filter in
	 * the query
	 *
	 */
	private static void constructSelectQueryHelper(
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<QueryField>>> htblClassNameProperty,
			Hashtable<String, QueryVariable> htblSubjectVariables,
			Hashtable<String, String> htblIndividualIDSubjVarName, StringBuilder queryBuilder,
			StringBuilder filterConditionsBuilder, StringBuilder endGraphPatternBuilder,
			StringBuilder sparqlProjectedFieldsBuilder, StringBuilder sqlProjectedFieldsBuilder, String currentClassURI,
			String currentClassInstanceUniqueIdentifier, QueryField queryField, Hashtable<String, Object> htblPropValue,
			int[] vairableNum, int[] subjectNum) {

		if (!htblIndividualIDSubjVarName.containsKey(currentClassInstanceUniqueIdentifier)) {
			/*
			 * if htblIndividualIDSubjVarName does not contain the
			 * currentClassInstanceUniqueIdentifier so I have to add it and its
			 * subjectVariableName
			 */
			htblIndividualIDSubjVarName.put(currentClassInstanceUniqueIdentifier, "subject" + (subjectNum[0] - 1));
		}

		/*
		 * The property is an objectProperty and the value is a nestedObject(new
		 * class object instance that has its own properties and values)
		 */
		if (queryField.isValueObject()) {

			/*
			 * add property and reference to graph node the represent the object
			 * value node
			 *
			 * eg. SELECT (COUNT(*) as ?isUnique ) WHERE { ?subject0 a
			 * iot-platform:Developer ; foaf:knows ?subject1. ?subject1 a
			 * foaf:Person.
			 *
			 * ?subject1 is the object node reference and is linked to main node
			 * (?subject0) with objectProperty (foaf:knows)
			 */

			if (htblClassNameProperty.get(currentClassURI).get(currentClassInstanceUniqueIdentifier)
					.indexOf(queryField) < htblClassNameProperty.get(currentClassURI)
							.get(currentClassInstanceUniqueIdentifier).size() - 1) {
				queryBuilder.append(" ; \n" + queryField.getPrefixedPropertyName() + " ?subject" + subjectNum[0]);
			} else {
				/*
				 * it is the last property so we will end the previous triple
				 * pattern with ; and this one with .
				 */
				queryBuilder
						.append(" ; \n" + queryField.getPrefixedPropertyName() + " ?subject" + subjectNum[0] + " . \n");

			}

			/*
			 * add subjectVarible and it property to
			 * htblSubjectVariablePropertyName to be used to properly construct
			 * query results
			 */
			htblSubjectVariables.put("subject" + subjectNum[0],
					new QueryVariable(htblIndividualIDSubjVarName.get(currentClassInstanceUniqueIdentifier),
							getPropertyName(queryField.getPrefixedPropertyName()), currentClassURI));

			/*
			 * get the value classType which is stored in the
			 * prefixedObjectValueClassName of the propertyValue this classType
			 * represent the prefiexedClassName of the value class type and it
			 * was added by RequestValidation class
			 */
			String objectClassTypeName = queryField.getObjectValueTypeClassName();

			/*
			 * get the uniqueIdentifer of the objectProperty inOrder to
			 * breakDown the nestedObject to construct Recursively the query
			 */
			String objectVaueUniqueIdentifier = queryField.getIndividualUniqueIdentifier();

			/*
			 * get the objectValueInstance's propertyValueList
			 */
			ArrayList<QueryField> queryFieldList = htblClassNameProperty.get(objectClassTypeName)
					.get(objectVaueUniqueIdentifier);

			/*
			 * is for building other graph nodes and patterns and add them to
			 * the end of the main graph pattern ( graph pattern with node
			 * representing the subjectClassInstance of the nestedObjectValue )
			 */
			StringBuilder tempBuilder = new StringBuilder();

			/*
			 * start the new graph pattern that will be added to the end
			 */
			tempBuilder.append("?subject" + subjectNum[0] + " a " + "<" + objectClassTypeName + ">");
			sparqlProjectedFieldsBuilder.append("  ?subject" + subjectNum[0]);
			sqlProjectedFieldsBuilder.append(" , subject" + subjectNum[0]);
			subjectNum[0]++;

			/*
			 * objectValueHtblPropValue is the same htblPropValue but for this
			 * objectValueInstance I doing this to keep every instance
			 * independent from other instance even if more than one instance
			 * have the same ontology class type
			 *
			 * objectValueHtblPropValue has key prefiexedPropertyName and value
			 * valueOfProperty.
			 *
			 * It is used to avoid duplicating same triple patterns eg: mbox
			 * when person has more the one email so I want to add foaf:mbox
			 * ?var only one time for this instance
			 */
			Hashtable<String, Object> objectValueHtblPropValue = new Hashtable<>();

			for (QueryField objectPropertyValue : queryFieldList) {

				constructSelectQueryHelper(htblClassNameProperty, htblSubjectVariables, htblIndividualIDSubjVarName,
						tempBuilder, filterConditionsBuilder, endGraphPatternBuilder, sparqlProjectedFieldsBuilder,
						sqlProjectedFieldsBuilder, objectClassTypeName, objectVaueUniqueIdentifier, objectPropertyValue,
						objectValueHtblPropValue, vairableNum, subjectNum);
			}

			/*
			 * append tempBuilder to endGraphPatternBuilder to add the
			 * nestedObject Patterns to the end of its main patterns (above
			 * graph node that has relation to nestedObject node)
			 */
			endGraphPatternBuilder.append(tempBuilder.toString());

			/*
			 * ReIntialize tempBuilder to remove all what was builded on it to
			 * be used again
			 */
			tempBuilder = new StringBuilder();

		} else {

			/*
			 * The property is not an objectProperty it is property that has a
			 * literal value (the value can be datatype value (eg.
			 * string,int,float) or a reference to an existed object instance
			 */
			if (htblClassNameProperty.get(currentClassURI).get(currentClassInstanceUniqueIdentifier)
					.indexOf(queryField) < htblClassNameProperty.get(currentClassURI)
							.get(currentClassInstanceUniqueIdentifier).size() - 1) {

				/*
				 * Check if the prefixedPropertyName of this instance was added
				 * before
				 */
				if (!htblPropValue.containsKey(queryField.getPrefixedPropertyName())) {

					/*
					 * if we add a new graph pattern (by checking if the
					 * prefixedPropertyName was not added before for this
					 * current instance) then increment the variableNum[0]
					 *
					 * add prifixedPropertyName to htblPropValue to avoid
					 * duplicating it again for this instance
					 *
					 * I do this after adding filter condition and graph pattern
					 * to maintain the same variableNames between graph pattern
					 * added and condtion
					 */
					if (!htblPropValue.containsKey(queryField.getPrefixedPropertyName())) {
						htblPropValue.put(queryField.getPrefixedPropertyName(), "?var" + vairableNum[0]);
						sparqlProjectedFieldsBuilder.append("  ?var" + vairableNum[0]);
						sqlProjectedFieldsBuilder.append(" , var" + vairableNum[0]);

						/*
						 * add var variable and it property to
						 * htblSubjectVariablePropertyName to be used to
						 * properly construct query results
						 */
						htblSubjectVariables.put("var" + vairableNum[0],
								new QueryVariable(htblIndividualIDSubjVarName.get(currentClassInstanceUniqueIdentifier),
										getPropertyName(queryField.getPrefixedPropertyName()), currentClassURI));

						vairableNum[0]++;
					}

					/*
					 * it is not the last property so we will end the previous
					 * triple pattern with ;
					 */
					queryBuilder.append(" ; \n" + queryField.getPrefixedPropertyName() + " "
							+ htblPropValue.get(queryField.getPrefixedPropertyName()));

				}

			} else {
				if (!htblPropValue.containsKey(queryField.getPrefixedPropertyName())) {

					/*
					 * if we add a new graph pattern (by checking if the
					 * prefixedPropertyName was not added before for this
					 * current instance) then increment the variableNum[0]
					 *
					 * add prifixedPropertyName to htblPropValue to avoid
					 * duplicating it again for this instance
					 *
					 * I do this after adding filter condition and graph pattern
					 * to maintain the same variableNames between graph pattern
					 * added and condtion
					 */
					if (!htblPropValue.containsKey(queryField.getPrefixedPropertyName())) {
						htblPropValue.put(queryField.getPrefixedPropertyName(), "?var" + vairableNum[0]);
						sparqlProjectedFieldsBuilder.append("  ?var" + vairableNum[0]);
						sqlProjectedFieldsBuilder.append(" , var" + vairableNum[0]);
						/*
						 * add var variable and it property to
						 * htblSubjectVariablePropertyName to be used to
						 * properly construct query results
						 */
						htblSubjectVariables.put("var" + vairableNum[0],
								new QueryVariable(htblIndividualIDSubjVarName.get(currentClassInstanceUniqueIdentifier),
										getPropertyName(queryField.getPrefixedPropertyName()), currentClassURI));
						vairableNum[0]++;
					}

					/*
					 * it is the last property so we will end the previous
					 * triple pattern with ; and this one with .
					 */
					queryBuilder.append(" ; \n" + queryField.getPrefixedPropertyName() + " "
							+ htblPropValue.get(queryField.getPrefixedPropertyName()) + " . \n");

				} else {

					/*
					 * end the previous triple pattern with .
					 *
					 * Because after checking it was found that the end
					 * prefixedPropertyName was added before to this instance's
					 * graph patterns
					 */
					queryBuilder.append(" . \n");
				}

			}

		}

	}

	private static String getPropertyName(String prefixedPropertyName) {
		int startIndex = 0;
		for (int i = 0; i < prefixedPropertyName.length(); i++) {
			if (prefixedPropertyName.charAt(i) == ':') {
				startIndex = i;
				break;
			}
		}

		return prefixedPropertyName.substring(startIndex + 1, prefixedPropertyName.length());
	}

	/*
	 * constructSelectAllQueryNoFilters method is used to create a selectAll
	 * query
	 */
	public static String constructSelectAllQueryNoFilters(Class SubjectClass, String modelName) {

		StringBuilder stringBuilder = new StringBuilder();

		StringBuilder prefixStringBuilder = new StringBuilder();
		int counter = 0;
		int stop = Prefix.values().length - 1;
		for (Prefix prefix : Prefix.values()) {
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

}
