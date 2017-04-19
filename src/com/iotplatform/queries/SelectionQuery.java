package com.iotplatform.queries;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.mapers.OntologyMapper;
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
		System.out.println(htblClassNameProperty);
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
		 * filterBuilder is used to build filter part of the query
		 */
		StringBuilder filterBuilder = new StringBuilder();

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
		 * htbloptionalTypeClassList holds subjectVariableName of objectValue of
		 * property that has different types as key and the value is a list of
		 * classTypes specified in the values part of the selectQueryRequest
		 */
		Hashtable<String, ArrayList<String>> htbloptionalTypeClassList = new Hashtable<>();

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
					sqlProjectedFieldsBuilder, filterBuilder, mainClassPrefixedName, mainInstanceUniqueIdentifier,
					queryField, vairableNum, subjectNum, htblValueTypePropObjectVariable, htbloptionalTypeClassList);

		}

		/*
		 * unProjectOptionalPartBuilder is used to build the final optional part
		 * that gets the other object values of a given property that were not
		 * specified in the values field by the user
		 * 
		 * eg. The user specifed the values part of foaf:member of class
		 * foaf:Group . In the values part the user specified foaf:Person and
		 * foaf:organization only . foaf:member has valueObjectType foaf:Agent
		 * so it can have an objectValue of type foaf:Group (because foaf:Group
		 * is subclass of foaf:Agnet) . This builder will build the optional
		 * query part that gets the other values subject and their classType
		 *
		 * This capability is enabled by default and the user can disable it in
		 * the request option part
		 */
		StringBuilder unProjectOptionalPartBuilder = new StringBuilder();

		/*
		 * htblfilterClassesURI is used to avoid replicating classURIs in the
		 * filter condition of unProjectOptionalPart.
		 * 
		 * eg. Replicating happens in the same discussed example of foaf:member
		 * property. When the user add foaf:Person and foaf:Orgnaization to
		 * values field, foaf:Agent which is the superClass of both is
		 * replicated in the filter condition, so this relpication will be
		 * avoided by using this hashtable that will hold classUri as key so not
		 * duplicates will be added
		 */
		Hashtable<String, String> htblfilterClassesURI = new Hashtable<>();

		/*
		 * check if htbloptionalTypeClassList has keyValues
		 */
		if (htbloptionalTypeClassList.size() > 0) {
			/*
			 * iterate over htbloptionalTypeClassList and for each key add a new
			 * optionalQuery (each key represent and objectValueQueryVariable
			 * for a property. The property is different and also the
			 * objectValueQueryVariable )
			 */
			Iterator<String> htbloptionalTypeClassListIter = htbloptionalTypeClassList.keySet().iterator();

			int classVariableCounter = 0;
			while (htbloptionalTypeClassListIter.hasNext()) {
				String objectValueQueryVariable = htbloptionalTypeClassListIter.next();

				/*
				 * The first item in the list is propertyName of
				 * objectValueQueryVariable
				 */
				String propertyName = htbloptionalTypeClassList.get(objectValueQueryVariable).get(0);

				/*
				 * The second item in the list is subjectClassURI of
				 * objectValueQueryVariable
				 */
				String subjectClassURI = htbloptionalTypeClassList.get(objectValueQueryVariable).get(1);

				/*
				 * The third item in the list is the subjectClassQueryVariable
				 * of objectValueQueryVariable
				 */
				String subjectClassQueryVariable = htbloptionalTypeClassList.get(objectValueQueryVariable).get(2);

				/*
				 * this fourth item in htbloptionalTypeClassList is the range of
				 * the property. I add it to get all its subClassTypes in order
				 * to avoid adding optional query if the user specified all the
				 * types in values field in the request body
				 */
				String propertyRangeClassUri = htbloptionalTypeClassList.get(objectValueQueryVariable).get(3);

				/*
				 * get number of type classes of the property associated with
				 * objectValueQueryVariable and add 1 for propertyRangeClassUri
				 * itself (because it is also a type)
				 */
				int classTypesNum = OntologyMapper.getHtblMainOntologyClassesUriMappers().get(propertyRangeClassUri)
						.getClassTypesList().size() + 1;

				/*
				 * typeCounter is used to count number of types used. I
				 * increment this counter every time I add a condition to filter
				 * part of this optional query
				 * 
				 * At the end if typeCounter == classTypesNum this means that
				 * all the type classes were mentioned by user so no need to add
				 * this optional query
				 */
				int typeCounter = 0;

				/*
				 * optionalQueryTempBuilder is used to build optional query for
				 * this objectValueQueryVariable
				 */
				StringBuilder optionalQueryTempBuilder = new StringBuilder();

				/*
				 * Start unProjectOptionalPartBuilder
				 */
				optionalQueryTempBuilder
						.append("OPTIONAL { " + objectValueQueryVariable + " a  ?class" + classVariableCounter + " \n");
				optionalQueryTempBuilder.append("FILTER ( ");

				/*
				 * add filterPart
				 */
				ArrayList<String> optionalTypeClassList = htbloptionalTypeClassList.get(objectValueQueryVariable);

				/*
				 * add propertyRangeClassUri to filter part
				 */
				optionalQueryTempBuilder
						.append(" ?class" + classVariableCounter + " != <" + propertyRangeClassUri + ">  ");

				/*
				 * add classUri to htblfilterClassesURI to avoid replicating it
				 * again in the filter condtion
				 */
				htblfilterClassesURI.put(propertyRangeClassUri, propertyRangeClassUri);

				typeCounter++;

				/*
				 * iterating over optionalTypeClassList
				 * 
				 * The loop start from 4 because I manually get and use the
				 * first 4 elements in the list. so I will skip them here
				 */
				for (int i = 4; i < htbloptionalTypeClassList.get(objectValueQueryVariable).size(); i++) {

					String objectValueClassURI = optionalTypeClassList.get(i);
					optionalQueryTempBuilder
							.append(" && ?class" + classVariableCounter + " != <" + objectValueClassURI + ">  ");

					/*
					 * add classUri to htblfilterClassesURI to avoid replicating
					 * it again in the filter condtion
					 */
					htblfilterClassesURI.put(objectValueClassURI, objectValueClassURI);

					typeCounter++;

					/*
					 * add to unProjectOptionalPartBuilder all the subClasses of
					 * objectValueClass if it has because every subClass
					 * instance is also instance of superClass and the triple
					 * that indicates this is inserted in the database
					 * 
					 * so I don't want to replicate data as I want to get only
					 * the classType instances that were not added by the user
					 * in the values list
					 * 
					 * eg. if the user added foaf:Person as a classType in the
					 * values list of foaf:member property (foaf:member property
					 * has range foaf:Agent which is the superClass of
					 * foaf:Person so foaf:member can has an foaf:Person
					 * instance), foaf:Person has iot-platorm:Developer as its
					 * subClass so when the user add foaf:Person to value list
					 * the query performed will get also and foaf:Developer
					 * instance . So here I have to ensure the no instances of
					 * foaf:Person or its subClasses got by this optionalQuery
					 * to avoid replication of results and enhance query
					 * performance
					 */

					/*
					 * get class of the objectValueClassURI
					 */
					Class objectValueClass = OntologyMapper.getHtblMainOntologyClassesUriMappers()
							.get(objectValueClassURI);

					/*
					 * check if it has subClasses in order to loop on them and
					 * add them to unProjectOptionalPartBuilder
					 */
					if (objectValueClass.isHasTypeClasses()) {

						Hashtable<String, Class> htblSubClasses = objectValueClass.getClassTypesList();
						Iterator<String> htblSubClassesIter = htblSubClasses.keySet().iterator();

						while (htblSubClassesIter.hasNext()) {

							String subClassName = htblSubClassesIter.next();
							Class subClass = htblSubClasses.get(subClassName);

							/*
							 * check that classUri was not added before
							 */
							if (!htblfilterClassesURI.containsKey(subClass.getUri())) {

								optionalQueryTempBuilder.append(
										" && ?class" + classVariableCounter + " != <" + subClass.getUri() + ">  ");

								/*
								 * add classUri to htblfilterClassesURI to avoid
								 * replicating it again in the filter condtion
								 */
								htblfilterClassesURI.put(subClass.getUri(), subClass.getUri());

								typeCounter++;
							}
						}

					}

					/*
					 * check if it has subClasses in order to loop on them and
					 * add them to unProjectOptionalPartBuilder
					 */
					if (objectValueClass.getSuperClassesList().size() != 0) {

						for (Class superClass : objectValueClass.getSuperClassesList()) {

							/*
							 * check that classUri was not added before
							 */
							if (!htblfilterClassesURI.containsKey(superClass.getUri())) {

								optionalQueryTempBuilder.append(
										" && ?class" + classVariableCounter + " != <" + superClass.getUri() + ">  ");

								/*
								 * add classUri to htblfilterClassesURI to avoid
								 * replicating it again in the filter condtion
								 */
								htblfilterClassesURI.put(superClass.getUri(), superClass.getUri());

							}
						}

					}

				}

				/*
				 * check if typeCounter == classTypesNum this means that all the
				 * type classes were mentioned by user so no need to add this
				 * optional query so I will not complete this iteration
				 * 
				 * if they are not equal so i will append
				 * optionalQueryTempBuilder to unProjectOptionalPartBuilder
				 * (this builder hold all the optional queries of this type)
				 */
				if (typeCounter == classTypesNum) {
					continue;
				} else {
					unProjectOptionalPartBuilder.append(optionalQueryTempBuilder.toString());
				}

				/*
				 * End filter part of unProjectOptionalPartBuilder
				 */
				unProjectOptionalPartBuilder.append(" ) \n");

				/*
				 * add bind part of unProjectOptionalPartBuilder to add the
				 * aliases to the projecting fields
				 */
				unProjectOptionalPartBuilder
						.append("BIND ( " + objectValueQueryVariable + " AS ?object" + classVariableCounter + " ) ");
				unProjectOptionalPartBuilder.append(
						"BIND ( ?class" + classVariableCounter + " AS ?objectType" + classVariableCounter + " ) ");

				/*
				 * add queryVariable for new projected variable (?object and
				 * ?objectType)
				 */
				htblSubjectVariables.put("object" + classVariableCounter,
						new QueryVariable(subjectClassQueryVariable, propertyName, subjectClassURI));
				htblSubjectVariables.put("objecttype" + classVariableCounter,
						new QueryVariable("object" + classVariableCounter, "type", null));

				/*
				 * add bind aliases to sparqlProjectedFieldsBuilder
				 * 
				 */
				sparqlProjectedFieldsBuilder.append(" ?object" + classVariableCounter);
				sparqlProjectedFieldsBuilder.append(" ?objectType" + classVariableCounter);

				/*
				 * add bind aliases to sqlProjectedFieldsBuilder
				 */
				sqlProjectedFieldsBuilder.append(" , object" + classVariableCounter);
				sqlProjectedFieldsBuilder.append(" , objectType" + classVariableCounter);

				/*
				 * add bound filter condition to filterBuilder
				 */
				filterBuilder.append(" || BOUND ( ?object" + classVariableCounter + " ) ");

				/*
				 * end optional query part of unProjectOptionalPartBuilder
				 */
				unProjectOptionalPartBuilder.append(" } \n");

				/*
				 * increment classVariableCounter to have different variable
				 * name
				 */
				classVariableCounter++;
			}
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

		if (filterBuilder.length() > 0) {
			/*
			 * start filterBuilder
			 */
			filterBuilder.insert(0, "FILTER ( ");

			/*
			 * end filterBuilder
			 */
			filterBuilder.append(" ) \n ");

		}
		/*
		 * Appending the endGraphPatternBuilder to the end of the queryBuilder
		 */
		queryBuilder.append(endGraphPatternBuilder.toString());

		/*
		 * append unProjectOptionalPartBuilder to queryBuilder
		 */
		queryBuilder.append(unProjectOptionalPartBuilder);

		/*
		 * Append filterBuilder to query builder. It must be appended after
		 * appending endGraphPatternBuilder (which holds graph patterns)
		 */
		queryBuilder.append(filterBuilder.toString());

		/*
		 * complete query by appending projection field and graph patterns
		 */
		StringBuilder mainBuilder = new StringBuilder();

		mainBuilder.append("SELECT " + sqlProjectedFieldsBuilder.toString() + "\n FROM TABLE( SEM_MATCH ( ' SELECT "
				+ sparqlProjectedFieldsBuilder.toString() + " \n WHERE { \n " + queryBuilder.toString()
				+ " }' , \n sem_models('" + applicationModelName + "'),null, \n SEM_ALIASES("
				+ prefixStringBuilder.toString() + "),null))");

		Object[] returnObject = { mainBuilder.toString(), htblSubjectVariables };
		return returnObject;
	}

	/*
	 * A recursive method that construct a select query
	 *
	 * it takes the reference of
	 * htblClassNameProperty,queryBuilder,filterConditionsBuilder,filterBuilder,
	 * unProjectOptionalPartBuilder and endGraphPatternBuilder to construct
	 * query
	 * 
	 * currentClassURI holds the classURI of the subjectClassURI,
	 *
	 * currentClassInstanceUniqueIdentifier holds the uniqueIdentifer of the
	 * current instance of subjectClassURI (for a subjectClassURI a user can
	 * mention different instances with different patterns needed)
	 * 
	 * queryField is an instance of QueryFields which is created by the
	 * selectQueryValidation after parsing and validating the query request body
	 * 
	 * To know what each other parameter is see the first part of
	 * constructSelectQuery method where I initialized and document all the
	 * parameters of this method and then call it
	 */
	private static void constructSelectQueryHelper(
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<QueryField>>> htblClassNameProperty,
			Hashtable<String, QueryVariable> htblSubjectVariables,
			Hashtable<String, String> htblIndividualIDSubjVarName, StringBuilder queryBuilder,
			StringBuilder filterConditionsBuilder, StringBuilder endGraphPatternBuilder,
			StringBuilder sparqlProjectedFieldsBuilder, StringBuilder sqlProjectedFieldsBuilder,
			StringBuilder filterBuilder, String currentClassURI, String currentClassInstanceUniqueIdentifier,
			QueryField queryField, int[] vairableNum, int[] subjectNum,
			Hashtable<String, String> htblValueTypePropObjectVariable,
			Hashtable<String, ArrayList<String>> htblOptionalTypeClassList) {

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
					 * not subject1 because when it the variable is not in
					 * htblValueTypePropObjectVariable it means that it is the
					 * first time to see this property so I have to use two
					 * subjectVariable the first one to do the relation pattern
					 * as discussed above and then incrementing to use another
					 * variable when adding binding part
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

					/*
					 * incrementing subjectNum after using it to avoid
					 * subjectVariables to be repeated
					 */
					subjectNum[0]++;

					/*
					 * add alisaed subjectVariable to filterBuilder in order to
					 * make sure that it is bounded(has value to avoid returning
					 * null values)
					 */
					if (filterBuilder.length() == 0) {
						/*
						 * first filter condition so do not add or logic
						 * operator ( || )
						 */
						filterBuilder.append("BOUND ( ?" + subjectVariable + " )");
					} else {
						/*
						 * not the first filter condition so add or logic
						 * operator ( || )
						 */
						filterBuilder.append(" || BOUND ( ?" + subjectVariable + " )");
					}

					/*
					 * create a new optionalTypeClassList to hold classTypes
					 * because it is the first time to this property
					 */
					ArrayList<String> optionalTypeClassList = new ArrayList<>();
					htblOptionalTypeClassList.put(
							"?" + htblValueTypePropObjectVariable.get(queryField.getPrefixedPropertyName()),
							optionalTypeClassList);

					/*
					 * add propertyName as first element in the list,
					 * subjectClassURI as second element,
					 * subjectClassQueryVariable as the third element in the
					 * list and range classType of property as the fourth
					 * element in the list
					 * 
					 * I am adding the above discussed elements in this order
					 * because I will need them when adding optionalQuery that
					 * get all the missing values with types not specified by
					 * the user
					 * 
					 * For this results to be represented using
					 * SelectionQueryResult class, I have to create a
					 * queryVariable instance for the variables in the optional
					 * query, and to do so I must have the above elements
					 * 
					 * See the last part in constructSelectQuery method in the
					 * part of creating the described optionalQuery to know how
					 * I get this elements and use them
					 */
					String propName = getPropertyName(queryField.getPrefixedPropertyName());
					Class subjectClass = OntologyMapper.getHtblMainOntologyClassesUriMappers().get(currentClassURI);

					/*
					 * add propertyName as first element
					 */
					optionalTypeClassList.add(propName);

					/*
					 * add subjectClassURI as second element
					 */
					optionalTypeClassList.add(currentClassURI);

					/*
					 * add subjectClassQueryVariable as third element
					 */

					optionalTypeClassList.add(htblIndividualIDSubjVarName.get(currentClassInstanceUniqueIdentifier));

					/*
					 * add range classType of property as the third element in
					 * the list
					 */
					optionalTypeClassList
							.add(((ObjectProperty) subjectClass.getProperties().get(propName)).getObject().getUri());

					/*
					 * add objectValueTypeClassName to optionalTypeClassList
					 */
					optionalTypeClassList.add(queryField.getObjectValueTypeClassName());

				} else {

					/*
					 * The objectValue is a objectValueType and the property is
					 * a repeated one so it must take the same variable as the
					 * previous one and it will not be linked again to the
					 * subjectVariable
					 * 
					 * I have to end the previous query pattern part by adding a
					 * dot ( . ) if it is the last one
					 */

					if (htblClassNameProperty.get(currentClassURI).get(currentClassInstanceUniqueIdentifier)
							.indexOf(queryField) == htblClassNameProperty.get(currentClassURI)
									.get(currentClassInstanceUniqueIdentifier).size() - 1) {

						/*
						 * it is the last property so we will end the previous
						 * triple pattern with ; and this one with .
						 */
						queryBuilder.append(" . \n");

					}

					/*
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

					/*
					 * add alisaed subjectVariable to filterBuilder in order to
					 * make sure that it is bounded(has value to avoid returning
					 * null values)
					 */
					if (filterBuilder.length() == 0) {
						/*
						 * first filter condition so do not add or logic
						 * operator ( || )
						 */
						filterBuilder.append("BOUND ( ?" + subjectVariable + " )");
					} else {
						/*
						 * not the first filter condition so add or logic
						 * operator ( || )
						 */
						filterBuilder.append(" || BOUND ( ?" + subjectVariable + " )");
					}

					/*
					 * add objectValueTypeClassName to optionalTypeClassList
					 * only (without creating a new list or checking if the
					 * htblOptionalTypeClassList has the
					 * propertyObjectQueryVariable) because it is not the first
					 * time to see this property because it was added before to
					 * htblValueTypePropObjectVariable
					 */
					htblOptionalTypeClassList
							.get("?" + htblValueTypePropObjectVariable.get(queryField.getPrefixedPropertyName()))
							.add(queryField.getObjectValueTypeClassName());

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
							sqlProjectedFieldsBuilder, filterBuilder, objectClassTypeName, objectVaueUniqueIdentifier,
							objectPropertyValue, vairableNum, subjectNum, htblValueTypePropObjectVariable,
							htblOptionalTypeClassList);
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
				 * and it bindPatternTemp(which holds the binding part of
				 * optional query) must not be null if the
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
				 * So I will pass endGraphPatternBuilder to the recursive call
				 * 
				 * eg: Select * where{ .. ?subject1 a foaf:Person; foaf:knows
				 * ?subject2 . ?subject2 a foaf:Person; foaf:userName ?userName.
				 * ....}
				 * 
				 * ?subject2 patterns is added to default patterns because it is
				 * not an optional patterns
				 */
				StringBuilder nestedQueryPattern = new StringBuilder();
				for (QueryField objectPropertyValue : queryFieldList) {

					constructSelectQueryHelper(htblClassNameProperty, htblSubjectVariables, htblIndividualIDSubjVarName,
							tempBuilder, filterConditionsBuilder, nestedQueryPattern, sparqlProjectedFieldsBuilder,
							sqlProjectedFieldsBuilder, filterBuilder, objectClassTypeName, objectVaueUniqueIdentifier,
							objectPropertyValue, vairableNum, subjectNum, htblValueTypePropObjectVariable,
							htblOptionalTypeClassList);
				}

				/*
				 * check that nestedQueryPattern is not empty to append it to
				 * tempBuilder as discussed above
				 */
				if (nestedQueryPattern.length() > 0) {
					tempBuilder.append(nestedQueryPattern.toString());
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
