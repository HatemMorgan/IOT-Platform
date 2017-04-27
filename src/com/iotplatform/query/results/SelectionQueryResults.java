package com.iotplatform.query.results;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.daos.DynamicOntologyDao;
import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.XSDDatatype;
import com.iotplatform.ontology.dynamicConcepts.DynamicConceptsUtility;
import com.iotplatform.ontology.mapers.DynamicOntologyMapper;
import com.iotplatform.ontology.mapers.OntologyMapper;
import com.iotplatform.utilities.QueryVariable;

/*
 * SelectionQueryResults is used to construct the appropriate query result the will be returned to the users
 * 
 *  1- remove all repeated patterns 
 *  2- remove nulls 
 *  3- nested object to appear in the appropriate way that is expected by the user
 *  4- remove URIs and prefixes to fully abstract semantic web field
 */

@Component
public class SelectionQueryResults {

	// private DynamicConceptsUtility dynamicPropertiesUtility;
	private DynamicOntologyDao dynamicOntologyDao;

	@Autowired
	public SelectionQueryResults(DynamicOntologyDao dynamicOntologyDao) {
		this.dynamicOntologyDao = dynamicOntologyDao;
	}

	/*
	 * constructQueryResult method used to return results without any prefixed
	 * ontology URIs
	 */

	private Object[] constructSinglePropertyValuePair(String propertyURI, Object value, Class subjectClass,
			String applicationModelName) {
		Object[] res = new Object[2];

		String propertyName = subjectClass.getHtblPropUriName().get(propertyURI);

		if (propertyName == null) {

			ArrayList<String> classList = new ArrayList<>();
			classList.add(subjectClass.getName());

			for (Class superClass : subjectClass.getSuperClassesList()) {
				classList.add(superClass.getName());
			}

			dynamicOntologyDao.loadAndCacheDynamicClassesofApplicationDomain(applicationModelName, classList);

			subjectClass = DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
					.get(subjectClass.getName().toLowerCase());
			propertyName = subjectClass.getHtblPropUriName().get(propertyURI);
		}

		Property property = subjectClass.getProperties().get(propertyName);

		if (property instanceof ObjectProperty) {
			value = value.toString().substring(Prefix.IOT_PLATFORM.getUri().length(), value.toString().length());
		} else {
			/*
			 * datatype property
			 */
			value = typeCastValueToItsDataType((DataTypeProperty) property, value);
		}

		res[0] = propertyName;
		res[1] = value;

		return res;
	}

	private static Object typeCastValueToItsDataType(DataTypeProperty dataTypeProperty, Object value) {
		if (XSDDatatype.boolean_type.equals(dataTypeProperty.getDataType())) {
			return Boolean.parseBoolean(value.toString());
		}

		if (XSDDatatype.decimal_typed.equals(dataTypeProperty.getDataType())) {
			return Double.parseDouble(value.toString());
		}

		if (XSDDatatype.float_typed.equals(dataTypeProperty.getDataType())) {
			return Float.parseFloat(value.toString());
		}

		if (XSDDatatype.integer_typed.equals(dataTypeProperty.getDataType())) {
			return Integer.parseInt(value.toString());
		}

		if (XSDDatatype.string_typed.equals(dataTypeProperty.getDataType())) {
			return value.toString();
		}

		if (XSDDatatype.dateTime_typed.equals(dataTypeProperty.getDataType())) {
			return Date.parse(value.toString());
		}

		if (XSDDatatype.double_typed.equals(dataTypeProperty.getDataType())) {
			return Double.parseDouble(value.toString());
		}
		return null;
	}
	/*
	 * constractResponeJsonObject method is responsible take two inputs
	 * 
	 * 1-The ResultSet returned from querying the application model and take the
	 * 2-subjectClass to check if the property returned has multiple values or
	 * not. If it has multiple value it should be returned as an arrayList
	 */

	public List<LinkedHashMap<String, Object>> constractResponeJsonObjectForListSelection(ResultSet results,
			Class subjectClass, String applicationModelName) throws SQLException {

		List<LinkedHashMap<String, Object>> responseJson = new ArrayList<>();

		LinkedHashMap<Object, LinkedHashMap<String, Object>> temp = new LinkedHashMap<>();

		Hashtable<String, Property> subjectClassProperties = subjectClass.getProperties();

		while (results.next()) {

			Object subject = results.getObject(1);

			/*
			 * create a new hashtable to hold subject's property and value
			 */

			if (temp.size() == 0) {
				LinkedHashMap<String, Object> htblSubjectPropVal = new LinkedHashMap<>();
				temp.put(subject, htblSubjectPropVal);
				responseJson.add(htblSubjectPropVal);
			}

			// skip rdf:type property

			if (results.getString(2).equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
				continue;
			}

			Object[] preparedPropVal = constructSinglePropertyValuePair(results.getString(2), results.getString(3),
					subjectClass, applicationModelName);

			String propertyName = preparedPropVal[0].toString();
			Object value = preparedPropVal[1];

			/*
			 * check if the property passed is a multiValues Property it is
			 * after calling constructSinglePropertyValuePair method because
			 * constructSinglePropertyValuePair checks if the property was
			 * cached or need to be cached
			 */

			Property property = subjectClassProperties.get(propertyName);

			if (property.isMulitpleValues()) {

				/*
				 * check if the property was added before to use the previous
				 * created value array
				 */

				if (temp.containsKey(subject)) {

					if (temp.get(subject).containsKey(propertyName)) {

						((ArrayList) temp.get(subject).get(propertyName)).add(value);
					} else {

						/*
						 * property was not added before so create a new
						 * arraylist of objects to hold values and add it to
						 * multipleValuePropNameValueArr hashtable
						 */

						ArrayList<Object> valueList = new ArrayList<>();
						valueList.add(value);
						temp.get(subject).put(propertyName, valueList);
					}
				} else {
					LinkedHashMap<String, Object> htblAdminPropVal = new LinkedHashMap<>();
					temp.put(subject, htblAdminPropVal);

					if (temp.get(subject).containsKey(propertyName)) {

						((ArrayList) temp.get(subject).get(propertyName)).add(value);
					} else {

						/*
						 * property was not added before so create a new
						 * arraylist of objects to hold values and add it to
						 * multipleValuePropNameValueArr hashtable
						 */

						ArrayList<Object> valueList = new ArrayList<>();
						valueList.add(value);
						temp.get(subject).put(propertyName, valueList);
					}
					responseJson.add(htblAdminPropVal);
				}

			} else {

				/*
				 * as long as the current subject equal to subject got from the
				 * results then add the property and value to the hashtable . If
				 * they are not the same this means that this is a new subject
				 * so we have to construct a new hashtable to hold its data
				 */

				if (temp.containsKey(subject)) {
					temp.get(subject).put(propertyName, value);
				} else {

					LinkedHashMap<String, Object> htblAdminPropVal = new LinkedHashMap<>();
					temp.put(subject, htblAdminPropVal);
					temp.get(subject).put(propertyName, value);
					responseJson.add(htblAdminPropVal);

				}
			}
		}
		return responseJson;
	}

	public static List<LinkedHashMap<String, Object>> constructQueryResult(String applicationName, ResultSet results,
			String requestClassName, Hashtable<String, QueryVariable> htblSubjectVariables) {

		/*
		 * consturctedQueryResult is the list of constructed query result where
		 * each element in the list represent a Hashtable<String, Object> which
		 * contains the propValues of an individual
		 */
		List<LinkedHashMap<String, Object>> consturctedQueryResult = new ArrayList<>();

		/*
		 * htblIndividualQueryVariabesList is used to hold uniqueIdentifier of
		 * an individual (key) and a list of queryVariables of individual in its
		 * subjectVariavle list
		 * 
		 * An individual can be repeated in more than one queryVariable but I
		 * only want to remove duplicated results of the same queryVariable
		 * because it was only repeated due to graph traversing(it has nothing
		 * of what query done)
		 * 
		 * The replication I am removing happening due to projection of results
		 * from traversing graph
		 */
		LinkedHashMap<String, ArrayList<String>> htblIndividualQueryVariabesList = null;

		/*
		 * htblSubjectVariablehtblpropVal holds the subjectVariable as key and
		 * its list of individuals as value
		 */
		Hashtable<String, Object> htblSubjectVariablehtblpropVal = null;

		/*
		 * htblindividuals holds the individual uniqueIdentifier as key and the
		 * Hashtable<String,Object> that hold the propValues as value of the key
		 */
		Hashtable<String, Hashtable<String, Object>> htblindividuals = new Hashtable<>();

		try {

			ResultSetMetaData rsmd = results.getMetaData();

			int columnsNumber = rsmd.getColumnCount();
			while (results.next()) {

				/*
				 * SUBJECT0 must be always the first column
				 */
				String mainSubjectUri = results.getString("SUBJECT0");

				/*
				 * check if the mainSubjectUri(which represents an individual)
				 * exist before or not in htblIndividualSubjectIndex
				 */
				if (!htblindividuals.containsKey(mainSubjectUri)) {

					/*
					 * Initialize htblIndividualQueryVariabesList which is used
					 * to hold uniqueIdentifier of an individual (key) and
					 * queryVariable of individual in its subjectVariavle list
					 * 
					 */
					htblIndividualQueryVariabesList = new LinkedHashMap<>();

					/*
					 * construct new data structures instances to hold data of
					 * the new individual
					 */
					htblSubjectVariablehtblpropVal = new Hashtable<>();

					LinkedHashMap<String, Object> htblIndividualPropertyValue = new LinkedHashMap<>();

					htblSubjectVariablehtblpropVal.put("subject0", htblIndividualPropertyValue);

					/*
					 * add htblIndividualPropertyValue to
					 * consturctedQueryResult(the returned result)
					 */
					consturctedQueryResult.add(htblIndividualPropertyValue);

					/*
					 * add htblIndividualPropertyValue to htblindividuals to
					 * avoid creating a new Hashtable<String, Object> if it was
					 * repeated
					 */
					htblindividuals.put(mainSubjectUri, htblSubjectVariablehtblpropVal);

				} else {
					/*
					 * mainSubjectUri was added before so get its
					 * Hashtable<String, Object>
					 */
					htblSubjectVariablehtblpropVal = htblindividuals.get(mainSubjectUri);
				}

				/*
				 * iterate over the row columns and start from i=2 to skip the
				 * first column (subject0)
				 */
				for (int i = 2; i <= columnsNumber; i++) {
					String columnName = rsmd.getColumnName(i).toLowerCase();
					Object propValue = results.getObject(i);

					/*
					 * skip if propValue = null
					 */
					if (propValue == null)
						continue;

					if (columnName.contains("subject")) {

						/*
						 * get value of subject variable column which is the
						 * uniqueIdentifier of the individual (subject of the
						 * individual)
						 */
						String individualUniqueIdentifier = results.getString(columnName);

						/*
						 * call constructResultOfSubjectColumn which will
						 * construct result for subject variables
						 */
						constructResultOfSubjectColumn(columnName, htblIndividualQueryVariabesList,
								htblSubjectVariablehtblpropVal, htblSubjectVariables, individualUniqueIdentifier);
					} else {
						/*
						 * check that it is variable
						 */
						if (columnName.contains("var")) {

							/*
							 * Call constructResultOfVarColumn which will
							 * construct result for var variables
							 */
							constructResultOfVarColumn(columnName, htblSubjectVariables, htblSubjectVariablehtblpropVal,
									propValue);

						} else {

							/*
							 * it will be object and ObjectclassType which hold
							 * the results of the results of classTypes that
							 * were not added to values field in query request
							 * body
							 * 
							 * check if the columnName is object variable which
							 * will hold the subject (uniqueIdentfier) of the
							 * individual
							 */
							if (columnName.contains("object") && !columnName.contains("objecttype")) {

								/*
								 * get value of subject variable column which is
								 * the uniqueIdentifier of the objectValue
								 * individual (subject of the individual)
								 */
								String objectUniqueIdentifier = results.getString(columnName);

								/*
								 * get objectType from the results.
								 * 
								 * Increment i to get the next column value
								 * which has the type of objectValue
								 */
								i++;
								String objectClassTypeURI = results.getObject(i).toString();
								/*
								 * check if objectUniqueIdentifier was seen
								 * before or it is the first time
								 */
								if (htblIndividualQueryVariabesList.containsKey(objectUniqueIdentifier)) {

									/*
									 * check that this objectUniqueIdentifier
									 * was not repeated for the same columnName
									 * 
									 * If it was repeated for the same
									 * columnName(queryVariable) so it is
									 * replicating due to projection after
									 * traversing graph
									 * 
									 * If it was repeated but for another
									 * columnName(queryVariable) so I have to
									 * add it to constructed results. Therefore
									 * I called
									 * constructResultOfSubjectColumnHelper to
									 * add it
									 */
									if (!htblIndividualQueryVariabesList.get(objectUniqueIdentifier)
											.contains(columnName)) {

										/*
										 * call constructResultsOfObjectColumn
										 * which will construct result for
										 * object and objectType variables
										 */
										constructResultsOfObjectColumn(columnName, objectUniqueIdentifier,
												objectClassTypeURI, htblSubjectVariables,
												htblSubjectVariablehtblpropVal);

										/*
										 * add new columnName (queryVariable) to
										 * list of queryVariables of this
										 * individualUniqueIdentifier in
										 * htblIndividualQueryVariabesList
										 */
										htblIndividualQueryVariabesList.get(objectUniqueIdentifier).add(columnName);
									} else {
										/*
										 * individual is repeated for the same
										 * columnName(queryVariable) so I have
										 * to skip it
										 */
										continue;
									}

								} else {
									/*
									 * call constructResultsOfObjectColumn which
									 * will construct result for object and
									 * objectType variables
									 */
									constructResultsOfObjectColumn(columnName, objectUniqueIdentifier,
											objectClassTypeURI, htblSubjectVariables, htblSubjectVariablehtblpropVal);

									/*
									 * add objectUniqueIdentifier to
									 * htblIndividualQueryVariabesList and
									 * create a new list to hold its
									 * queryVariables
									 */
									htblIndividualQueryVariabesList.put(objectUniqueIdentifier, new ArrayList<>());
									/*
									 * add new columnName (queryVariable) to
									 * list of queryVariables of this
									 * individualUniqueIdentifier in
									 * htblIndividualQueryVariabesList
									 */
									htblIndividualQueryVariabesList.get(objectUniqueIdentifier).add(columnName);
								}
							} else {

								if (columnName.contains("objecttype")) {

									constructResultOfObjectTypeColumn(columnName, propValue, htblSubjectVariables,
											htblSubjectVariablehtblpropVal);
								}

							}

						}
					}

				}
			}
		} catch (

		SQLException e) {
			throw new DatabaseException(e.getMessage(), requestClassName);
		}
		System.out.println(consturctedQueryResult);
		return consturctedQueryResult;
	}

	/*
	 * constructResultOfSubjectColumn method will construct result for subject
	 * variables. This method is called by constructQueryResult
	 */
	private static void constructResultOfSubjectColumn(String columnName,
			LinkedHashMap<String, ArrayList<String>> htblIndividualQueryVariabesList,
			Hashtable<String, Object> htblSubjectVariablehtblpropVal,
			Hashtable<String, QueryVariable> htblSubjectVariables, String individualUniqueIdentifier) {
		/*
		 * get queryVariable of the subjectVariable to know its propertyName
		 */
		QueryVariable queryVariable = htblSubjectVariables.get(columnName);
		String propertyName = queryVariable.getPropertyName();

		/*
		 * get parrentSubjectVariable of the objectVariable (columnName)
		 */
		String parrentSubjectVariable = queryVariable.getSubjectVariableName();

		/*
		 * get propertyMapping of propertyName that connects subjectVariable to
		 * objectVariable (columnName)
		 */
		String classUri = queryVariable.getSubjectClassUri();
		Class propertyClass = OntologyMapper.getHtblMainOntologyClassesUriMappers().get(classUri);
		Property property = propertyClass.getProperties().get(propertyName);

		/*
		 * check if the individual was added before or not
		 */
		if (!htblSubjectVariablehtblpropVal.containsKey(columnName)) {

			/*
			 * create a new ArrayList<Hashtable<String, Object>> to hold the
			 * individuals of this subjectVariable
			 */
			ArrayList<LinkedHashMap<String, Object>> htblSubjectIndividualsList = new ArrayList<>();

			/*
			 * create a new Hashtable<String, Object> to hold individuals
			 * propValues
			 */
			LinkedHashMap<String, Object> subjectIndividual = new LinkedHashMap<>();

			/*
			 * add it to htblSubjectIndividualsList
			 */
			htblSubjectIndividualsList.add(subjectIndividual);

			/*
			 * add individualUniqueIdentifier to htblIndividualQueryVariabesList
			 * and create a new list to hold its queryVariables
			 */
			htblIndividualQueryVariabesList.put(individualUniqueIdentifier, new ArrayList<>());
			/*
			 * add new columnName (queryVariable) to list of queryVariables of
			 * this individualUniqueIdentifier in
			 * htblIndividualQueryVariabesList
			 */
			htblIndividualQueryVariabesList.get(individualUniqueIdentifier).add(columnName);

			/*
			 * add new subjectVariable's individualsList to
			 * htblSubjectVariablehtblpropVal
			 */
			htblSubjectVariablehtblpropVal.put(columnName, htblSubjectIndividualsList);

			/*
			 * get list of individuals of the parentSubjectVariable
			 * 
			 * if the parentSubjectVairable is subject0 it will be only one
			 * individual so I will cast it to Hashtable<String,Object>
			 * 
			 * else it will be a list of individuals so I will cast it to
			 * ArrayList<Hashtable<String,Object>>
			 */
			LinkedHashMap<String, Object> parentSubjectVariableIndividual;
			if (parrentSubjectVariable.equals("subject0")) {

				parentSubjectVariableIndividual = (LinkedHashMap<String, Object>) htblSubjectVariablehtblpropVal
						.get(parrentSubjectVariable);
			} else {
				ArrayList<LinkedHashMap<String, Object>> individualsList = (ArrayList<LinkedHashMap<String, Object>>) htblSubjectVariablehtblpropVal
						.get(parrentSubjectVariable);
				parentSubjectVariableIndividual = individualsList.get(individualsList.size() - 1);
			}

			/*
			 * check if the property has multipleValues in order to add property
			 * to parentSubjectVariableIndividual with a value list (to hold
			 * multiple values)
			 */
			if (property.isMulitpleValues()) {

				/*
				 * check if the property was added before or not
				 */
				if (!parentSubjectVariableIndividual.containsKey(propertyName)) {

					/*
					 * create a new ArrayList<Hashtable<String, Object>> to hold
					 * objectValues
					 */
					ArrayList<LinkedHashMap<String, Object>> valueList = new ArrayList<>();

					/*
					 * add subjectIndividual to propertyValueList (valueList)
					 */
					valueList.add(subjectIndividual);

					/*
					 * add property and its values list to
					 * parentSubjectVariableIndividual
					 */
					parentSubjectVariableIndividual.put(propertyName, valueList);

				} else {

					/*
					 * if the property existed before it means that this
					 * subjectVariable individual with
					 * individualUniqueIdentifier is another value to be added
					 * to valueList
					 * 
					 * get the propertyValueList
					 */
					ArrayList<LinkedHashMap<String, Object>> valueList = (ArrayList<LinkedHashMap<String, Object>>) parentSubjectVariableIndividual
							.get(propertyName);

					/*
					 * add subjectIndividual to propertyValueList (valueList)
					 */
					valueList.add(subjectIndividual);
				}

			} else {

				/*
				 * check if the property was added before or not
				 */
				if (!parentSubjectVariableIndividual.containsKey(propertyName)) {

					/*
					 * add property and subjectIndividual to
					 * parentSubjectVariableIndividual
					 */
					parentSubjectVariableIndividual.put(propertyName, subjectIndividual);

				} else {

					/*
					 * it will never happen but I will skip if it happens.
					 * 
					 * It will never happen that a property with a single value
					 * has multiple values because I checked that it never
					 * happen in insertion
					 */
					return;
				}

			}

		} else {

			/*
			 * subjectVariable has been seen before.
			 * 
			 * Check if the individual with individualUniqueIdentifier was seen
			 * before or not
			 */
			if (!htblIndividualQueryVariabesList.containsKey(individualUniqueIdentifier)) {

				constructResultOfSubjectColumnHelper(columnName, htblIndividualQueryVariabesList,
						individualUniqueIdentifier, parrentSubjectVariable, htblSubjectVariablehtblpropVal, property,
						propertyName);

				/*
				 * add individualUniqueIdentifier to
				 * htblIndividualQueryVariabesList and create a new list to hold
				 * its queryVariables
				 */
				htblIndividualQueryVariabesList.put(individualUniqueIdentifier, new ArrayList<>());
				/*
				 * add new columnName (queryVariable) to list of queryVariables
				 * of this individualUniqueIdentifier in
				 * htblIndividualQueryVariabesList
				 */
				htblIndividualQueryVariabesList.get(individualUniqueIdentifier).add(columnName);

			} else {

				/*
				 * check that this individualUniqueIdentifier was not repeated
				 * for the same columnName
				 * 
				 * If it was repeated for the same columnName(queryVariable) so
				 * it is replicating due to projection after traversing graph
				 * 
				 * If it was repeated but for another columnName(queryVariable)
				 * so I have to add it to constructed results. Therefore I
				 * called constructResultOfSubjectColumnHelper to add it
				 */
				if (!htblIndividualQueryVariabesList.get(individualUniqueIdentifier).contains(columnName)) {

					constructResultOfSubjectColumnHelper(columnName, htblIndividualQueryVariabesList,
							individualUniqueIdentifier, parrentSubjectVariable, htblSubjectVariablehtblpropVal,
							property, propertyName);

					/*
					 * add new columnName (queryVariable) to list of
					 * queryVariables of this individualUniqueIdentifier in
					 * htblIndividualQueryVariabesList
					 */
					htblIndividualQueryVariabesList.get(individualUniqueIdentifier).add(columnName);
				} else {

					/*
					 * individual is repeated for the same
					 * columnName(queryVariable) so I have to skip it
					 */
					return;
				}

			}

		}
	}

	/*
	 * constructResultOfSubjectColumnHelper is used to construct results for
	 * subjectVariable
	 */
	private static void constructResultOfSubjectColumnHelper(String columnName,
			LinkedHashMap<String, ArrayList<String>> htblIndividualQueryVariabesList, String individualUniqueIdentifier,
			String parrentSubjectVariable, Hashtable<String, Object> htblSubjectVariablehtblpropVal, Property property,
			String propertyName) {
		/*
		 * get subjectVariableIndividualsList
		 */
		ArrayList<LinkedHashMap<String, Object>> individualsList = (ArrayList<LinkedHashMap<String, Object>>) htblSubjectVariablehtblpropVal
				.get(columnName);

		/*
		 * create new Hashtable<String, Object> to hold individual's
		 * propertyValue
		 */
		LinkedHashMap<String, Object> subjectIndividual = new LinkedHashMap<>();
		individualsList.add(subjectIndividual);

		/*
		 * add individualUniqueIdentifier to htblIndividualQueryVariabesList
		 */
		htblIndividualQueryVariabesList.put(individualUniqueIdentifier, new ArrayList<>());

		/*
		 * get list of individuals of the parentSubjectVariable
		 * 
		 * if the parentSubjectVairable is subject0 it will be only one
		 * individual so I will cast it to Hashtable<String,Object>
		 * 
		 * else it will be a list of individuals so I will cast it to
		 * ArrayList<Hashtable<String,Object>>
		 */
		LinkedHashMap<String, Object> parentSubjectVariableIndividual;
		if (parrentSubjectVariable.equals("subject0")) {

			parentSubjectVariableIndividual = (LinkedHashMap<String, Object>) htblSubjectVariablehtblpropVal
					.get(parrentSubjectVariable);
		} else {
			ArrayList<LinkedHashMap<String, Object>> parentSubjectVariableIndividualsList = (ArrayList<LinkedHashMap<String, Object>>) htblSubjectVariablehtblpropVal
					.get(parrentSubjectVariable);

			/*
			 * get parentSubjectVariableIndividual
			 * 
			 * It will always be the last item in the list because the results
			 * are added in order
			 */
			parentSubjectVariableIndividual = parentSubjectVariableIndividualsList
					.get(parentSubjectVariableIndividualsList.size() - 1);
		}

		/*
		 * check if the property has multipleValues in order to add property to
		 * parentSubjectVariableIndividual with a value list (to hold multiple
		 * values)
		 */
		if (property.isMulitpleValues()) {

			/*
			 * check if the property was added before or not
			 */
			if (!parentSubjectVariableIndividual.containsKey(propertyName)) {

				/*
				 * create a new ArrayList<Hashtable<String, Object>> to hold
				 * objectValues
				 */
				ArrayList<LinkedHashMap<String, Object>> valueList = new ArrayList<>();

				/*
				 * add subjectIndividual to propertyValueList (valueList)
				 */
				valueList.add(subjectIndividual);

				/*
				 * add property and its values list to
				 * parentSubjectVariableIndividual
				 */
				parentSubjectVariableIndividual.put(propertyName, valueList);

			} else {

				/*
				 * if the property existed before it means that this
				 * subjectVariable individual with individualUniqueIdentifier is
				 * another value to be added to valueList
				 * 
				 * get the propertyValueList
				 */
				ArrayList<LinkedHashMap<String, Object>> valueList = (ArrayList<LinkedHashMap<String, Object>>) parentSubjectVariableIndividual
						.get(propertyName);

				/*
				 * add subjectIndividual to propertyValueList (valueList)
				 */
				valueList.add(subjectIndividual);
			}

		} else {

			/*
			 * check if the property was added before or not
			 */
			if (!parentSubjectVariableIndividual.containsKey(propertyName)) {

				/*
				 * add property and subjectIndividual to
				 * parentSubjectVariableIndividual
				 */
				parentSubjectVariableIndividual.put(propertyName, subjectIndividual);

			} else {

				/*
				 * it will never happen but I will skip if it happens.
				 * 
				 * It will never happen that a property with a single value has
				 * multiple values because I checked that it never happen in
				 * insertion
				 */
				return;
			}

		}

	}

	/*
	 * 
	 * constructResultOfVarColumn methpd will construct result for subject
	 * variables. This method is called by constructQueryResult
	 */
	private static void constructResultOfVarColumn(String columnName,
			Hashtable<String, QueryVariable> htblSubjectVariables,
			Hashtable<String, Object> htblSubjectVariablehtblpropVal, Object propValue) {
		/*
		 * get queryVariable of the subjectVariable to know its propertyName
		 */
		QueryVariable queryVariable = htblSubjectVariables.get(columnName);
		String propertyName = queryVariable.getPropertyName();

		/*
		 * get subjectVariable of the objectVariable (columnName)
		 */
		String subjectVariable = queryVariable.getSubjectVariableName();

		/*
		 * get propertyMapping of propertyName that connects subjectVariable to
		 * objectVariable (columnName)
		 */
		String classUri = queryVariable.getSubjectClassUri();
		Class propertyClass = OntologyMapper.getHtblMainOntologyClassesUriMappers().get(classUri);
		Property property = propertyClass.getProperties().get(propertyName);

		/*
		 * type cast propValue to its dataType given by its property
		 * 
		 * The value is returned from database as a String
		 */
		if (property instanceof DataTypeProperty) {
			propValue = typeCastValueToItsDataType((DataTypeProperty) property, propValue);
		} else {
			/*
			 * Object Property remove prefix uri
			 */
			propValue = propValue.toString().substring(Prefix.IOT_PLATFORM.getUri().length(),
					propValue.toString().length());
		}

		/*
		 * get list of individuals of the parentSubjectVariable
		 * 
		 * if the parentSubjectVairable is subject0 it will be only one
		 * individual so I will cast it to Hashtable<String,Object>
		 * 
		 * else it will be a list of individuals so I will cast it to
		 * ArrayList<Hashtable<String,Object>>
		 */
		LinkedHashMap<String, Object> subjectVariableIndividual;
		if (subjectVariable.equals("subject0")) {

			subjectVariableIndividual = (LinkedHashMap<String, Object>) htblSubjectVariablehtblpropVal
					.get(subjectVariable);
		} else {
			ArrayList<LinkedHashMap<String, Object>> subjectVariableIndividualsList = (ArrayList<LinkedHashMap<String, Object>>) htblSubjectVariablehtblpropVal
					.get(subjectVariable);

			/*
			 * get subjectVariableIndividual
			 * 
			 * It will always be the last item in the list because the results
			 * are added in order
			 */
			subjectVariableIndividual = subjectVariableIndividualsList.get(subjectVariableIndividualsList.size() - 1);
		}
		/*
		 * check if the property has multipleValues in order to add property to
		 * subjectVariableIndividual with a value list (to hold multiple values)
		 */
		if (property.isMulitpleValues()) {

			/*
			 * check if the property was added before or not
			 */
			if (!subjectVariableIndividual.containsKey(propertyName)) {

				/*
				 * create a new ArrayList<Object> to hold value
				 */
				ArrayList<Object> valueList = new ArrayList<>();

				/*
				 * add value to propertyValueList (valueList)
				 */
				valueList.add(propValue);

				/*
				 * add property and its values list to subjectVariableIndividual
				 */
				subjectVariableIndividual.put(propertyName, valueList);

			} else {

				/*
				 * if the property existed before it means that this value is
				 * another value to be added to valueList
				 * 
				 * get the propertyValueList (valueList)
				 */
				ArrayList<Object> valueList = (ArrayList<Object>) subjectVariableIndividual.get(propertyName);

				/*
				 * check that the propValue does not exist before in the
				 * valueList
				 */
				if (!valueList.contains(propValue)) {

					/*
					 * add subjectIndividual to propertyValueList (valueList)
					 */
					valueList.add(propValue);
				}
			}

		} else {

			/*
			 * check if the property was added before or not
			 */
			if (!subjectVariableIndividual.containsKey(propertyName)) {

				/*
				 * add property and value to subjectVariableIndividual
				 */
				subjectVariableIndividual.put(propertyName, propValue);

			} else {

				/*
				 * it will never happen but I will skip if it happens.
				 * 
				 * It will never happen that a property with a single value has
				 * multiple values because I checked that it never happen in
				 * insertion
				 */
				return;
			}

		}

	}

	/*
	 * constructResultsOfObjectColumn method will construct result for object
	 * and objectType variables
	 */
	private static void constructResultsOfObjectColumn(String objectColumnName, String objectUniqueIdentifier,
			String objectClassTypeURI, Hashtable<String, QueryVariable> htblSubjectVariables,
			Hashtable<String, Object> htblSubjectVariablehtblpropVal) {
		/*
		 * get queryVariable of the subjectVariable to know its propertyName
		 */
		QueryVariable queryVariable = htblSubjectVariables.get(objectColumnName);
		String propertyName = queryVariable.getPropertyName();

		/*
		 * get parrentSubjectVariable of the objectVariable (columnName)
		 */
		String parrentSubjectVariable = queryVariable.getSubjectVariableName();

		/*
		 * get propertyMapping of propertyName that connects subjectVariable to
		 * objectVariable (columnName)
		 */
		String classUri = queryVariable.getSubjectClassUri();
		Class propertyClass = OntologyMapper.getHtblMainOntologyClassesUriMappers().get(classUri);
		Property property = propertyClass.getProperties().get(propertyName);

		/*
		 * get list of individuals of the parentSubjectVariable
		 * 
		 * if the parentSubjectVairable is subject0 it will be only one
		 * individual so I will cast it to Hashtable<String,Object>
		 * 
		 * else it will be a list of individuals so I will cast it to
		 * ArrayList<Hashtable<String,Object>>
		 */
		LinkedHashMap<String, Object> parentSubjectVariableIndividual;
		int index = 0;
		if (parrentSubjectVariable.equals("subject0")) {

			parentSubjectVariableIndividual = (LinkedHashMap<String, Object>) htblSubjectVariablehtblpropVal
					.get(parrentSubjectVariable);
		} else {
			ArrayList<LinkedHashMap<String, Object>> parentSubjectVariableIndividualsList = (ArrayList<LinkedHashMap<String, Object>>) htblSubjectVariablehtblpropVal
					.get(parrentSubjectVariable);
			index = parentSubjectVariableIndividualsList.size() - 1;

			/*
			 * get parentSubjectVariableIndividual
			 * 
			 * It will always be the last item in the list because the results
			 * are added in order
			 */
			parentSubjectVariableIndividual = parentSubjectVariableIndividualsList.get(index);
		}

		/*
		 * create a new Hashtable<String,Object> to hold values of object value
		 */
		Hashtable<String, Object> htblObjectIndividualPropValue = new Hashtable<>();

		/*
		 * get objectType from the results.
		 * 
		 * Increment i to get the next columnName which has the type of
		 * objectValue
		 */

		Class objectClassType = OntologyMapper.getHtblMainOntologyClassesUriMappers().get(objectClassTypeURI);
		String uniqueIdentifierProperty = objectClassType.getUniqueIdentifierPropertyName();

		/*
		 * if the objectClassType does not have a uniqueIdentifierProperty
		 * defined in the ontology of the requesting application.
		 * 
		 * The System will generate an id and make it the uniqueIdentifier for
		 * that class
		 */
		if (uniqueIdentifierProperty == null) {
			uniqueIdentifierProperty = "id";
		}

		/*
		 * add uniqueIdentifier result and objectClassType to
		 * htblObjectIndividualPropValue
		 */
		htblObjectIndividualPropValue.put(uniqueIdentifierProperty, getValueFromURI(objectUniqueIdentifier));
		htblObjectIndividualPropValue.put("classType", objectClassType.getName());

		/*
		 * check if the property has multipleValues in order to add property to
		 * parentSubjectVariableIndividual with a value list (to hold multiple
		 * values)
		 */
		if (property.isMulitpleValues()) {

			/*
			 * check if the property was added before or not
			 */
			if (!parentSubjectVariableIndividual.containsKey(propertyName)) {

				/*
				 * create a new ArrayList<Hashtable<String, Object>> to hold
				 * objectValues
				 */
				ArrayList<Hashtable<String, Object>> valueList = new ArrayList<>();

				/*
				 * add subjectIndividual to propertyValueList (valueList)
				 */
				valueList.add(htblObjectIndividualPropValue);

				/*
				 * add property and its values list to
				 * parentSubjectVariableIndividual
				 */
				parentSubjectVariableIndividual.put(propertyName, valueList);

			} else {

				/*
				 * if the property existed before it means that this
				 * subjectVariable individual with individualUniqueIdentifier is
				 * another value to be added to valueList
				 * 
				 * get the propertyValueList
				 */
				ArrayList<Hashtable<String, Object>> valueList = (ArrayList<Hashtable<String, Object>>) parentSubjectVariableIndividual
						.get(propertyName);

				/*
				 * add subjectIndividual to propertyValueList (valueList)
				 */
				valueList.add(htblObjectIndividualPropValue);
			}

		} else {

			/*
			 * check if the property was added before or not
			 */
			if (!parentSubjectVariableIndividual.containsKey(propertyName)) {

				/*
				 * add property and subjectIndividual to
				 * parentSubjectVariableIndividual
				 */
				parentSubjectVariableIndividual.put(propertyName, htblObjectIndividualPropValue);

			} else {

				/*
				 * it will never happen but I will skip if it happens.
				 * 
				 * It will never happen that a property with a single value has
				 * multiple values because I checked that it never happen in
				 * insertion
				 */
				return;
			}

		}
	}

	/*
	 * constructResultOfObjectTypeColumn is used to construct result of
	 * columnName = objecttype
	 */
	private static void constructResultOfObjectTypeColumn(String columnName, Object propValue,
			Hashtable<String, QueryVariable> htblSubjectVariables,
			Hashtable<String, Object> htblSubjectVariablehtblpropVal) {
		/*
		 * get queryVariable of the subjectVariable to know its propertyName
		 */
		QueryVariable queryVariable = htblSubjectVariables.get(columnName);

		/*
		 * get subjectVariable of the objectVariable (columnName)
		 */
		String subjectVariable = queryVariable.getSubjectVariableName();

		/*
		 * get list of individuals of the parentSubjectVariable
		 * 
		 * if the parentSubjectVairable is subject0 it will be only one
		 * individual so I will cast it to Hashtable<String,Object>
		 * 
		 * else it will be a list of individuals so I will cast it to
		 * ArrayList<Hashtable<String,Object>>
		 */
		LinkedHashMap<String, Object> subjectVariableIndividual;
		if (subjectVariable.equals("subject0")) {

			subjectVariableIndividual = (LinkedHashMap<String, Object>) htblSubjectVariablehtblpropVal
					.get(subjectVariable);
		} else {
			ArrayList<LinkedHashMap<String, Object>> subjectVariableIndividualsList = (ArrayList<LinkedHashMap<String, Object>>) htblSubjectVariablehtblpropVal
					.get(subjectVariable);

			/*
			 * get subjectVariableIndividual
			 * 
			 * It will always be the last item in the list because the results
			 * are added in order
			 */
			subjectVariableIndividual = subjectVariableIndividualsList.get(subjectVariableIndividualsList.size() - 1);
		}

		/*
		 * add classType field and classType of objectValue to
		 * subjectVariableIndividual of the individual
		 */
		subjectVariableIndividual.put("classType", getValueFromURI(propValue.toString()));

	}

	/*
	 * getValueFromURI is used to get value from a URI.
	 * 
	 * URI consists of a prefixURI and value so I loop on all prefixes and
	 * return the value by removing the matched prefexURI
	 * 
	 * eg: uri = http://iot-platform#enterprisecompanies the returned value =
	 * enterprisecompanies
	 */
	private static String getValueFromURI(String uri) {
		for (Prefix prefix : Prefix.values()) {
			if (uri.contains(prefix.getUri())) {
				return uri.substring(prefix.getUri().length());
			}
		}
		return null;
	}

	/*
	 * getHtblPropValue is used to get the htblPropValue
	 * 
	 * I created this method because I can have two type of propertiesValueList
	 * 
	 * 1- a list of Hashtable<String, Object> if the property has multiple
	 * values
	 * 
	 * 2- A single Hashtable<String, Object> if the property has a single value
	 * 
	 * I store them as an object in htblSubjectVariablehtblpropVal so this
	 * method is used to get the htblPropValue which contains the property and
	 * its value
	 */
	private static Hashtable<String, Object> getHtblPropValue(Object subjectVariablePropValueObject) {
		/*
		 * get target htblfieldObject that holds the propValues of
		 * subjectVariable
		 */
		Hashtable<String, Object> htblPropValue;
		if (subjectVariablePropValueObject instanceof java.util.Hashtable<?, ?>) {
			/*
			 * subjectVariable has one single Hashtable<String, Object> that
			 * holds its prop values
			 */
			htblPropValue = (Hashtable<String, Object>) subjectVariablePropValueObject;

		} else {
			/*
			 * subjectVariable has list of Hashtable<String, Object> that holds
			 * all individuals' prop values
			 */
			List<Hashtable<String, Object>> htblIndividualshtblPropValueList = (List<Hashtable<String, Object>>) subjectVariablePropValueObject;

			/*
			 * get the index of the last individual because I am iterating on
			 * values so I am adding values in order
			 */
			int lastIndividualIndex = htblIndividualshtblPropValueList.size() - 1;
			htblPropValue = htblIndividualshtblPropValueList.get(lastIndividualIndex);
		}

		return htblPropValue;
	}

}