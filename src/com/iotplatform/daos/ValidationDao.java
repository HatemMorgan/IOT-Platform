package com.iotplatform.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.exceptions.InvalidPropertyValuesException;
import com.iotplatform.exceptions.UniqueConstraintViolationException;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.utilities.ValueOfTypeClassUtility;

import oracle.spatial.rdf.client.jena.Oracle;

@Component
public class ValidationDao {
	private Oracle oracle;

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

	public boolean hasNoConstraintViolations(String applicationModelName,
			ArrayList<ValueOfTypeClassUtility> classValueList,
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<Object>>> htblUniquePropValueList,
			Class subjectClass) {

		String queryString = constructViolationsCheckQueryStr(applicationModelName, classValueList,
				htblUniquePropValueList);
		 System.out.println(queryString);
		try {
			ResultSet resultSet = oracle.executeQuery(queryString, 0, 1);
			resultSet.next();

			Object integrityCheck = resultSet.getObject("isFound");
			Object uniquenessCheck = resultSet.getObject("isUnique");

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
	 * It calls both constructIntegrityConstraintCheckSubQuery and
	 * constructUniqueConstraintCheckSubQuery to get subQueries and construct
	 * the main query
	 * 
	 * It also add graph patterns to the query to check that no constraint
	 * violations happened
	 * 
	 * The Sparql injected sql query auto constructed query has the same format
	 * like this:
	 * 
	 * select isFound,isUnique from table(sem_match('select ?isFound ?isUnique
	 * where{ { SELECT (COUNT(*) as ?isFound ) WHERE {
	 * iot-platform:testapplication a iot-platform:Application .
	 * iot-platform:karammorgan a foaf:Person . }} { SELECT (COUNT(*) as
	 * ?isUnique ) WHERE { ?subject0 a foaf:Agent ; foaf:mbox ?var0 . ?subject1
	 * a foaf:Person ; foaf:userName ?var1 . FILTER ( LCASE( ?var0) =
	 * "haytham.ismails@gmail.com"|| LCASE( ?var0) =
	 * "haytham.ismails@student.guc.edu.eg"|| LCASE( ?var0) =
	 * "ahmedmorganl@gmail.com"|| LCASE( ?var0) = "hatemmorgan17s@gmail.com"||
	 * LCASE( ?var0) = "hatem.el-sayeds@student.guc.edu.eg"|| LCASE( ?var1) =
	 * "haythamismails"|| LCASE( ?var1) = "ahmedmorganl"|| LCASE( ?var1) =
	 * "hatemmorgans" ) }}
	 * }',sem_models('TESTAPPLICATION_MODEL'),null,SEM_ALIASES(SEM_ALIAS('ssn','
	 * http://purl.oclc.org/NET/ssnx/ssn#'),SEM_ALIAS('geo','http://www.w3.org/
	 * 2003/01/geo/wgs84_pos#'),SEM_ALIAS('iot-lite','http://purl.oclc.org/NET/
	 * UNIS/fiware/iot-lite#'),SEM_ALIAS('iot-platform','http://iot-platform#'),
	 * SEM_ALIAS('foaf','http://xmlns.com/foaf/0.1/'),SEM_ALIAS('xsd','http://
	 * www.w3.org/2001/XMLSchema#'),SEM_ALIAS('owl','http://www.w3.org/2002/07/
	 * owl#'),SEM_ALIAS('rdfs','http://www.w3.org/2000/01/rdf-schema#'),
	 * SEM_ALIAS('rdf','http://www.w3.org/1999/02/22-rdf-syntax-ns#'),SEM_ALIAS(
	 * 'qu','http://purl.org/NET/ssnx/qu/qu#'),SEM_ALIAS('DUL','http://www.loa-
	 * cnr.it/ontologies/DUL.owl#')),null))
	 *
	 */
	private String constructViolationsCheckQueryStr(String applicationModelName,
			ArrayList<ValueOfTypeClassUtility> classValueList,
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<Object>>> htblUniquePropValueList) {

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("select isFound,isUnique from table(sem_match('select ?isFound ?isUnique where{ ");
		StringBuilder prefixStringBuilder = new StringBuilder();

		String dataIntegrityConstraintVoilationCheckSubQueryStr = "";
		String datauniqueConstraintViolationCheckSubQueryStr = "";

		if (classValueList.size() > 0)
			dataIntegrityConstraintVoilationCheckSubQueryStr = constructIntegrityConstraintCheckSubQuery(
					classValueList);

		if (htblUniquePropValueList.size() > 0)
			datauniqueConstraintViolationCheckSubQueryStr = constructUniqueConstraintCheckSubQuery(
					htblUniquePropValueList);

		stringBuilder.append(dataIntegrityConstraintVoilationCheckSubQueryStr + "  "
				+ datauniqueConstraintViolationCheckSubQueryStr);

		int counter = 0;
		int stop = Prefix.values().length - 1;
		for (Prefix prefix : Prefix.values()) {
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
		stringBuilder.append("}',sem_models('" + applicationModelName + "'),null,");
		stringBuilder.append("SEM_ALIASES(" + prefixStringBuilder.toString() + "),null))");

		return stringBuilder.toString();
	}

	/*
	 * constructIntegrityConstraintCheckSubQuery method takes a list of
	 * objectProperties values and construct a subquery that checks that the
	 * object are valid (the passed objectValue exist in the applicationModel)
	 * 
	 * eg . { SELECT (COUNT(*) as ?isFound ) WHERE {
	 * iot-platform:testapplication a iot-platform:Application .
	 * iot-platform:karammorgan a foaf:Person . }}
	 */

	private String constructIntegrityConstraintCheckSubQuery(ArrayList<ValueOfTypeClassUtility> classValueList) {

		StringBuilder stringBuilder = new StringBuilder();

		/*
		 * start of the subQuery
		 */

		stringBuilder.append("{ SELECT (COUNT(*) as ?isFound ) WHERE { ");

		for (ValueOfTypeClassUtility valueOfTypeClass : classValueList) {
			Class valueClassType = valueOfTypeClass.getTypeClass();
			Object value = valueOfTypeClass.getValue();

			/*
			 * any instance created has the prefix of iot-platform so the
			 * subject will have the iot-platform prefix
			 */

			String subject = Prefix.IOT_PLATFORM.getPrefix() + value.toString().toLowerCase().replaceAll(" ", "");
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
	 * constructUniqueConstraintCheckSubQuery it constructs subQuery that checks
	 * for unique constraint violations eg.
	 * 
	 * { SELECT (COUNT(*) as ?isUnique ) WHERE { ?subject0 a foaf:Agent ;
	 * foaf:mbox ?var0 . ?subject1 a foaf:Person ; foaf:userName ?var1 . FILTER
	 * ( LCASE( ?var0) = "haytham.ismails@gmail.com"|| LCASE( ?var0) =
	 * "haytham.ismails@student.guc.edu.eg"|| LCASE( ?var0) =
	 * "ahmedmorganl@gmail.com"|| LCASE( ?var0) = "hatemmorgan17s@gmail.com"||
	 * LCASE( ?var0) = "hatem.el-sayeds@student.guc.edu.eg"|| LCASE( ?var1) =
	 * "haythamismails"|| LCASE( ?var1) = "ahmedmorganl"|| LCASE( ?var1) =
	 * "hatemmorgans" ) }}
	 */
	private String constructUniqueConstraintCheckSubQuery(
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<Object>>> htblUniquePropValueList) {

		/*
		 * main builder for dynamically building the query
		 */
		StringBuilder queryBuilder = new StringBuilder();

		/*
		 * filterConditionsBuilder is for building filter conditions
		 */
		StringBuilder filterConditionsBuilder = new StringBuilder();

		/*
		 * start of the subQuery
		 */
		queryBuilder.append("{ SELECT (COUNT(*) as ?isUnique ) WHERE { \n");

		/*
		 * start of filter part
		 */
		filterConditionsBuilder.append(" FILTER ( ");

		/*
		 * iterate on htblUniquePropValueList to construct query
		 */
		Iterator<String> htblUniquePropValueListIterator = htblUniquePropValueList.keySet().iterator();

		int subjectCount = 0;
		int variableCount = 0;

		/*
		 * indicates that it is the start of building query inOrder not to add
		 * || at the begining of the first filter condition
		 */
		boolean start = true;
		while (htblUniquePropValueListIterator.hasNext()) {
			String prefixedSubjectClassName = htblUniquePropValueListIterator.next();
			LinkedHashMap<String, ArrayList<Object>> propValueMap = htblUniquePropValueList
					.get(prefixedSubjectClassName);

			/*
			 * iterate on propValueMap to get prefixedPropertyName to construct
			 * graph pattern and from list of values construct filter conditions
			 */

			Iterator<String> propValueMapIterator = propValueMap.keySet().iterator();

			while (propValueMapIterator.hasNext()) {
				String prefixedPropertyName = propValueMapIterator.next();
				ArrayList<Object> valueList = propValueMap.get(prefixedPropertyName);

				/*
				 * start a new graph pattern (this triple pattern minimize
				 * search area because of specifying that the first subject
				 * variable (?subject0) is of type cretin class )
				 * 
				 * And then add the prefiexedPropertyName and ?var to be used in
				 * filterCondition
				 */
				queryBuilder.append("?subject" + subjectCount + " a " + prefixedSubjectClassName + " ; \n");
				queryBuilder.append(prefixedPropertyName + "  " + "?var" + variableCount + " . \n");

				/*
				 * add filer conditions based on values
				 */
				for (Object value : valueList) {

					/*
					 * the first condition so do not add || at the badging of
					 * the condition
					 */
					if (start) {

						if (value instanceof String) {
							filterConditionsBuilder.append("LCASE( " + "?var" + variableCount + ") = \""
									+ value.toString().toLowerCase() + "\"");
						} else {
							filterConditionsBuilder.append(
									"LCASE( " + "?var" + variableCount + ") = " + value.toString().toLowerCase());
						}

						start = false;

					} else {

						/*
						 * add || at the beginning of the condition
						 */
						if (value instanceof String) {
							filterConditionsBuilder.append("|| \n LCASE( " + "?var" + variableCount + ") = \""
									+ value.toString().toLowerCase() + "\"");
						} else {
							filterConditionsBuilder.append(
									"|| \n LCASE( " + "?var" + variableCount + ") = " + value.toString().toLowerCase());
						}
					}

				}

				/*
				 * increment counters
				 */
				subjectCount++;
				variableCount++;

			}

		}

		filterConditionsBuilder.append(" )");

		/*
		 * complete end of the subquery structure
		 */
		queryBuilder.append(" " + filterConditionsBuilder.toString() + " \n }} ");

		return queryBuilder.toString();
	}

	// public String constructUniqueContstraintCheckSubQueryStr3(
	// LinkedHashMap<String, LinkedHashMap<String, ArrayList<PropertyValue>>>
	// htblUniquePropValueList,
	// String mainClassPrefixedName, String mainInstanceUniqueIdentifier) {
	//
	// /*
	// * main builder for dynamically building the query
	// */
	// StringBuilder queryBuilder = new StringBuilder();
	//
	// /*
	// * filterConditionsBuilder is for building filter conditions
	// */
	// StringBuilder filterConditionsBuilder = new StringBuilder();
	//
	// /*
	// * endGraphPatternBuilder is for building other graph nodes and patterns
	// * and add them to the end of the main graph pattern
	// */
	// StringBuilder endGraphPatternBuilder = new StringBuilder();
	//
	// /*
	// * Getting propertyList of the main subject(this list constructs the
	// * main graph pattern)
	// */
	// ArrayList<PropertyValue> currentClassPropertyValueList =
	// htblUniquePropValueList.get(mainClassPrefixedName)
	// .get(mainInstanceUniqueIdentifier);
	//
	// /*
	// * htblPropValue has key prefiexedPropertyName and value
	// * valueOfProperty.
	// *
	// * It is used to avoid duplicating same triple patterns eg: mbox when
	// * person has more the one email so I want to add foaf:mbox ?var only
	// * one time for this instance
	// */
	// Hashtable<String, Object> htblPropValue = new Hashtable<>();
	//
	// /*
	// * start of the subQuery
	// */
	// queryBuilder.append("{ SELECT (COUNT(*) as ?isUnique ) WHERE { \n");
	//
	// /*
	// * start of the query graph patterns (this triple pattern minimize
	// * search area because of specifing that the first subject variable
	// * (?subject0) is of type certin class )
	// */
	// queryBuilder.append("?subject0" + " a " + mainClassPrefixedName);
	//
	// /*
	// * counters that are used to assign different variables .
	// *
	// * They must be arrays of integers in order to be able to pass by
	// * reference
	// */
	// int[] vairableNum = { 0 };
	// int[] subjectNum = { 1 };
	//
	// /*
	// * iterating on the propertyList of the main subject to construct the
	// * main graph pattern and break through values recursively to construct
	// * other graph nodes and patterns using endGraphPatternBuilder
	// */
	// for (PropertyValue propertyValue : currentClassPropertyValueList) {
	//
	// helper(htblUniquePropValueList, queryBuilder, filterConditionsBuilder,
	// endGraphPatternBuilder,
	// mainClassPrefixedName, mainInstanceUniqueIdentifier, propertyValue,
	// htblPropValue, vairableNum,
	// subjectNum);
	//
	// // if (propertyValue.isObject()) {
	// // subjectNum[0]++;
	// // } else {
	// // if (!htblPropValue.containsKey(propertyValue.getPropertyName()))
	// // vairableNum[0]++;
	// // }
	//
	// }
	//
	// /*
	// * end of the main graph pattern
	// */
	// // queryBuilder.append(" . \n");
	//
	// /*
	// * Appending the endGraphPatternBuilder to the end of the queryBuilder
	// */
	// queryBuilder.append(endGraphPatternBuilder.toString());
	//
	// filterConditionsBuilder.append(" )");
	//
	// /*
	// * complete end of the subquery structure
	// */
	// queryBuilder.append(" " + filterConditionsBuilder.toString() + " \n }}
	// ");
	//
	// return queryBuilder.toString();
	// }
	//
	// /*
	// * A recursive method that construct the uniqueConstraintCheck query
	// *
	// * it takes the reference of
	// * htblUniquePropValueList,queryBuilder,filterConditionsBuilder and
	// * endGraphPatternBuilder to construct query
	// *
	// * It also recursively take propertyValue and currentClassPrefixedName to
	// * breakdown all values and construct a proper graph patterns and filter
	// in
	// * the query
	// *
	// */
	// public void helper(LinkedHashMap<String, LinkedHashMap<String,
	// ArrayList<PropertyValue>>> htblUniquePropValueList,
	// StringBuilder queryBuilder, StringBuilder filterConditionsBuilder,
	// StringBuilder endGraphPatternBuilder,
	// String currentClassPrefixedName, String
	// currentClassInstanceUniqueIdentifier, PropertyValue propertyValue,
	// Hashtable<String, Object> htblPropValue, int[] vairableNum, int[]
	// subjectNum) {
	//
	// // System.out.println("hessss-->" + propertyValue.getPropertyName() + "
	// // "
	// // +
	// //
	// htblUniquePropValueList.get(currentClassPrefixedName).get(currentClassInstanceUniqueIdentifier)
	// // .indexOf(propertyValue)
	// // + " "
	// // +
	// //
	// htblUniquePropValueList.get(currentClassPrefixedName).get(currentClassInstanceUniqueIdentifier).size()
	// // + " " + currentClassPrefixedName + " " +
	// // currentClassInstanceUniqueIdentifier);
	// /*
	// * The property is an objectProperty and the value is a nestedObject(new
	// * class object instance that has its own properties and values)
	// */
	// if (propertyValue.isObject()) {
	//
	// /*
	// * add property and reference to graph node the represent the object
	// * value node
	// *
	// * eg. SELECT (COUNT(*) as ?isUnique ) WHERE { ?subject0 a
	// * iot-platform:Developer ; foaf:knows ?subject1. ?subject1 a
	// * foaf:Person.
	// *
	// * ?subject1 is the object node reference and is linked to main node
	// * (?subject0) with objectProperty (foaf:knows)
	// */
	//
	// if
	// (htblUniquePropValueList.get(currentClassPrefixedName).get(currentClassInstanceUniqueIdentifier)
	// .indexOf(propertyValue) <
	// htblUniquePropValueList.get(currentClassPrefixedName)
	// .get(currentClassInstanceUniqueIdentifier).size() - 1) {
	// queryBuilder.append(" ; \n" + propertyValue.getPropertyName() + "
	// ?subject" + subjectNum[0]);
	// } else {
	// /*
	// * it is the last property so we will end the previous triple
	// * pattern with ; and this one with .
	// */
	// queryBuilder.append(" ; \n" + propertyValue.getPropertyName() + "
	// ?subject" + subjectNum[0] + " . \n");
	//
	// }
	//
	// /*
	// * get the value classType which is stored in the
	// * prefixedObjectValueClassName of the propertyValue this classType
	// * represent the prefiexedClassName of the value class type and it
	// * was added by RequestValidation class
	// */
	// String objectClassTypeName =
	// propertyValue.getPrefixedObjectValueClassName();
	//
	// /*
	// * get the uniqueIdentifer of the objectProperty inOrder to
	// * breakDown the nestedObject to construct Recursively the query
	// */
	// String objectVaueUniqueIdentifier = propertyValue.getValue().toString();
	//
	// /*
	// * get the objectValueInstance's propertyValueList
	// */
	// ArrayList<PropertyValue> propertyValueList =
	// htblUniquePropValueList.get(objectClassTypeName)
	// .get(objectVaueUniqueIdentifier);
	//
	// /*
	// * is for building other graph nodes and patterns and add them to
	// * the end of the main graph pattern ( graph pattern with node
	// * representing the subjectClassInstance of the nestedObjectValue )
	// */
	// StringBuilder tempBuilder = new StringBuilder();
	//
	// /*
	// * start the new graph pattern that will be added to the end
	// */
	// tempBuilder.append("?subject" + subjectNum[0] + " a " +
	// objectClassTypeName);
	// subjectNum[0]++;
	//
	// /*
	// * objectValueHtblPropValue is the same htblPropValue but for this
	// * objectValueInstance I doing this to keep every instance
	// * independent from other instance even if more than one instance
	// * have the same ontology class type
	// *
	// * objectValueHtblPropValue has key prefiexedPropertyName and value
	// * valueOfProperty.
	// *
	// * It is used to avoid duplicating same triple patterns eg: mbox
	// * when person has more the one email so I want to add foaf:mbox
	// * ?var only one time for this instance
	// */
	// Hashtable<String, Object> objectValueHtblPropValue = new Hashtable<>();
	//
	// for (PropertyValue objectPropertyValue : propertyValueList) {
	//
	// helper(htblUniquePropValueList, tempBuilder, filterConditionsBuilder,
	// endGraphPatternBuilder,
	// objectClassTypeName, objectVaueUniqueIdentifier, objectPropertyValue,
	// objectValueHtblPropValue,
	// vairableNum, subjectNum);
	// }
	//
	// /*
	// * append tempBuilder to endGraphPatternBuilder to add the
	// * nestedObject Patterns to the end of its main patterns (above
	// * graph node that has relation to nestedObject node)
	// */
	// endGraphPatternBuilder.append(tempBuilder.toString());
	//
	// /*
	// * ReIntialize tempBuilder to remove all what was builded on it to
	// * be used again
	// */
	// tempBuilder = new StringBuilder();
	//
	// } else {
	//
	// /*
	// * The property is not an objectProperty it is property that has a
	// * literal value (the value can be datatype value (eg.
	// * string,int,float) or a reference to an existed object instance
	// */
	// if
	// (htblUniquePropValueList.get(currentClassPrefixedName).get(currentClassInstanceUniqueIdentifier)
	// .indexOf(propertyValue) <
	// htblUniquePropValueList.get(currentClassPrefixedName)
	// .get(currentClassInstanceUniqueIdentifier).size() - 1) {
	//
	// /*
	// * Check if the prefixedPropertyName of this instance was added
	// * before
	// */
	// if (!htblPropValue.containsKey(propertyValue.getPropertyName())) {
	//
	// /*
	// * if we add a new graph pattern (by checking if the
	// * prefixedPropertyName was not added before for this
	// * current instance) then increment the variableNum[0]
	// *
	// * add prifixedPropertyName to htblPropValue to avoid
	// * duplicating it again for this instance
	// *
	// * I do this after adding filter condition and graph pattern
	// * to maintain the same variableNames between graph pattern
	// * added and condtion
	// */
	// if (!htblPropValue.containsKey(propertyValue.getPropertyName())) {
	// htblPropValue.put(propertyValue.getPropertyName(), "?var" +
	// vairableNum[0]);
	// vairableNum[0]++;
	// }
	//
	// /*
	// * it is not the last property so we will end the previous
	// * triple pattern with ;
	// */
	// queryBuilder.append(" ; \n" + propertyValue.getPropertyName() + " "
	// + htblPropValue.get(propertyValue.getPropertyName()));
	//
	// }
	//
	// } else {
	// if (!htblPropValue.containsKey(propertyValue.getPropertyName())) {
	//
	// /*
	// * if we add a new graph pattern (by checking if the
	// * prefixedPropertyName was not added before for this
	// * current instance) then increment the variableNum[0]
	// *
	// * add prifixedPropertyName to htblPropValue to avoid
	// * duplicating it again for this instance
	// *
	// * I do this after adding filter condition and graph pattern
	// * to maintain the same variableNames between graph pattern
	// * added and condtion
	// */
	// if (!htblPropValue.containsKey(propertyValue.getPropertyName())) {
	// htblPropValue.put(propertyValue.getPropertyName(), "?var" +
	// vairableNum[0]);
	// vairableNum[0]++;
	// }
	//
	// /*
	// * it is the last property so we will end the previous
	// * triple pattern with ; and this one with .
	// */
	// queryBuilder.append(" ; \n" + propertyValue.getPropertyName() + " "
	// + htblPropValue.get(propertyValue.getPropertyName()) + " . \n");
	//
	// } else {
	//
	// /*
	// * end the previous triple pattern with .
	// *
	// * Because after checking it was found that the end
	// * prefixedPropertyName was added before to this instance's
	// * graph patterns
	// */
	// queryBuilder.append(" . \n");
	// }
	//
	// }
	//
	// if (filterConditionsBuilder.length() == 0) {
	//
	// /*
	// * start of filter part
	// */
	// filterConditionsBuilder.append(" FILTER ( ");
	//
	// /*
	// * I will add condition for every value no duplicate removing
	// * here even if there are more than value for a property of the
	// * same instance
	// *
	// * eg. foaf:mbox ?var0 ; FILTER ( || LCASE( ?var0) =
	// * "haytham.ismails@gmail.com" || LCASE( ?var0) =
	// * "haytham.ismails@student.guc.edu.eg")
	// *
	// * check is the value is a string value to make add ""
	// */
	//
	// if (propertyValue.getPropertyName() instanceof String) {
	// filterConditionsBuilder.append(" \n LCASE( " +
	// htblPropValue.get(propertyValue.getPropertyName())
	// + ") = \"" + propertyValue.getValue().toString().toLowerCase() + "\"");
	// } else {
	// filterConditionsBuilder.append(" \n LCASE( " +
	// htblPropValue.get(propertyValue.getPropertyName())
	// + ") = " + propertyValue.getValue().toString().toLowerCase());
	// }
	// } else {
	// if (propertyValue.getPropertyName() instanceof String) {
	// filterConditionsBuilder.append("|| \n LCASE( " +
	// htblPropValue.get(propertyValue.getPropertyName())
	// + ") = \"" + propertyValue.getValue().toString().toLowerCase() + "\"");
	// } else {
	// filterConditionsBuilder.append("|| \n LCASE( " +
	// htblPropValue.get(propertyValue.getPropertyName())
	// + ") = " + propertyValue.getValue().toString().toLowerCase());
	// }
	// }
	// }
	//
	// }

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
