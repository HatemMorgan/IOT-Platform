package com.iotplatform.queries;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.utilities.QueryField;
import com.iotplatform.utilities.QueryVariable;

/*
 * SelectionQuery is used to construct the appropriate select query to query data.
 * 
 * 1- It constructs a select queries with graph patterns and optional patterns after taking 
 * LinkedHashMap<String, LinkedHashMap<String, ArrayList<QueryField>>> htblClassNameProperty
 * input from queryRequestValidation which was called by the service 
 * 
 * 2- It constructs a selectAll query for a given applicationName and a className
 */

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
		 * htblValueTypePropObjectVariable is used with property that has
		 * multiple values type eg: foaf:member in foaf:Group class
		 * 
		 * I holds the propertyName as key and objectVariable as value
		 * 
		 * I used htblValueTypePropObjectVariable because for this type of
		 * queries I have to use optional queries in order to traverse all the
		 * nodes of the objects type because with default graph patterns I will
		 * not be able to get objectValue types and get some properties of this
		 * type
		 */
		Hashtable<String, String> htblValueTypePropObjectVariable = new Hashtable<>();

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
					vairableNum, subjectNum, htblValueTypePropObjectVariable);

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

		// System.out.println(htblSubjectVariables);
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
			String currentClassInstanceUniqueIdentifier, QueryField queryField, int[] vairableNum, int[] subjectNum,
			Hashtable<String, String> htblValueTypePropObjectVariable) {

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

			String subjectVariable = "subject" + subjectNum[0];

			/*
			 * is for building other graph nodes and patterns and add them to
			 * the end of the main graph pattern ( graph pattern with node
			 * representing the subjectClassInstance of the nestedObjectValue )
			 */
			StringBuilder tempBuilder = new StringBuilder();

			/*
			 * get the value classType which is stored in the
			 * prefixedObjectValueClassName of the propertyValue this classType
			 * represent the prefiexedClassName of the value class type and it
			 * was added by RequestValidation class
			 */
			String objectClassTypeName = queryField.getObjectValueTypeClassName();

			/*
			 * Initialize bindPatternTemp to null. This variable will hold bind
			 * pattern temporary to be binded to tempBuilder at the end
			 */
			String bindPatternTemp = null;

			/*
			 * check if the objectValue of the queryField isValueObjectType
			 * which means the objectValue has to be added as an optional query
			 * and I have to save its variable to be used again if the property
			 * is repeated again
			 */
			if (queryField.isValueObjectType()) {

				/*
				 * check if this property is repeated by checking that
				 * propertyName is not added before in
				 * htblValueTypePropObjectVariable
				 */
				if (!htblValueTypePropObjectVariable.containsKey(queryField.getPrefixedPropertyName())) {

					htblValueTypePropObjectVariable.put(queryField.getPrefixedPropertyName(), subjectVariable);

					/*
					 * add property and reference to graph node the represent
					 * the object value node
					 *
					 * eg. SELECT (COUNT(*) as ?isUnique ) WHERE { ?subject0 a
					 * iot-platform:Developer ; foaf:knows ?subject1. ?subject1
					 * a foaf:Person.
					 *
					 * ?subject1 is the object node reference and is linked to
					 * main node (?subject0) with objectProperty (foaf:knows)
					 */
					if (htblClassNameProperty.get(currentClassURI).get(currentClassInstanceUniqueIdentifier)
							.indexOf(queryField) < htblClassNameProperty.get(currentClassURI)
									.get(currentClassInstanceUniqueIdentifier).size() - 1) {
						queryBuilder.append(" ; \n" + queryField.getPrefixedPropertyName() + " ?" + subjectVariable);
					} else {

						/*
						 * it is the last property so we will end the previous
						 * triple pattern with ; and this one with .
						 */
						queryBuilder.append(
								" ; \n" + queryField.getPrefixedPropertyName() + " ?" + subjectVariable + " . \n");

					}

					/*
					 * adding optional query when the property queried has
					 * multiple value types so the optional query act as an if
					 * condition.
					 * 
					 * if value is of type A then get me some properties of
					 * individual of type A
					 */
					tempBuilder.append("OPTIONAL { \n");
					tempBuilder.append("?" + htblValueTypePropObjectVariable.get(queryField.getPrefixedPropertyName())
							+ " a " + "<" + objectClassTypeName + "> ");
					/*
					 * incrementing subjectNumber to have a new subject variable
					 * to be used for binding and then projected. as we don't
					 * need to project the same subjectVariable
					 * 
					 * eg: ?subject0 a <http://xmlns.com/foaf/0.1/Group> ;
					 * foaf:name ?var0 ; foaf:member subject1 ;
					 * iot-platform:description ?var8 .
					 * 
					 * OPTIONAL { subject1 a <http://xmlns.com/foaf/0.1/Person>
					 * ; BIND(subject1 AS subject2 ) ; foaf:userName ?var1 ;
					 * foaf:age ?var2 ; foaf:knows subject3 . }
					 * 
					 * I am incrementing to have subject2 and project subject2
					 * not subject1
					 */
					subjectNum[0]++;
					subjectVariable = "subject" + subjectNum[0];

					/*
					 * set bindPattern String to bind pattern temporary
					 */
					bindPatternTemp = "BIND( ?"
							+ htblValueTypePropObjectVariable.get(queryField.getPrefixedPropertyName()) + " AS ?"
							+ subjectVariable + " ) \n";

					sparqlProjectedFieldsBuilder.append(" ?" + subjectVariable);
					sqlProjectedFieldsBuilder.append(" , " + subjectVariable);
					subjectNum[0]++;

				} else {

					/*
					 * The objectValue is a objectValueType and the property is
					 * a repeated one so it must take the same variable as the
					 * previous one and it will not be linked again to the
					 * subjectVariable
					 * 
					 * 
					 * adding optional query when the property queried has
					 * multiple value types so the optional query act as an if
					 * condition.
					 * 
					 * if value is of type A then get me some properties of
					 * individual of type A
					 * 
					 */
					tempBuilder.append("OPTIONAL { \n");
					tempBuilder.append("?" + htblValueTypePropObjectVariable.get(queryField.getPrefixedPropertyName())
							+ " a " + "<" + objectClassTypeName + ">");

					/*
					 * set bindPattern String to bind pattern temporary
					 */
					bindPatternTemp = "BIND( ?"
							+ htblValueTypePropObjectVariable.get(queryField.getPrefixedPropertyName()) + " AS ?"
							+ subjectVariable + " ) \n";

					sparqlProjectedFieldsBuilder.append(" ?" + subjectVariable);
					sqlProjectedFieldsBuilder.append(" , " + subjectVariable);

					/*
					 * incrementing subjectNum after using it to avoid
					 * subjectVariables to be repeated
					 */
					subjectNum[0]++;

				}

			} else {

				/*
				 * ObjectProperty but does not have value object type (it
				 * targets only one objectValue type (property range))
				 * 
				 * 
				 * add property and reference to graph node the represent the
				 * object value node
				 *
				 * eg. SELECT (COUNT(*) as ?isUnique ) WHERE { ?subject0 a
				 * iot-platform:Developer ; foaf:knows ?subject1. ?subject1 a
				 * foaf:Person.
				 *
				 * ?subject1 is the object node reference and is linked to main
				 * node (?subject0) with objectProperty (foaf:knows)
				 */
				if (htblClassNameProperty.get(currentClassURI).get(currentClassInstanceUniqueIdentifier)
						.indexOf(queryField) < htblClassNameProperty.get(currentClassURI)
								.get(currentClassInstanceUniqueIdentifier).size() - 1) {
					queryBuilder.append(" ; \n" + queryField.getPrefixedPropertyName() + " ?" + subjectVariable);
				} else {

					/*
					 * it is the last property so we will end the previous
					 * triple pattern with ; and this one with .
					 */
					queryBuilder
							.append(" ; \n" + queryField.getPrefixedPropertyName() + " ?" + subjectVariable + " . \n");

				}

				/*
				 * start the new graph pattern that will be added to the end
				 */
				tempBuilder.append("?" + subjectVariable + " a " + "<" + objectClassTypeName + ">");
				sparqlProjectedFieldsBuilder.append(" ?" + subjectVariable);
				sqlProjectedFieldsBuilder.append(" , " + subjectVariable);
				subjectNum[0]++;
			}

			/*
			 * add subjectVarible and it property to
			 * htblSubjectVariablePropertyName to be used to properly construct
			 * query results
			 */
			htblSubjectVariables.put(subjectVariable,
					new QueryVariable(htblIndividualIDSubjVarName.get(currentClassInstanceUniqueIdentifier),
							getPropertyName(queryField.getPrefixedPropertyName()), currentClassURI));

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
			 * check if the queryField's property is multiple value type
			 * property (this is determined by the user input request query body
			 * by adding values field)
			 */
			if (queryField.isValueObjectType()) {

				/*
				 * create a new string builder to hold all the patterns related
				 * to the subjectVariable of the optional query to be added at
				 * the end of the optional query
				 * 
				 * eg: select * where { ... OPTIONAL { ?subject1 a
				 * <http://xmlns.com/foaf/0.1/Person> ; foaf:userName ?var1 ;
				 * foaf:age ?var2 ; foaf:knows ?subject3 . ?subject3 a
				 * <http://iot-platform#Developer> ; foaf:age ?var3 ;
				 * foaf:familyName ?var4 ; foaf:job ?var5 . BIND( ?subject1 AS
				 * ?subject2 ) } ... }
				 * 
				 * we use new StringBuilder and pass it instead of
				 * endGraphPatternBuilder in the recursive call so it hold
				 * ?subject3 variable and all its patterns and at the end of
				 * iteration on subjectVariable of the optional query. I add it
				 * at the end of the optional query (builded using tempBuilder)
				 */
				StringBuilder optionalqueryPattern = new StringBuilder();
				for (QueryField objectPropertyValue : queryFieldList) {

					constructSelectQueryHelper(htblClassNameProperty, htblSubjectVariables, htblIndividualIDSubjVarName,
							tempBuilder, filterConditionsBuilder, optionalqueryPattern, sparqlProjectedFieldsBuilder,
							sqlProjectedFieldsBuilder, objectClassTypeName, objectVaueUniqueIdentifier,
							objectPropertyValue, vairableNum, subjectNum, htblValueTypePropObjectVariable);
				}

				/*
				 * check that optionalqueryPattern is not empty to append it to
				 * tempBuilder as discussed above
				 */
				if (optionalqueryPattern.length() > 0) {
					tempBuilder.append(optionalqueryPattern.toString());
				}

				/*
				 * check if the queryField hold a property that has multiple
				 * value type eg: foaf:member associated with foaf:Group class
				 * and it bindPatternTemp must not be null if the
				 * queryField.isValueObjectType() is true it is secondary check
				 * but it will always be true if the first condition is true
				 */
				if (bindPatternTemp != null) {
					/*
					 * append bindPatternTemp tp tempBuilder, I do this because
					 * I need to have the bind pattern at the end of the
					 * optional query part
					 */
					tempBuilder.append(bindPatternTemp);

					/*
					 * end the optional query part
					 */
					tempBuilder.append(" } \n");
				}
			} else {

				/*
				 * The queryField's property is not multiple value type so I
				 * will not need to create a new StringBuilder to pass it
				 * instead of endGraphPatternBuilder in the recursive call
				 * because any related patterns has to be added to the default
				 * patterns part
				 * 
				 * eg: Select * where{ .. ?subject1 a foaf:Person; foaf:knows
				 * ?subject2 . ?subject2 a foaf:Person; foaf:userName ?userName.
				 * ....}
				 * 
				 * ?subject2 patterns is added to default patterns because it is
				 * not an optional patterns
				 */

				for (QueryField objectPropertyValue : queryFieldList) {

					constructSelectQueryHelper(htblClassNameProperty, htblSubjectVariables, htblIndividualIDSubjVarName,
							tempBuilder, filterConditionsBuilder, endGraphPatternBuilder, sparqlProjectedFieldsBuilder,
							sqlProjectedFieldsBuilder, objectClassTypeName, objectVaueUniqueIdentifier,
							objectPropertyValue, vairableNum, subjectNum, htblValueTypePropObjectVariable);
				}
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

		} else

		{

			/*
			 * The property is not an objectProperty it is property that has a
			 * literal value (the value can be datatype value (eg.
			 * string,int,float) or a reference to an existed object instance
			 */
			if (htblClassNameProperty.get(currentClassURI).get(currentClassInstanceUniqueIdentifier)
					.indexOf(queryField) < htblClassNameProperty.get(currentClassURI)
							.get(currentClassInstanceUniqueIdentifier).size() - 1) {

				/*
				 * if we add a new graph pattern (by checking if the
				 * prefixedPropertyName was not added before for this current
				 * instance) then increment the variableNum[0]
				 *
				 * add prifixedPropertyName to htblPropValue to avoid
				 * duplicating it again for this instance
				 *
				 * I do this after adding filter condition and graph pattern to
				 * maintain the same variableNames between graph pattern added
				 * and condtion
				 */

				sparqlProjectedFieldsBuilder.append("  ?var" + vairableNum[0]);
				sqlProjectedFieldsBuilder.append(" , var" + vairableNum[0]);

				/*
				 * add var variable and it property to
				 * htblSubjectVariablePropertyName to be used to properly
				 * construct query results
				 */
				htblSubjectVariables.put("var" + vairableNum[0],
						new QueryVariable(htblIndividualIDSubjVarName.get(currentClassInstanceUniqueIdentifier),
								getPropertyName(queryField.getPrefixedPropertyName()), currentClassURI));

				/*
				 * it is not the last property so we will end the previous
				 * triple pattern with ;
				 */
				queryBuilder.append(" ; \n" + queryField.getPrefixedPropertyName() + " " + "?var" + vairableNum[0]);
				vairableNum[0]++;

			} else {

				/*
				 * if we add a new graph pattern (by checking if the
				 * prefixedPropertyName was not added before for this current
				 * instance) then increment the variableNum[0]
				 *
				 * add prifixedPropertyName to htblPropValue to avoid
				 * duplicating it again for this instance
				 *
				 * I do this after adding filter condition and graph pattern to
				 * maintain the same variableNames between graph pattern added
				 * and condtion
				 */

				sparqlProjectedFieldsBuilder.append("  ?var" + vairableNum[0]);
				sqlProjectedFieldsBuilder.append(" , var" + vairableNum[0]);

				/*
				 * add var variable and it property to
				 * htblSubjectVariablePropertyName to be used to properly
				 * construct query results
				 */
				htblSubjectVariables.put("var" + vairableNum[0],
						new QueryVariable(htblIndividualIDSubjVarName.get(currentClassInstanceUniqueIdentifier),
								getPropertyName(queryField.getPrefixedPropertyName()), currentClassURI));

				/*
				 * it is the last property so we will end the previous triple
				 * pattern with ; and this one with .
				 */

				queryBuilder.append(
						" ; \n" + queryField.getPrefixedPropertyName() + " " + "?var" + vairableNum[0] + " . \n");
				vairableNum[0]++;

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
