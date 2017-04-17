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

import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.XSDDatatype;
import com.iotplatform.ontology.dynamicConcepts.DynamicConceptsUtility;
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

	private DynamicConceptsUtility dynamicPropertiesUtility;

	@Autowired
	public SelectionQueryResults(DynamicConceptsUtility dynamicPropertiesUtility) {
		this.dynamicPropertiesUtility = dynamicPropertiesUtility;
	}

	/*
	 * constructQueryResult method used to return results without any prefixed
	 * ontology URIs
	 */

	private Object[] constructSinglePropertyValuePair(String applicationName, String propertyURI, Object value,
			Class subjectClass) {
		Object[] res = new Object[2];

		String propertyName = subjectClass.getHtblPropUriName().get(propertyURI);

		if (propertyName == null) {

			/*
			 * update subject class properties list by loading the dynamic
			 * properties from database
			 */

			dynamicPropertiesUtility.getDynamicProperties(applicationName, subjectClass);
			propertyName = subjectClass.getHtblPropUriName().get(propertyURI);

		}
		System.out.println(propertyName + "   " + propertyURI);
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

	public List<Hashtable<String, Object>> constractResponeJsonObjectForListSelection(String applicationName,
			ResultSet results, Class subjectClass) throws SQLException {

		List<Hashtable<String, Object>> responseJson = new ArrayList<>();

		Hashtable<Object, Hashtable<String, Object>> temp = new Hashtable<>();

		Hashtable<String, Property> subjectClassProperties = subjectClass.getProperties();

		while (results.next()) {

			Object subject = results.getObject(1);

			/*
			 * create a new hashtable to hold subject's property and value
			 */

			if (temp.size() == 0) {
				Hashtable<String, Object> htblSubjectPropVal = new Hashtable<>();
				temp.put(subject, htblSubjectPropVal);
				responseJson.add(htblSubjectPropVal);
			}

			// skip rdf:type property

			if (results.getString(2).equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
				continue;
			}

			Object[] preparedPropVal = constructSinglePropertyValuePair(applicationName, results.getString(2),
					results.getString(3), subjectClass);

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
					Hashtable<String, Object> htblAdminPropVal = new Hashtable<>();
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

					Hashtable<String, Object> htblAdminPropVal = new Hashtable<>();
					temp.put(subject, htblAdminPropVal);
					temp.get(subject).put(propertyName, value);
					responseJson.add(htblAdminPropVal);

				}
			}
		}
		return responseJson;
	}

	public static List<Hashtable<String, Object>> constructQueryResult2(String applicationName, ResultSet results,
			String requestClassName, Hashtable<String, QueryVariable> htblSubjectVariables) {

		/*
		 * consturctedQueryResult is the list of constructed query result where
		 * each element in the list represent a Hashtable<String, Object> which
		 * contains the propValues of an individual
		 */
		List<Hashtable<String, Object>> consturctedQueryResult = new ArrayList<>();

		/*
		 * helperList is the list of all hashtables created to hold the
		 * propertyValues of each new subjectVariable
		 */
		// List<Hashtable<String, Object>> helperList = null;

		/*
		 * htblIndividualIndex is used to hold uniqueIdentifier of an individual
		 * (key) and index of individual in its subjectVariavle list
		 * 
		 */
		LinkedHashMap<String, Integer> htblIndividualIndex = null;

		/*
		 * htblSubjectVariablehtblpropVal holds the subjectVariable as key and
		 * its list of individuals as value
		 */
		Hashtable<String, Object> htblSubjectVariablehtblpropVal = null;

		try {
			/*
			 * is used to hold subjectURI of the current individual (subjectURI
			 * is the uniqueIdnetifier of this individual)
			 */
			String currentMainSubjectURI = "";

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
				if (!currentMainSubjectURI.equals(mainSubjectUri)) {

					/*
					 * initialize helperList which is the list of all hashtables
					 * created to hold the propertyValues of each new
					 * subjectVariable
					 */
					// helperList = new ArrayList<>();

					/*
					 * Initialize htblIndividualIndex whic is used to hold
					 * uniqueIdentifier of an individual (key) and index of
					 * individual in its subjectVariavle list
					 * 
					 */
					htblIndividualIndex = new LinkedHashMap<>();

					/*
					 * construct new data structures instances to hold data of
					 * the new individual
					 */
					htblSubjectVariablehtblpropVal = new Hashtable<>();

					Hashtable<String, Object> htblIndividualPropertyValue = new Hashtable<>();

					htblSubjectVariablehtblpropVal.put("subject0", htblIndividualPropertyValue);

					consturctedQueryResult.add(htblIndividualPropertyValue);

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
						 * get queryVariable of the subjectVariable to know its
						 * propertyName
						 */
						QueryVariable queryVariable = htblSubjectVariables.get(columnName);
						String propertyName = queryVariable.getPropertyName();

						/*
						 * get parrentSubjectVariable of the objectVariable
						 * (columnName)
						 */
						String parrentSubjectVariable = queryVariable.getSubjectVariableName();

						/*
						 * get propertyMapping of propertyName that connects
						 * subjectVariable to objectVariable (columnName)
						 */
						String classUri = queryVariable.getSubjectClassUri();
						Class propertyClass = OntologyMapper.getHtblMainOntologyClassesUriMappers().get(classUri);
						Property property = propertyClass.getProperties().get(propertyName);

						String individualUniqueIdentifier = results.getString(columnName);

						/*
						 * check if the individual was added before or not
						 */
						if (!htblSubjectVariablehtblpropVal.containsKey(columnName)) {

							/*
							 * create a new ArrayList<Hashtable<String, Object>>
							 * to hold the individuals of this subjectVariable
							 */
							ArrayList<Hashtable<String, Object>> htblSubjectIndividualsList = new ArrayList<>();

							/*
							 * create a new Hashtable<String, Object> to hold
							 * individuals propValues
							 */
							Hashtable<String, Object> subjectIndividual = new Hashtable<>();

							/*
							 * add it to htblSubjectIndividualsList
							 */
							htblSubjectIndividualsList.add(subjectIndividual);

							/*
							 * add individualUniqueIdentifier with index=0 as it
							 * is the first time to see this subjectVariable to
							 * htblIndividualIndex
							 */
							htblIndividualIndex.put(individualUniqueIdentifier, 0);

							/*
							 * add new subjectVariable's individualsList to
							 * htblSubjectVariablehtblpropVal
							 */
							htblSubjectVariablehtblpropVal.put(columnName, htblSubjectIndividualsList);

							/*
							 * get list of individuals of the
							 * parentSubjectVariable
							 * 
							 * if the parentSubjectVairable is subject0 it will
							 * be only one individual so I will cast it to
							 * Hashtable<String,Object>
							 * 
							 * else it will be a list of individuals so I will
							 * cast it to ArrayList<Hashtable<String,Object>>
							 */
							Hashtable<String, Object> parentSubjectVariableIndividual;
							if (parrentSubjectVariable.equals("subject0")) {

								parentSubjectVariableIndividual = (Hashtable<String, Object>) htblSubjectVariablehtblpropVal
										.get(parrentSubjectVariable);
							} else {
								ArrayList<Hashtable<String, Object>> individualsList = (ArrayList<Hashtable<String, Object>>) htblSubjectVariablehtblpropVal
										.get(parrentSubjectVariable);
								parentSubjectVariableIndividual = individualsList.get(individualsList.size() - 1);
							}

							/*
							 * check if the property has multipleValues in order
							 * to add property to
							 * parentSubjectVariableIndividual with a value list
							 * (to hold multiple values)
							 */
							if (property.isMulitpleValues()) {

								/*
								 * check if the property was added before or not
								 */
								if (!parentSubjectVariableIndividual.containsKey(propertyName)) {

									/*
									 * create a new ArrayList<Hashtable<String,
									 * Object>> to hold objectValues
									 */
									ArrayList<Hashtable<String, Object>> valueList = new ArrayList<>();

									/*
									 * add subjectIndividual to
									 * propertyValueList (valueList)
									 */
									valueList.add(subjectIndividual);

									/*
									 * add property and its values list to
									 * parentSubjectVariableIndividual
									 */
									parentSubjectVariableIndividual.put(propertyName, valueList);

								} else {

									/*
									 * if the property existed before it means
									 * that this subjectVariable individual with
									 * individualUniqueIdentifier is another
									 * value to be added to valueList
									 * 
									 * get the propertyValueList
									 */
									ArrayList<Hashtable<String, Object>> valueList = (ArrayList<Hashtable<String, Object>>) parentSubjectVariableIndividual
											.get(propertyName);

									/*
									 * add subjectIndividual to
									 * propertyValueList (valueList)
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
									 * it will never happen but I will skip if
									 * it happens.
									 * 
									 * It will never happen that a property with
									 * a single value has multiple values
									 * because I checked that it never happen in
									 * insertion
									 */
									continue;
								}

							}

						} else {

							/*
							 * subjectVariable has been seen before.
							 * 
							 * Check if the individual with
							 * individualUniqueIdentifier was seen before or not
							 */
							if (!htblIndividualIndex.containsKey(individualUniqueIdentifier)) {

								/*
								 * get subjectVariableIndividualsList
								 */
								ArrayList<Hashtable<String, Object>> individualsList = (ArrayList<Hashtable<String, Object>>) htblSubjectVariablehtblpropVal
										.get(columnName);

								/*
								 * create new Hashtable<String, Object> to hold
								 * individual's propertyValue
								 */
								Hashtable<String, Object> subjectIndividual = new Hashtable<>();
								individualsList.add(subjectIndividual);

								/*
								 * add individualUniqueIdentifier to
								 * htblIndividualIndex
								 */
								htblIndividualIndex.put(individualUniqueIdentifier, individualsList.size() - 1);

								/*
								 * get list of individuals of the
								 * parentSubjectVariable
								 * 
								 * if the parentSubjectVairable is subject0 it
								 * will be only one individual so I will cast it
								 * to Hashtable<String,Object>
								 * 
								 * else it will be a list of individuals so I
								 * will cast it to
								 * ArrayList<Hashtable<String,Object>>
								 */
								Hashtable<String, Object> parentSubjectVariableIndividual;
								if (parrentSubjectVariable.equals("subject0")) {

									parentSubjectVariableIndividual = (Hashtable<String, Object>) htblSubjectVariablehtblpropVal
											.get(parrentSubjectVariable);
								} else {
									ArrayList<Hashtable<String, Object>> parentSubjectVariableIndividualsList = (ArrayList<Hashtable<String, Object>>) htblSubjectVariablehtblpropVal
											.get(parrentSubjectVariable);
									parentSubjectVariableIndividual = individualsList.get(individualsList.size() - 1);
								}

								/*
								 * check if the property has multipleValues in
								 * order to add property to
								 * parentSubjectVariableIndividual with a value
								 * list (to hold multiple values)
								 */
								if (property.isMulitpleValues()) {

									/*
									 * check if the property was added before or
									 * not
									 */
									if (!parentSubjectVariableIndividual.containsKey(propertyName)) {

										/*
										 * create a new
										 * ArrayList<Hashtable<String, Object>>
										 * to hold objectValues
										 */
										ArrayList<Hashtable<String, Object>> valueList = new ArrayList<>();

										/*
										 * add subjectIndividual to
										 * propertyValueList (valueList)
										 */
										valueList.add(subjectIndividual);

										/*
										 * add property and its values list to
										 * parentSubjectVariableIndividual
										 */
										parentSubjectVariableIndividual.put(propertyName, valueList);

									} else {

										/*
										 * if the property existed before it
										 * means that this subjectVariable
										 * individual with
										 * individualUniqueIdentifier is another
										 * value to be added to valueList
										 * 
										 * get the propertyValueList
										 */
										ArrayList<Hashtable<String, Object>> valueList = (ArrayList<Hashtable<String, Object>>) parentSubjectVariableIndividual
												.get(propertyName);

										/*
										 * add subjectIndividual to
										 * propertyValueList (valueList)
										 */
										valueList.add(subjectIndividual);
									}

								} else {

									/*
									 * check if the property was added before or
									 * not
									 */
									if (!parentSubjectVariableIndividual.containsKey(propertyName)) {

										/*
										 * add property and subjectIndividual to
										 * parentSubjectVariableIndividual
										 */
										parentSubjectVariableIndividual.put(propertyName, subjectIndividual);

									} else {

										/*
										 * it will never happen but I will skip
										 * if it happens.
										 * 
										 * It will never happen that a property
										 * with a single value has multiple
										 * values because I checked that it
										 * never happen in insertion
										 */
										continue;
									}

								}

							}else{
								/*
								 * individual was added before so I will skip it
								 */
								continue;
							}

						}

					} else {
						/*
						 * check that it is variable 
						 */
					}

				}
			}
		} catch (

		SQLException e) {
			throw new DatabaseException(e.getMessage(), requestClassName);
		}

		return consturctedQueryResult;
	}

	public static List<Hashtable<String, Object>> constructQueryResult(String applicationName, ResultSet results,
			String requestClassName, Hashtable<String, QueryVariable> htblSubjectVariables) {

		List<Hashtable<String, Object>> consturctedQueryResult = new ArrayList<>();

		/*
		 * htblIndividualSubjectIndex has subjectUri as key and index of
		 * subjectUri in consturctedQueryResult
		 */
		LinkedHashMap<String, Integer> htblIndividualSubjectIndex = new LinkedHashMap<>();

		/*
		 * htblIndividualIndex is used to hold uniqueIdentifier of an individual
		 * (key) and index of individual in list (value)
		 * 
		 * htblIndividualIndex is used only when there is an objectProperty that
		 * has multiple values so the value of this property has to be
		 * List<Hashtable<String, Object>> each row in the list represent an
		 * individual so htblIndividualIndex is used to get index of individual
		 */
		LinkedHashMap<String, Integer> htblIndividualIndex = new LinkedHashMap<>();

		/*
		 * helperList is the list of all hashtables created to hold the
		 * propertyValues of each new subject
		 */
		List<Hashtable<String, Object>> helperList = new ArrayList<>();

		try {
			ResultSetMetaData rsmd = results.getMetaData();

			int columnsNumber = rsmd.getColumnCount();
			while (results.next()) {

				/*
				 * SUBJECT0 must be always the first column
				 */
				String mainSubjectUri = results.getString("SUBJECT0");

				/*
				 * check if the subjectUri exist before or not in
				 * htblIndividualSubjectIndex
				 */
				if (!htblIndividualSubjectIndex.containsKey(mainSubjectUri)) {

					/*
					 * construct new data structures instances to hold data of
					 * the new subjectUri
					 */
					Hashtable<String, Object> htblSubjectVariablehtblpropVal = new Hashtable<>();

					Hashtable<String, Object> htblIndividualPropertyValue = new Hashtable<>();

					htblSubjectVariablehtblpropVal.put("subject0", htblIndividualPropertyValue);

					helperList.add(htblSubjectVariablehtblpropVal);

					/*
					 * store index of mainSubjectUri
					 * htblSubjectVariablehtblpropVal in helperList
					 */
					int index = helperList.size() - 1;
					htblIndividualSubjectIndex.put(mainSubjectUri, index);

					consturctedQueryResult.add(htblIndividualPropertyValue);

				}

				/*
				 * iterate over the row columns and start from i=2 to skip the
				 * first column (subject0)
				 */
				for (int i = 2; i <= columnsNumber; i++) {

					String columnName = rsmd.getColumnName(i).toLowerCase();
					Object propValue = results.getObject(i);

					if (propValue == null)
						continue;

					if (columnName.contains("subject")) {
						int index = htblIndividualSubjectIndex.get(mainSubjectUri);

						/*
						 * get queryVariable of the subjectVariable to know its
						 * propertyName
						 */
						QueryVariable queryVariable = htblSubjectVariables.get(columnName);
						String propertyName = queryVariable.getPropertyName();

						/*
						 * get parrentSubjectVariable of the objectVariable
						 * (columnName)
						 */
						String parrentSubjectVariable = queryVariable.getSubjectVariableName();

						/*
						 * get propertyMapping of propertyName that connects
						 * subjectVariable to objectVariable (columnName)
						 */
						String classUri = queryVariable.getSubjectClassUri();
						Class propertyClass = OntologyMapper.getHtblMainOntologyClassesUriMappers().get(classUri);
						Property property = propertyClass.getProperties().get(propertyName);

						Hashtable<String, Object> htblSubjectVariablehtblpropVal = helperList.get(index);

						String individualUniqueIdentifier = results.getString(columnName);

						/*
						 * check if the subject variable exist
						 * 
						 * if it exist so it means the this subject has a
						 * repeated value either it is the same as the previous
						 * one or it is a new value and I will know which case
						 * by checking the individualUniqueIdentifier
						 * 
						 * the value might be repeated because every record is
						 * repeated once their is a one graph pattern that have
						 * different result than the previous one
						 */
						if (htblSubjectVariablehtblpropVal.containsKey(columnName)) {

							/*
							 * create new Hashtable<String, Object> instance to
							 * hold new subjectVariable data
							 */
							Hashtable<String, Object> htblPropValue = new Hashtable<>();

							/*
							 * Make sure that individualUniqueIdentifier was not
							 * added before
							 */
							if (!htblIndividualIndex.containsKey(individualUniqueIdentifier)) {

								/*
								 * get subjectVariablePropValue from
								 * htblSubjectVariablehtblpropVal
								 */
								Object parentSubjectVariablePropValue = htblSubjectVariablehtblpropVal
										.get(parrentSubjectVariable);

								/*
								 * get target htblParentFieldObject that holds
								 * the propValues of parentSubjectVariable
								 */
								Hashtable<String, Object> htblParentPropValue = getHtblPropValue(
										parentSubjectVariablePropValue);

								/*
								 * check if the property has multipleValue in
								 * order to add the new individual to list of
								 * individuals
								 */
								if (property.isMulitpleValues()) {

									/*
									 * get valueList of the property from
									 * parentVariable htblParentPropValue
									 */
									ArrayList<Hashtable<String, Object>> valueList = (ArrayList<Hashtable<String, Object>>) htblParentPropValue
											.get(propertyName);

									/*
									 * if valueList is null it means that the
									 * subjectVariable is a totally new result
									 * so I will create a new list of
									 * htblPropValue because it is a property
									 * with multiple values
									 * 
									 * then add valueList to parentSUbject's
									 * htblPropertyValue and then create a new
									 * subjectPropValue and add it to
									 * htblSubjectVariablehtblpropVal
									 * 
									 * I created a new subjectPropValue because
									 * I am using pointer referencing, so I have
									 * to remove the previous reference of that
									 * subjectVariable and create a new one to
									 * be referenced by the
									 * parentSubjectVariable htblPropValue
									 * 
									 * This condition happen when we have a
									 * property in the parentSubjectVariable
									 * that has another subjectVariable that was
									 * added before but with different results
									 * so it must point to the whole
									 * subjectVariable htblPropValue not only a
									 * single value in the list
									 */
									if (valueList == null) {
										valueList = new ArrayList<Hashtable<String, Object>>();
										htblParentPropValue.put(propertyName, valueList);

										/*
										 * create a new subjectVariable
										 * htblPropValueList because the
										 * property has multiple values
										 */
										htblSubjectVariablehtblpropVal.put(columnName, valueList);

									}

									/*
									 * adding new individual to value List
									 * 
									 * this will also be added to list of
									 * individuals of subjectVariable stored in
									 * columnName because they both reference
									 * (pointers) the same list so adding it in
									 * one of them reflect the other
									 */
									valueList.add(htblPropValue);

									/*
									 * add index (which will be zero because it
									 * is the first time to see this
									 * subjectVariable) of the individual to
									 * htblIndividualIndex
									 */
									htblIndividualIndex.put(individualUniqueIdentifier, null);

								} else {

									/*
									 * add objectPropertyName and add reference
									 * to htblPropValue(Hashtable<String,Object)
									 * that holds the subjectVariabelIndividual
									 * data
									 */
									htblParentPropValue.put(propertyName, htblPropValue);

									/*
									 * get subjectVariablePropValue of the
									 * subjectVariable (stored in columnName)
									 * from htblSubjectVariablehtblpropVal
									 */
									Object subjectVariablePropValue = htblSubjectVariablehtblpropVal.get(columnName);

									/*
									 * if subjectVariablePropValue is an
									 * instance of hashtable<String,Object> so I
									 * will reinitialize a new instance to hold
									 * new individual data
									 */
									if (subjectVariablePropValue instanceof java.util.Hashtable<?, ?>) {
										htblSubjectVariablehtblpropVal.put(columnName, htblPropValue);

									}

									/*
									 * add index (which will be zero because it
									 * is the first time to see this
									 * subjectVariable) of the individual to
									 * htblIndividualIndex
									 */
									htblIndividualIndex.put(individualUniqueIdentifier, null);
								}

							} else {

								/*
								 * individualUniqueIdentifier was added before
								 * so skip this iteration because we do not need
								 * to override that past values because they are
								 * the same
								 */
								continue;
							}

						} else {

							/*
							 * create new Hashtable<String, Object> instance to
							 * hold new subjectVariable data
							 */
							Hashtable<String, Object> htblPropValue = new Hashtable<>();

							/*
							 * get subjectVariablePropValue from
							 * htblSubjectVariablehtblpropVal
							 */
							Object parentSubjectVariablePropValue = htblSubjectVariablehtblpropVal
									.get(parrentSubjectVariable);

							/*
							 * get target htblfieldObject that holds the
							 * propValues of parentSubjectVariable
							 */
							Hashtable<String, Object> htblParentFieldObject = getHtblPropValue(
									parentSubjectVariablePropValue);

							/*
							 * check if the property has multipleValue in order
							 * to create a new Arraylist to hold values
							 */
							if (property.isMulitpleValues()) {
								ArrayList<Hashtable<String, Object>> valueList = new ArrayList<>();
								valueList.add(htblPropValue);

								htblParentFieldObject.put(propertyName, valueList);

								/*
								 * add new subject variable and its valueList to
								 * htblSubjectVariablehtblpropVal
								 */
								htblSubjectVariablehtblpropVal.put(columnName, valueList);

								/*
								 * add index (which will be zero because it is
								 * the first time to see this subjectVariable)
								 * of the individual to htblIndividualIndex
								 */
								htblIndividualIndex.put(individualUniqueIdentifier, 0);

							} else {
								htblParentFieldObject.put(propertyName, htblPropValue);

								/*
								 * add new subject variable and its
								 * htblPropValue to
								 * htblSubjectVariablehtblpropVal
								 */
								htblSubjectVariablehtblpropVal.put(columnName, htblPropValue);

								/*
								 * add index (which will be zero because it is
								 * the first time to see this subjectVariable)
								 * of the individual to htblIndividualIndex
								 */
								htblIndividualIndex.put(individualUniqueIdentifier, 0);
							}

						}

					} else {
						if (columnName.contains("var")) {
							/*
							 * it is a variable not a subject so get value of
							 * the variable
							 * 
							 * get queryVariable of the subjectVariable to know
							 * its propertyName
							 */
							QueryVariable queryVariable = htblSubjectVariables.get(columnName);
							String propertyName = queryVariable.getPropertyName();

							/*
							 * get propertyMapping of propertyName that connects
							 * subjectVariable to objectVariable (columnName)
							 */
							String classUri = queryVariable.getSubjectClassUri();
							Class propertyClass = OntologyMapper.getHtblMainOntologyClassesUriMappers().get(classUri);
							Property property = propertyClass.getProperties().get(propertyName);

							/*
							 * construct value datatype of propValue if the
							 * property is a datatype property
							 */
							if (property instanceof DataTypeProperty) {
								propValue = typeCastValueToItsDataType((DataTypeProperty) property, propValue);
							}

							/*
							 * get subjectVariable of the objectVariable
							 * (columnName)
							 */
							String parentSubjectVariable = queryVariable.getSubjectVariableName();

							/*
							 * get parentSubjectVariablePropValue of the
							 * subjectVariable
							 */
							int index = htblIndividualSubjectIndex.get(mainSubjectUri);
							Object parentSubjectVariablePropValue = helperList.get(index).get(parentSubjectVariable);
							/*
							 * get target htblParentFieldObject that holds the
							 * propValues of subjectVariable
							 */
							Hashtable<String, Object> htblParentFieldObject;

							if (parentSubjectVariablePropValue instanceof java.util.Hashtable<?, ?>) {

								/*
								 * parentSubjectVariable has one single
								 * Hashtable<String, Object> that holds its prop
								 * values
								 */
								htblParentFieldObject = (Hashtable<String, Object>) parentSubjectVariablePropValue;

							} else {

								/*
								 * subjectVariable has list of Hashtable<String,
								 * Object> that holds all individuals' prop
								 * values
								 */
								List<Hashtable<String, Object>> htblfieldObjectList = (List<Hashtable<String, Object>>) parentSubjectVariablePropValue;

								/*
								 * get the index of the last individual because
								 * I am iterating on values so I am adding
								 * values in order s
								 */
								int lastIndividualIndex = htblfieldObjectList.size() - 1;
								htblParentFieldObject = htblfieldObjectList.get(lastIndividualIndex);
							}

							/*
							 * check if the subjectVariablePropVal contains the
							 * propertyName
							 */
							if (htblParentFieldObject.containsKey(propertyName) && property.isMulitpleValues()) {
								Object value = htblParentFieldObject.get(propertyName);

								/*
								 * if the value exist then it must be an
								 * arrayList of values so I have to add
								 * literalValue
								 */
								ArrayList<Object> valueList = (ArrayList<Object>) value;
								if (!valueList.contains(propValue)) {
									valueList.add(propValue);
								}
							} else {

								/*
								 * check if the property has multipleValue in
								 * order to create a new Arraylist to hold
								 * values
								 */
								if (property.isMulitpleValues()) {
									ArrayList<Object> valueList = new ArrayList<>();
									valueList.add(propValue);

									htblParentFieldObject.put(propertyName, valueList);

								} else {
									htblParentFieldObject.put(propertyName, propValue);

								}

							}

						} else {

						}
					}
				}
			}

		} catch (

		SQLException e) {
			throw new DatabaseException(e.getMessage(), requestClassName);
		}

		System.out.println("--->" + helperList);

		return consturctedQueryResult;
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