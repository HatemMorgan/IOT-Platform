package com.iotplatform.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.exceptions.InvalidPropertyValuesException;
import com.iotplatform.exceptions.UniqueConstraintViolationException;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.classes.Application;
import com.iotplatform.ontology.classes.Person;
import com.iotplatform.utilities.PropertyValue;
import com.iotplatform.utilities.ValueOfTypeClass;

import oracle.spatial.rdf.client.jena.Oracle;

@Component
public class ValidationDao {
	private Oracle oracle;
	private final String suffix = "_MODEL";

	@Autowired
	public ValidationDao(Oracle oracle) {
		this.oracle = oracle;
	}

	/*
	 * checkIfInstanceExsist used to query the passed application model to check
	 * if there are instances of a specified classes passed to it . This
	 * validation is done to make sure that no object property has a value which
	 * is not available to maintain the consistency and integrity of the data.
	 * 
	 * It also check if there is no unique constraint violation
	 * 
	 * The query will return two variables isUnique and isFound
	 * 
	 * isUnique returns the number of statements that match the graph patterns
	 * and filter conditions added to the subquery so if isUnique variable
	 * greater than zero then there is a unique constraint violation
	 * 
	 * isFound return the number of statements that match the graph patterns
	 * added to the subquery . if isFound variable is 0 then there is some or
	 * all objectValues does not exist so it violates the data integrity
	 * constraint
	 */

	public boolean hasNoConstraintViolations(String applicationName, ArrayList<ValueOfTypeClass> classValueList,
			LinkedHashMap<String, ArrayList<PropertyValue>> htblUniquePropValueList, Class subjectClass) {

		String queryString = constructViolationsCheckQueryStr(applicationName, classValueList, htblUniquePropValueList,
				subjectClass);
		System.out.println(queryString);
		try {
			ResultSet resultSet = oracle.executeQuery(queryString, 0, 1);
			resultSet.next();

			Object integrityCheck = resultSet.getObject("isFound");
			Object uniquenessCheck = resultSet.getObject("isUnique");

			System.out.println(integrityCheck.toString());
			System.out.println(uniquenessCheck.toString());

			if (integrityCheck != null) {
				if (Integer.parseInt(integrityCheck.toString()) == 0) {
					throw new InvalidPropertyValuesException(subjectClass.getName());
				}

			}

			if (uniquenessCheck != null) {
				if (Integer.parseInt(uniquenessCheck.toString()) != 0) {
					throw new UniqueConstraintViolationException(subjectClass.getName());
				}
			}

			return true;

		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage(), "Application");

		}
	}

	/*
	 * Subqueries which executes in between 0.6 to 0.7 seconds
	 */

	/*
	 * constructQuery used to construct a sparql query String based on input it
	 * takes the application name to get the application model in order to query
	 * and it takes a hashtable of classes and values to check if there is an
	 * instance of that class with this value exist or not to insure data
	 * integrity and consistency
	 * 
	 * It also add graph patterns to the query to check that no constraint
	 * violations happened
	 * 
	 * The Sparql injected sql query auto constructed query has the same format
	 * like this:
	 * 
	 * select found from table(sem_match('select (count(*) as ?found)
	 * where{iot-platform:testapp a iot-platform:Application
	 * .}',sem_models('TESTAPP_MODEL'),null,SEM_ALIASES(SEM_ALIAS('iot-platform'
	 * ,'http://iot-platform#')),null));
	 */
	private String constructViolationsCheckQueryStr(String applicationName, ArrayList<ValueOfTypeClass> classValueList,
			LinkedHashMap<String, ArrayList<PropertyValue>> htblUniquePropValueList, Class subjectClass) {

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("select isFound,isUnique from table(sem_match('select ?isFound ?isUnique where{ ");
		StringBuilder prefixStringBuilder = new StringBuilder();

		String dataIntegrityConstraintVoilationCheckSubQueryStr = "";
		String datauniqueConstraintViolationCheckSubQueryStr = "";

		if (classValueList.size() > 0)
			dataIntegrityConstraintVoilationCheckSubQueryStr = constructIntegrityConstraintCheckSubQuery(
					classValueList);

		if (htblUniquePropValueList.size() > 0)
			datauniqueConstraintViolationCheckSubQueryStr = constructUniqueContstraintCheckSubQueryStr2(
					htblUniquePropValueList, subjectClass);

		stringBuilder.append(dataIntegrityConstraintVoilationCheckSubQueryStr + "  "
				+ datauniqueConstraintViolationCheckSubQueryStr);

		int counter = 0;
		int stop = Prefixes.values().length - 1;
		for (Prefixes prefix : Prefixes.values()) {
			/*
			 * 8 because there are only 9 prefixes and the counter started from
			 * 0
			 */
			if (counter == stop) {
				prefixStringBuilder.append("SEM_ALIAS('" + prefix.getPrefixName() + "','" + prefix.getUri() + "')");
			} else {
				prefixStringBuilder.append("SEM_ALIAS('" + prefix.getPrefixName() + "','" + prefix.getUri() + "'),");
			}

			counter++;
		}

		String modelName = applicationName.replaceAll(" ", "").toUpperCase() + suffix;
		stringBuilder.append("}',sem_models('" + modelName + "'),null,");
		stringBuilder.append("SEM_ALIASES(" + prefixStringBuilder.toString() + "),null))");

		return stringBuilder.toString();
	}

	/*
	 * constructIntegrityConstraintCheckSubQuery method takes a list of
	 * objectProperties values and construct a subquery that checks that the
	 * object are valid (the passed objectValue exist in the applicationModel)
	 */

	private String constructIntegrityConstraintCheckSubQuery(ArrayList<ValueOfTypeClass> classValueList) {

		StringBuilder stringBuilder = new StringBuilder();

		/*
		 * start of the subQuery
		 */

		stringBuilder.append("{ SELECT (COUNT(*) as ?isFound ) WHERE { ");

		for (ValueOfTypeClass valueOfTypeClass : classValueList) {
			Class valueClassType = valueOfTypeClass.getTypeClass();
			Object value = valueOfTypeClass.getValue();

			/*
			 * any instance created has the prefix of iot-platform so the
			 * subject will have the iot-platform prefix
			 */

			String subject = Prefixes.IOT_PLATFORM.getPrefix() + value.toString().toLowerCase();
			String object = valueClassType.getPrefix().getPrefix() + valueClassType.getName();

			stringBuilder.append(subject + " a " + object + " . ");

		}

		/*
		 * complete end of the subquery structure
		 */

		stringBuilder.append(" }} ");

		return stringBuilder.toString();
	}

	/*
	 * constructUniqueContstraintCheckSubQueryStr method takes a list of
	 * properties that must have unique value and the value inserted to check if
	 * there is a similar value that will violate unique constraint
	 */

	private String constructUniqueContstraintCheckSubQueryStr(ArrayList<PropertyValue> uniquePropValueList,
			Class subjectClass) {

		StringBuilder stringBuilder = new StringBuilder();
		StringBuilder filterConditionsStringBuilder = new StringBuilder();

		String variableName = "?var";
		int variableCount = 0;

		/*
		 * start of the subQuery
		 */

		stringBuilder.append("{ SELECT (COUNT(*) as ?isUnique ) WHERE { ");

		stringBuilder.append("?subject a <" + subjectClass.getUri() + "> ; ");

		/*
		 * start of filter part
		 */

		filterConditionsStringBuilder.append(" FILTER ( ");

		/*
		 * iteration on uniquePropValueList to construct graph patterns and
		 * filter conditions of the subquery
		 */

		int count = 0;
		int size = uniquePropValueList.size();

		for (PropertyValue propertyValue : uniquePropValueList) {
			String prefixedPropertyName = propertyValue.getPropertyName();
			Object value = propertyValue.getValue();

			if (count < size - 1) {
				stringBuilder.append(prefixedPropertyName + "  " + variableName + variableCount + " ; ");

				/*
				 * check is the value is a string value to make add ""
				 */

				if (value instanceof String) {
					filterConditionsStringBuilder.append("LCASE(" + variableName + variableCount + ") = \""
							+ value.toString().toLowerCase() + "\" ||  ");
				} else {
					filterConditionsStringBuilder.append("LCASE(" + variableName + variableCount + ") = "
							+ value.toString().toLowerCase() + " ||  ");
				}

			} else {

				/*
				 * check if it is the last condition to end the graph patterns
				 * with a dot
				 * 
				 * and not adding || for the last filter condition
				 */

				stringBuilder.append(prefixedPropertyName + "  " + variableName + variableCount + " .");

				/*
				 * check is the value is a string value to make add ""
				 */

				if (value instanceof String) {
					filterConditionsStringBuilder.append(
							"LCASE(" + variableName + variableCount + ") = \"" + value.toString().toLowerCase() + "\"");
				} else {
					filterConditionsStringBuilder
							.append("LCASE(" + variableName + variableCount + ") = " + value.toString().toLowerCase());
				}

			}

			count++;
			variableCount++;
		}

		/*
		 * complete end of the filter part
		 */

		filterConditionsStringBuilder.append(" )");

		/*
		 * complete end of the subquery structure
		 */

		stringBuilder.append(" " + filterConditionsStringBuilder.toString() + " }} ");

		return stringBuilder.toString();

	}

	public String constructUniqueContstraintCheckSubQueryStr3(
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<PropertyValue>>> htblUniquePropValueList,
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
		ArrayList<PropertyValue> currentClassPropertyValueList = htblUniquePropValueList.get(mainClassPrefixedName)
				.get(mainInstanceUniqueIdentifier);

		/*
		 * start of the subQuery
		 */
		queryBuilder.append("{ SELECT (COUNT(*) as ?isUnique ) WHERE { \n");

		/*
		 * start of filter part
		 */
		filterConditionsBuilder.append(" FILTER ( ");

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
		for (PropertyValue propertyValue : currentClassPropertyValueList) {

			helper(htblUniquePropValueList, queryBuilder, filterConditionsBuilder, endGraphPatternBuilder,
					mainClassPrefixedName, mainInstanceUniqueIdentifier, propertyValue, vairableNum, subjectNum);

			if (propertyValue.isObject()) {
				subjectNum[0]++;
			} else {
				vairableNum[0]++;
			}

		}

		/*
		 * end of the main graph pattern
		 */
		queryBuilder.append(" . \n");

		/*
		 * Appending the endGraphPatternBuilder to the end of the queryBuilder
		 */
		queryBuilder.append(endGraphPatternBuilder.toString());

		filterConditionsBuilder.append(" )");

		/*
		 * complete end of the subquery structure
		 */
		queryBuilder.append(" " + filterConditionsBuilder.toString() + " \n }} ");

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
	public void helper(LinkedHashMap<String, LinkedHashMap<String, ArrayList<PropertyValue>>> htblUniquePropValueList,
			StringBuilder queryBuilder, StringBuilder filterConditionsBuilder, StringBuilder endGraphPatternBuilder,
			String currentClassPrefixedName, String currentClassInstanceUniqueIdentifier, PropertyValue propertyValue,
			int[] vairableNum, int[] subjectNum) {

		/*
		 * The property is an objectProperty and the value is a nestedObject(new
		 * class object instance that has its own properties and values)
		 */
		if (propertyValue.isObject()) {

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
			queryBuilder.append(" ; \n" + propertyValue.getPropertyName() + "  ?subject" + subjectNum[0]);

			/*
			 * get the value classType which is stored in the
			 * prefixedObjectValueClassName of the propertyValue this classType
			 * represent the prefiexedClassName of the value class type and it
			 * was added by RequestValidation class
			 */
			String objectClassTypeName = propertyValue.getPrefixedObjectValueClassName();

			/*
			 * get the uniqueIdentifer of the objectProperty inOrder to
			 * breakDown the nestedObject to construct Recursively the query
			 */
			String objectVaueUniqueIdentifier = propertyValue.getValue().toString();

			/*
			 * get the objectValueInstance's propertyValueList
			 */
			ArrayList<PropertyValue> propertyValueList = htblUniquePropValueList.get(objectClassTypeName)
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

			for (PropertyValue objectPropertyValue : propertyValueList) {

				helper(htblUniquePropValueList, tempBuilder, filterConditionsBuilder, endGraphPatternBuilder,
						objectClassTypeName, objectVaueUniqueIdentifier, objectPropertyValue, vairableNum, subjectNum);
				vairableNum[0]++;
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

			if (htblUniquePropValueList.get(currentClassPrefixedName).get(currentClassInstanceUniqueIdentifier)
					.indexOf(propertyValue) < htblUniquePropValueList.get(currentClassPrefixedName)
							.get(currentClassInstanceUniqueIdentifier).size() - 1) {

				/*
				 * it is not the last property so we will end the previous
				 * triple pattern with ;
				 */

				queryBuilder.append(" ; \n" + propertyValue.getPropertyName() + "  ?var" + vairableNum[0]);

			} else {
				/*
				 * it is the last property so we will end the previous triple
				 * pattern with ; and this one with .
				 */
				queryBuilder.append(" ; \n" + propertyValue.getPropertyName() + "  ?var" + vairableNum[0] + " . \n");

			}

		}

	}

	public String constructUniqueContstraintCheckSubQueryStr2(
			LinkedHashMap<String, ArrayList<PropertyValue>> htblUniquePropValueList, Class subjectClass) {

		StringBuilder stringBuilder = new StringBuilder();
		StringBuilder filterConditionsStringBuilder = new StringBuilder();

		/*
		 * htblClassPropertyPrefixedNames is LinkedHashMap that holds key
		 * prefixedClassName and value LinkedHashMap<String, String> (which
		 * contains key prefixedPropertyName and value null if value is not
		 * object else value objectReference )
		 * 
		 * I used this LinkedHashMap to avoid replicating properties in the
		 * graph pattern part in the query
		 * 
		 * eg. avoid having : SELECT (COUNT(*) as ?isUnique ) WHERE { ?subject0
		 * a iot-platform:Developer ; foaf:mbox ?var0 ; foaf:mbox ?var1.}
		 */
		LinkedHashMap<String, LinkedHashMap<String, String>> htblClassPropertyPrefixedNames = new LinkedHashMap<>();

		/*
		 * htblPrefixedClassNameNodeReference is a LinkedHashMap that contains
		 * key prefixedClassName and value graphNodeReference which points to
		 * another class instance
		 * 
		 * I used this LinkedHashMap to link objectProperties with their
		 * nodeReference
		 * 
		 * eg. SELECT (COUNT(*) as ?isUnique ) WHERE { ?subject0 a
		 * iot-platform:Developer ; foaf:knows ?subject1. ?subject1 a
		 * foaf:Person;foaf:userName ?var4 ;
		 */
		LinkedHashMap<String, String> htblPrefixedClassNameNodeReference = new LinkedHashMap<>();

		/*
		 * give every key prefixedClassName in htblUniquePropValueList a node
		 * reference
		 */
		String subjectName = "?subject";
		int subjectCount = 0;
		Iterator<String> htblUniquePropValueListIterator = htblUniquePropValueList.keySet().iterator();

		while (htblUniquePropValueListIterator.hasNext()) {
			String prefxiedClassName = htblUniquePropValueListIterator.next();

			htblPrefixedClassNameNodeReference.put(prefxiedClassName, subjectName + subjectCount);
			subjectCount++;
		}

		String variableName = "?var";
		int variableCount = 0;

		/*
		 * start of the subQuery
		 */
		stringBuilder.append("{ SELECT (COUNT(*) as ?isUnique ) WHERE { \n");

		/*
		 * start of filter part
		 */
		filterConditionsStringBuilder.append(" FILTER ( ");

		/*
		 * Iterate over htblUniquePropValueList
		 */
		htblUniquePropValueListIterator = htblUniquePropValueList.keySet().iterator();

		boolean start = true;
		while (htblUniquePropValueListIterator.hasNext()) {
			String prefixedClassName = htblUniquePropValueListIterator.next();
			ArrayList<PropertyValue> propertyValuesList = htblUniquePropValueList.get(prefixedClassName);

			stringBuilder.append(htblPrefixedClassNameNodeReference.get(prefixedClassName) + " a " + prefixedClassName);

			/*
			 * add new row to htblClassPropertyPrefixedNames with new
			 * prefixedClassName and new htblPropertyName LinkedHashMap to hold
			 * properties
			 */
			LinkedHashMap<String, String> htblPropertyName = new LinkedHashMap<>();
			htblClassPropertyPrefixedNames.put(prefixedClassName, htblPropertyName);

			/*
			 * Iterate over propertyValueList of prefixedClassName key and
			 * construct uniqueConstraintSubQuery
			 */
			int size = propertyValuesList.size();
			for (int i = 0; i < size; i++) {
				PropertyValue propertyValue = propertyValuesList.get(i);

				String prefixedPropertyName = propertyValue.getPropertyName();
				Object prefixedPropertyValue = propertyValue.getValue();
				boolean propertyFound = false;

				/*
				 * check if the property was added before or not to avoid having
				 * duplicate properties in the graph pattern as discussed above
				 * with example
				 */
				if (htblClassPropertyPrefixedNames.get(prefixedClassName).containsKey(prefixedPropertyName)) {
					/*
					 * set propertyFound flag to true to avoid adding this
					 * property again to the query
					 */
					propertyFound = true;

				} else {
					/*
					 * property was not added before
					 * 
					 * check if propertyValue isObject (value is an object
					 * instance of anotherClass) to add the value of the
					 * property to the valueClassType graph node reference
					 * 
					 * else add value = null which indicates that the value was
					 * not an object (Literal)
					 */
					if (propertyValue.isObject()) {
						subjectCount++;

						/*
						 * classType of objectValue
						 */
						String prefixedValueClassType = propertyValue.getValue().toString();
						htblClassPropertyPrefixedNames.get(prefixedClassName).put(prefixedPropertyName,
								htblPrefixedClassNameNodeReference.get(prefixedValueClassType));
					} else {
						htblClassPropertyPrefixedNames.get(prefixedClassName).put(prefixedPropertyName, null);
					}

				}

				/*
				 * check propertyFound is false to add property to the query
				 * 
				 * Construct query
				 */
				if (!propertyFound) {

					if (i < size) {

						if (propertyValue.isObject()) {
							stringBuilder.append(" ; \n" + prefixedPropertyName + "  "
									+ htblClassPropertyPrefixedNames.get(prefixedClassName).get(prefixedPropertyName));

						} else {
							stringBuilder.append(" ; \n" + prefixedPropertyName + "  " + variableName + variableCount);

						}

					} else {
						if (propertyValue.isObject()) {
							stringBuilder.append(prefixedPropertyName + "  "
									+ htblClassPropertyPrefixedNames.get(prefixedClassName).get(prefixedPropertyName));

						} else {
							stringBuilder.append(prefixedPropertyName + "  " + variableName + variableCount);

						}

					}
					variableCount++;
				}

				/*
				 * if property was an objectProperty so do not add it to the
				 * filter because its value is a nodeReference
				 * 
				 * check if the value was replicated to remove any duplicate
				 * filter condition
				 * 
				 * It will not work with multiple value property because it will
				 * need further computation because the propertyName is a key so
				 * it must be unique so its value will be overwrote so it will
				 * be duplicated eg. mbox (property for Agent class and all its
				 * subClasses eg. Person)
				 */
				if (!propertyValue.isObject()
						&& (htblClassPropertyPrefixedNames.get(prefixedClassName).get(prefixedPropertyName) == null
								|| !htblClassPropertyPrefixedNames.get(prefixedClassName).get(prefixedPropertyName)
										.equals(prefixedPropertyValue))) {

					htblClassPropertyPrefixedNames.get(prefixedClassName).put(prefixedPropertyName,
							prefixedPropertyValue.toString());

					if (i < size && (i != 0 || !start)) {

						/*
						 * check is the value is a string value to make add ""
						 */
						if (prefixedPropertyValue instanceof String) {
							filterConditionsStringBuilder.append(" ||  \n LCASE(" + variableName + (variableCount - 1)
									+ ") = \"" + prefixedPropertyValue.toString().toLowerCase() + "\" ");
						} else {
							filterConditionsStringBuilder.append("|| \n LCASE(" + variableName + (variableCount - 1)
									+ ") = " + prefixedPropertyValue.toString().toLowerCase());
						}
					} else {

						/*
						 * check is the value is a string value to make add ""
						 */

						if (prefixedPropertyValue instanceof String) {
							filterConditionsStringBuilder.append(" \n LCASE(" + variableName + (variableCount - 1)
									+ ") = \"" + prefixedPropertyValue.toString().toLowerCase() + "\" ");
						} else {
							filterConditionsStringBuilder.append(" \n  LCASE(" + variableName + (variableCount - 1)
									+ ") = " + prefixedPropertyValue.toString().toLowerCase());
						}
						/*
						 * start flag is used to dynamically avoid adding ||
						 * before the first filter condition
						 */
						start = false;
					}

				}

			}

			stringBuilder.append(" . \n");

		}

		filterConditionsStringBuilder.append(" )");

		/*
		 * complete end of the subquery structure
		 */

		// stringBuilder.append(" }}");
		stringBuilder.append(" " + filterConditionsStringBuilder.toString() + " }} ");

		// Iterator<String> iterator =
		// htblClassPropertyPrefixedNames.keySet().iterator();
		// while (iterator.hasNext()) {
		// String className = iterator.next();
		// System.out.println(className + " [ \n");
		//
		// LinkedHashMap<String, String> x =
		// htblClassPropertyPrefixedNames.get(className);
		//
		// Iterator<String> iterator2 = x.keySet().iterator();
		// while (iterator2.hasNext()) {
		// String property = iterator2.next();
		// String value = x.get(property);
		//
		// System.out.println("{ " + property + " : " + value + " } \n");
		// }
		//
		// System.out.println(" ] \n");
		// }

		return stringBuilder.toString();

	}

	/*
	 * Single Queries which executes in between 0.8 to 1 seconds
	 */

	// public boolean hasNoConstraintViolationsForSinqleQueries(String
	// applicationName,
	// ArrayList<ValueOfTypeClass> classValueList, ArrayList<PropertyValue>
	// uniquePropValueList,
	// Class subjectClass) {
	// long startTime = System.currentTimeMillis();
	//
	// if (classValueList.size() > 0) {
	// String queryString =
	// constructIntegrityConstraintCheckQuery(classValueList, applicationName);
	//
	// try {
	// ResultSet resultSet = oracle.executeQuery(queryString, 0, 1);
	// resultSet.next();
	//
	// Object integrityCheck = resultSet.getObject("isFound");
	//
	// System.out.println("Time Taken: " + ((System.currentTimeMillis() -
	// startTime) / 1000.0));
	// if (integrityCheck != null) {
	// if (Integer.parseInt(integrityCheck.toString()) == 0) {
	// throw new InvalidPropertyValuesException(subjectClass.getName());
	// }
	//
	// }
	// } catch (SQLException e) {
	// throw new DatabaseException(e.getMessage(), "Application");
	// }
	//
	// }
	// System.out.println("Time Taken: " + ((System.currentTimeMillis() -
	// startTime) / 1000.0));
	//
	// if (uniquePropValueList.size() > 0) {
	// String queryString =
	// constructUniqueContstraintCheckQueryStr(uniquePropValueList,
	// subjectClass,
	// applicationName);
	// try {
	// ResultSet resultSet = oracle.executeQuery(queryString, 0, 1);
	// resultSet.next();
	// Object uniquenessCheck = resultSet.getObject("isUnique");
	// if (uniquenessCheck != null) {
	// if (Integer.parseInt(uniquenessCheck.toString()) != 0) {
	// throw new UniqueConstraintViolationException(subjectClass.getName());
	// }
	// }
	// } catch (SQLException e) {
	// throw new DatabaseException(e.getMessage(), "Application");
	// }
	// }
	// System.out.println("Time Taken: " + ((System.currentTimeMillis() -
	// startTime) / 1000.0));
	// return true;
	//
	// }

	// private String
	// constructUniqueContstraintCheckQueryStr(ArrayList<PropertyValue>
	// uniquePropValueList,
	// Class subjectClass,String applicationName) {
	//
	// StringBuilder stringBuilder = new StringBuilder();
	// StringBuilder filterConditionsStringBuilder = new StringBuilder();
	//
	// String variableName = "?var";
	// int variableCount = 0;
	//
	// /*
	// * start of the subQuery
	// */
	//
	// stringBuilder.append("select isUnique from table(sem_match('SELECT
	// (COUNT(*) as ?isUnique ) WHERE { ");
	//
	// stringBuilder.append("?subject a <" + subjectClass.getUri() + "> ; ");
	//
	// /*
	// * start of filter part
	// */
	//
	// filterConditionsStringBuilder.append(" FILTER ( ");
	//
	// /*
	// * iteration on uniquePropValueList to construct graph patterns and
	// * filter conditions of the subquery
	// */
	//
	// int count = 0;
	// int size = uniquePropValueList.size();
	//
	// for (PropertyValue propertyValue : uniquePropValueList) {
	// String prefixedPropertyName = propertyValue.getPropertyName();
	// Object value = propertyValue.getValue();
	//
	// if (count < size - 1) {
	// stringBuilder.append(prefixedPropertyName + " " + variableName +
	// variableCount + " ; ");
	//
	// /*
	// * check is the value is a string value to make add ""
	// */
	//
	// if (value instanceof String) {
	// filterConditionsStringBuilder
	// .append("LCASE(" + variableName + variableCount + ") = \"" +
	// value.toString() + "\" || ");
	// } else {
	// filterConditionsStringBuilder
	// .append("LCASE(" + variableName + variableCount + ") = " +
	// value.toString() + " || ");
	// }
	//
	// } else {
	//
	// /*
	// * check if it is the last condition to end the graph patterns
	// * with a dot
	// *
	// * and not adding || for the last filter condition
	// */
	//
	// stringBuilder.append(prefixedPropertyName + " " + variableName +
	// variableCount + " .");
	//
	// /*
	// * check is the value is a string value to make add ""
	// */
	//
	// if (value instanceof String) {
	// filterConditionsStringBuilder
	// .append("LCASE(" + variableName + variableCount + ") = \"" +
	// value.toString() + "\"");
	// } else {
	// filterConditionsStringBuilder
	// .append("LCASE(" + variableName + variableCount + ") = " +
	// value.toString());
	// }
	//
	// }
	//
	// count++;
	// variableCount++;
	// }
	//
	// /*
	// * complete end of the filter part
	// */
	//
	// filterConditionsStringBuilder.append(" )");
	//
	// /*
	// * complete end of the subquery structure
	// */
	//
	//
	//
	// stringBuilder.append(" " + filterConditionsStringBuilder.toString() + "
	// }' ");
	//
	// StringBuilder prefixStringBuilder = new StringBuilder();
	// int counter = 0;
	// int stop = Prefixes.values().length - 1;
	// for (Prefixes prefix : Prefixes.values()) {
	// /*
	// * 8 because there are only 9 prefixes and the counter started from
	// * 0
	// */
	// if (counter == stop) {
	// prefixStringBuilder.append("SEM_ALIAS('" + prefix.getPrefixName() + "','"
	// + prefix.getUri() + "')");
	// } else {
	// prefixStringBuilder.append("SEM_ALIAS('" + prefix.getPrefixName() + "','"
	// + prefix.getUri() + "'),");
	// }
	//
	// counter++;
	// }
	//
	// String modelName = applicationName.replaceAll(" ", "").toUpperCase() +
	// suffix;
	// stringBuilder.append(" ,sem_models('" + modelName + "'),null,");
	// stringBuilder.append("SEM_ALIASES(" + prefixStringBuilder.toString() +
	// "),null))");
	//
	// return stringBuilder.toString();
	//
	// }

	// private String
	// constructIntegrityConstraintCheckQuery(ArrayList<ValueOfTypeClass>
	// classValueList,
	// String applicationName) {
	//
	// StringBuilder stringBuilder = new StringBuilder();
	//
	// /*
	// * start of the subQuery
	// */
	//
	// stringBuilder.append("select isFound from table(sem_match('SELECT
	// (COUNT(*) as ?isFound ) WHERE { ");
	//
	// for (ValueOfTypeClass valueOfTypeClass : classValueList) {
	// Class valueClassType = valueOfTypeClass.getTypeClass();
	// Object value = valueOfTypeClass.getValue();
	//
	// /*
	// * any instance created has the prefix of iot-platform so the
	// * subject will have the iot-platform prefix
	// */
	//
	// String subject = Prefixes.IOT_PLATFORM.getPrefix() +
	// value.toString().toLowerCase();
	// String object = valueClassType.getPrefix().getPrefix() +
	// valueClassType.getName();
	//
	// stringBuilder.append(subject + " a " + object + " . ");
	//
	// }
	//
	// /*
	// * complete end of the subquery structure
	// */
	//
	// stringBuilder.append(" }' ");
	//
	// StringBuilder prefixStringBuilder = new StringBuilder();
	// int counter = 0;
	// int stop = Prefixes.values().length - 1;
	// for (Prefixes prefix : Prefixes.values()) {
	// /*
	// * 8 because there are only 9 prefixes and the counter started from
	// * 0
	// */
	// if (counter == stop) {
	// prefixStringBuilder.append("SEM_ALIAS('" + prefix.getPrefixName() + "','"
	// + prefix.getUri() + "')");
	// } else {
	// prefixStringBuilder.append("SEM_ALIAS('" + prefix.getPrefixName() + "','"
	// + prefix.getUri() + "'),");
	// }
	//
	// counter++;
	// }
	//
	// String modelName = applicationName.replaceAll(" ", "").toUpperCase() +
	// suffix;
	// stringBuilder.append(" ,sem_models('" + modelName + "'),null,");
	// stringBuilder.append("SEM_ALIASES(" + prefixStringBuilder.toString() +
	// "),null))");
	//
	// return stringBuilder.toString();
	// }

	// public static void main(String[] args) {
	// String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
	// String szUser = "rdfusr";
	// String szPasswd = "rdfusr";
	//
	// ValidationDao validationDao = new ValidationDao(new Oracle(szJdbcURL,
	// szUser, szPasswd));
	//
	// ArrayList<PropertyValue> uniquePropValueList = new ArrayList<>();
	//
	// // those will not voilate any constraints
	// uniquePropValueList.add(new PropertyValue("foaf:userName",
	// "HatemMorgans"));
	// uniquePropValueList.add(new PropertyValue("foaf:mbox",
	// "hatemmorgan17s@gmail.com"));
	// uniquePropValueList.add(new PropertyValue("foaf:mbox",
	// "hatem.el-sayedl@student.guc.edu.eg"));
	//
	// // those will violate uniqueness constraint
	// // uniquePropValueList.add(new PropertyValue("foaf:userName",
	// // "HatemMorgan"));
	// // uniquePropValueList.add(new PropertyValue("foaf:mbox",
	// // "hatemmorgan17@gmail.com"));
	// // uniquePropValueList.add(new PropertyValue("foaf:mbox",
	// // "hatem.el-sayed--@student.guc.edu.eg"));
	//
	// ArrayList<ValueOfTypeClass> classValueList = new ArrayList<>();
	// classValueList.add(new ValueOfTypeClass(new Application(),
	// "testapplication"));
	// classValueList.add(new ValueOfTypeClass(new Person(), "hatemmorgan"));
	//
	// // this will fail the check
	// // classValueList.add(new ValueOfTypeClass(new Person(), "hatem"));
	// try {
	// //
	// System.out.println(validationDao.constructIntegrityConstraintCheckSubQuery(classValueList));
	// // System.out.println(
	// //
	// validationDao.constructUniqueContstraintCheckSubQueryStr(uniquePropValueList,
	// // new Person()));
	// //
	// System.out.println(validationDao.constructViolationsCheckQueryStr("test
	// // application", classValueList,
	// // uniquePropValueList, new Person()));
	// System.out.println(validationDao.hasNoConstraintViolations("testApplication",
	// classValueList,
	// uniquePropValueList, new Person()));
	// } catch (DatabaseException e) {
	// System.out.println(e.getCode());
	// System.out.println(e.getMessage());
	// System.out.println(e.getExceptionMessage());
	// }
	//
	// }
}
