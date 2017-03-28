package com.iotplatform.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.jena.update.UpdateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.XSDDataTypes;
import com.iotplatform.utilities.PropertyValue;
import com.iotplatform.utilities.QueryField;
import com.iotplatform.utilities.QueryUtility;
import com.iotplatform.utilities.SelectionUtility;

import oracle.spatial.rdf.client.jena.ModelOracleSem;
import oracle.spatial.rdf.client.jena.Oracle;

/*
 * MainDao is used to insert triples to application model
 */

@Repository("mainDao")
public class MainDao {

	private Oracle oracle;
	private static String prefixesString = null;
	private SelectionUtility selectionUtility;

	@Autowired
	public MainDao(Oracle oracle, SelectionUtility selectionUtility) {
		this.oracle = oracle;
		this.selectionUtility = selectionUtility;
	}

	/*
	 * insertData method insert new triples to passed application model
	 */
	public void insertData(String applicationModelName, String requestSubjectClassName,
			Hashtable<Class, ArrayList<ArrayList<PropertyValue>>> htblClassPropertyValue) {
		try {

			StringBuilder insertQueryBuilder = new StringBuilder();

			if (prefixesString == null) {
				StringBuilder prefixStringBuilder = new StringBuilder();
				for (Prefixes prefix : Prefixes.values()) {
					prefixStringBuilder.append("PREFIX	" + prefix.getPrefix() + "	<" + prefix.getUri() + ">\n");
				}

				prefixesString = prefixStringBuilder.toString();
			}

			insertQueryBuilder.append(prefixesString);
			insertQueryBuilder.append("INSERT DATA { \n");

			/*
			 * call constructInsertQuery method that return the constructed
			 * Triples
			 */
			String constructedTriples = constructInsertQuery(htblClassPropertyValue);

			/*
			 * append constructedTriples to queryBuilder
			 */
			insertQueryBuilder.append(constructedTriples);

			/*
			 * close insert query
			 */
			insertQueryBuilder.append("}");

			System.out.println(insertQueryBuilder.toString());
			/*
			 * execute query
			 */
			ModelOracleSem model = ModelOracleSem.createOracleSemModel(oracle, applicationModelName);
			UpdateAction.parseExecute(insertQueryBuilder.toString(), model);
			model.close();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), requestSubjectClassName);
		}

	}

	/*
	 * constructInsertQuery method construct insert query to insert new triples
	 * to given applicationName model
	 * 
	 * It take applicationName to get applicationModel name and it takes
	 * htblClassPropertyValue which is the prefixedPropertyValues of every new
	 * Instance of an ontologyClass (htblClassPropertyValue constructed in
	 * RequestValidation class)
	 */
	private String constructInsertQuery(Hashtable<Class, ArrayList<ArrayList<PropertyValue>>> htblClassPropertyValue) {

		StringBuilder insertQueryBuilder = new StringBuilder();

		/*
		 * iterate on htblClassPropertyValue
		 */
		Iterator<Class> htblClassPropertyValueIterator = htblClassPropertyValue.keySet().iterator();
		while (htblClassPropertyValueIterator.hasNext()) {
			Class subjectClass = htblClassPropertyValueIterator.next();

			/*
			 * get uniqueIdentifierPrefixedPropertyName to get the
			 * subjectUniqueIdentifer of this subjectClass
			 */
			Property uniqueIdentifierProperty;
			if (subjectClass.isHasUniqueIdentifierProperty()) {
				uniqueIdentifierProperty = subjectClass.getProperties()
						.get(subjectClass.getUniqueIdentifierPropertyName());

			} else {
				uniqueIdentifierProperty = subjectClass.getProperties().get("id");
			}

			/*
			 * Iterate on instances of type subjectClass
			 */
			for (int i = 0; i < htblClassPropertyValue.get(subjectClass).size(); i++) {
				ArrayList<PropertyValue> instancePropertyValueList = htblClassPropertyValue.get(subjectClass).get(i);
				String instanceTriples = constructClassInstanceTriples(subjectClass, uniqueIdentifierProperty,
						instancePropertyValueList);
				insertQueryBuilder.append(instanceTriples);
			}

		}

		return insertQueryBuilder.toString();
	}

	/*
	 * constructClassInstanceTriples method constructs triples of an instance of
	 * type subjectClass
	 */
	private static String constructClassInstanceTriples(Class subjectClass, Property uniqueIdentifierProperty,
			ArrayList<PropertyValue> instancePropertyValueList) {

		/*
		 * triplesBuilder is used to build instance triples
		 */
		StringBuilder triplesBuilder = new StringBuilder();

		/*
		 * tempBuilder is a tempBuilder to hold triples.
		 * 
		 * I used this String builder because the subjectUniqueIdentifier is the
		 * value of uniqueIdentifierPrefixedPropertyName and to construct right
		 * triples it must begin with the subjectUniqueIdentifier so tempBuilder
		 * will hold all the triples then at the end I will append it to
		 * triplesBuilder that will have the subjectUniqueIdentifier
		 */
		StringBuilder tempBuilder = new StringBuilder();

		String subjectUniqueIdentifier = "";

		/*
		 * check if the last propertyValue is the uniqueIdentifierPropertyValue
		 */
		int size = instancePropertyValueList.size();

		/*
		 * iterate over instancePropertyValueList to construct triples
		 */
		int count = 0;
		String uniqueIdentifierPrefixedPropertyName = uniqueIdentifierProperty.getPrefix().getPrefix()
				+ uniqueIdentifierProperty.getName();

		for (PropertyValue propertyValue : instancePropertyValueList) {

			/*
			 * finding subjectUniqueIdentifier
			 */
			if (propertyValue.getPropertyName().equals(uniqueIdentifierPrefixedPropertyName)) {
				subjectUniqueIdentifier = propertyValue.getValue().toString().toLowerCase().replace(" ", "");
				propertyValue.setValue(getValue(uniqueIdentifierProperty, propertyValue.getValue()));
			}

			/*
			 * checking if the propertyValue is the last one to end the
			 * statement with .
			 */
			if (count == size - 1) {
				tempBuilder.append(
						propertyValue.getPropertyName() + "  " + propertyValue.getValue().toString() + "  . \n");
			} else {
				tempBuilder.append(
						propertyValue.getPropertyName() + "  " + propertyValue.getValue().toString() + "  ; \n");
			}

			count++;
		}

		/*
		 * add subjectUniqueIdentifier and add triple that tells that
		 * subjectUniqueIdentifier is of type subjectClass
		 */
		triplesBuilder.append(Prefixes.IOT_PLATFORM.getPrefix() + subjectUniqueIdentifier + "  a  "
				+ subjectClass.getPrefix().getPrefix() + subjectClass.getName() + "  ; \n");

		/*
		 * get all superClasses of subjectClass to identify that the new
		 * instance is also an instance of all superClasses of subjectClass
		 */

		for (Class superClass : subjectClass.getSuperClassesList()) {
			triplesBuilder.append("  a  " + superClass.getPrefix().getPrefix() + superClass.getName() + "  ; \n");

		}

		/*
		 * add rest of triples by appending tempBuilder
		 */

		triplesBuilder.append(tempBuilder.toString());

		return triplesBuilder.toString();

	}

	/*
	 * getValue method returns the appropriate value by appending a prefix
	 */
	private static Object getValue(Property property, Object value) {

		if (property instanceof DataTypeProperty) {
			XSDDataTypes xsdDataType = ((DataTypeProperty) property).getDataType();
			value = "\"" + value.toString() + "\"" + xsdDataType.getXsdType();
			return value;
		} else {
			return Prefixes.IOT_PLATFORM.getPrefix() + value.toString().toLowerCase().replaceAll(" ", "");
		}
	}

	public List<Hashtable<String, Object>> queryData(
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<QueryField>>> htblClassNameProperty,
			String applicationName, String requestSubjectClassName) {

		return null;
	}

	public static String constructSelectQuery(
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<QueryField>>> htblClassNameProperty,
			String applicationName) {

		Iterator<String> htblClassNamePropertyIterator = htblClassNameProperty.keySet().iterator();

		/*
		 * get first prefixedClassName which is the prefixedName of the passed
		 * request className because LinkedHashMap keep the order of the
		 * insertion unchanged and GetQueryRequestValiation insert the
		 * requestClassPrefixName firstly
		 */
		String prefixedClassName = htblClassNamePropertyIterator.next();
		String mainInstanceUniqueIdentifier = htblClassNameProperty.get(prefixedClassName).keySet().iterator().next();

		return constructUniqueContstraintCheckSubQueryStr3(htblClassNameProperty, prefixedClassName,
				mainInstanceUniqueIdentifier);

	}

	public static String constructUniqueContstraintCheckSubQueryStr3(
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<QueryField>>> htblClassNameProperty,
			String mainClassPrefixedName, String mainInstanceUniqueIdentifier) {

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
		 * start of the subQuery
		 */
		queryBuilder.append("{ SELECT (COUNT(*) as ?isUnique ) WHERE { \n");

		/*
		 * start of the query graph patterns (this triple pattern minimize
		 * search area because of specifing that the first subject variable
		 * (?subject0) is of type certin class )
		 */
		queryBuilder.append("?subject0" + " a " + mainClassPrefixedName);

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

			helper(htblClassNameProperty, queryBuilder, filterConditionsBuilder, endGraphPatternBuilder,
					mainClassPrefixedName, mainInstanceUniqueIdentifier, queryField, htblPropValue, vairableNum,
					subjectNum);

		}

		/*
		 * Appending the endGraphPatternBuilder to the end of the queryBuilder
		 */
		queryBuilder.append(endGraphPatternBuilder.toString());

		// filterConditionsBuilder.append(" )");

		/*
		 * complete end of the subquery structure
		 */
		// queryBuilder.append(" " + filterConditionsBuilder.toString() + " \n
		// }}");

		return queryBuilder.toString();
	}

	/*
	 * A recursive method that construct the uniqueConstraintCheck query
	 *
	 * it takes the reference of
	 * htblUniquePropValueList,queryBuilder,filterConditionsBuilder and
	 * endGraphPatternBuilder to construct query
	 *
	 * It also recursively take propertyValue and currentClassPrefixedName to
	 * breakdown all values and construct a proper graph patterns and filter in
	 * the query
	 *
	 */
	public static void helper(LinkedHashMap<String, LinkedHashMap<String, ArrayList<QueryField>>> htblClassNameProperty,
			StringBuilder queryBuilder, StringBuilder filterConditionsBuilder, StringBuilder endGraphPatternBuilder,
			String currentClassPrefixedName, String currentClassInstanceUniqueIdentifier, QueryField queryField,
			Hashtable<String, Object> htblPropValue, int[] vairableNum, int[] subjectNum) {

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

			if (htblClassNameProperty.get(currentClassPrefixedName).get(currentClassInstanceUniqueIdentifier)
					.indexOf(queryField) < htblClassNameProperty.get(currentClassPrefixedName)
							.get(currentClassInstanceUniqueIdentifier).size() - 1) {
				queryBuilder.append(" ; \n" + queryField.getPrefixedPropertyName() + "?subject" + subjectNum[0]);
			} else {
				/*
				 * it is the last property so we will end the previous triple
				 * pattern with ; and this one with .
				 */
				queryBuilder
						.append(" ; \n" + queryField.getPrefixedPropertyName() + " ?subject" + subjectNum[0] + " . \n");

			}

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
			tempBuilder.append("?subject" + subjectNum[0] + " a " + objectClassTypeName);
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

				helper(htblClassNameProperty, tempBuilder, filterConditionsBuilder, endGraphPatternBuilder,
						objectClassTypeName, objectVaueUniqueIdentifier, objectPropertyValue, objectValueHtblPropValue,
						vairableNum, subjectNum);
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
			if (htblClassNameProperty.get(currentClassPrefixedName).get(currentClassInstanceUniqueIdentifier)
					.indexOf(queryField) < htblClassNameProperty.get(currentClassPrefixedName)
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

	public List<Hashtable<String, Object>> selectAll(String applicationModelName, Class subjectClass) {

		String applicationName = applicationModelName.replaceAll(" ", "").toUpperCase().substring(0,
				applicationModelName.length() - 6);

		String queryString = QueryUtility.constructSelectAllQueryNoFilters(subjectClass, applicationModelName);
		List<Hashtable<String, Object>> subjectClassIndividualsList = new ArrayList<>();

		try {
			ResultSet results = oracle.executeQuery(queryString, 0, 1);

			/*
			 * call constractResponeJsonObjectForListSelection method in
			 * selectionUtility class to construct the response json
			 */

			subjectClassIndividualsList = selectionUtility.constractResponeJsonObjectForListSelection(applicationName,
					results, subjectClass);

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), subjectClass.getName());
		}

		return subjectClassIndividualsList;
	}

}
