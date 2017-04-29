package com.iotplatform.queries;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.XSDDatatype;
import com.iotplatform.ontology.mapers.DynamicOntologyMapper;
import com.iotplatform.ontology.mapers.OntologyMapper;
import com.iotplatform.utilities.InsertionPropertyValue;
import com.iotplatform.utilities.UpdatePropertyValueUtility;

public class UpdateQuery {

	private static String prefixesString = null;

	/**
	 * 
	 * constructUpdateQuery is used to construct update query for the parsed
	 * update request
	 * 
	 * @param applicationModelName
	 * @param requestSubjectClassName
	 * @param updatePropertyValueList
	 * @param htblClassInsertionPropertyValue
	 * @return
	 */
	public static String constructUpdateQuery(Class subjectClass, String individuleUniqueIdentifier,
			ArrayList<UpdatePropertyValueUtility> updatePropertyValueList,
			Hashtable<String, ArrayList<ArrayList<InsertionPropertyValue>>> htblClassInsertionPropertyValue,
			String applicationModelName) {

		/*
		 * updateQueryBuilder is the main query builder
		 */
		StringBuilder updateQueryBuilder = new StringBuilder();

		/*
		 * deleteClauseBuilder is the delete part builder
		 */
		StringBuilder deleteClauseBuilder = new StringBuilder();

		/*
		 * insertClauseBuiler is the insert part builder
		 */
		StringBuilder insertClauseBuilder = new StringBuilder();

		/*
		 * whereClauseBuilder is the where part builder
		 */
		StringBuilder whereClauseBuilder = new StringBuilder();

		/*
		 * variable number array of 1 item. I use array to be able to pass it by
		 * reference between methods
		 */
		int[] varNum = { 0 };

		if (updatePropertyValueList != null) {
			constructUpdateQueryPatternsForUpdateRequestPart(subjectClass, updatePropertyValueList, deleteClauseBuilder,
					insertClauseBuilder, whereClauseBuilder, varNum);
		}

		if (htblClassInsertionPropertyValue != null) {
			constructUpdateQueryPatternsForInsertRequestPart(subjectClass, htblClassInsertionPropertyValue,
					applicationModelName, deleteClauseBuilder, insertClauseBuilder, whereClauseBuilder, varNum);
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

		updateQueryBuilder.append(prefixesString);

		if (deleteClauseBuilder.length() > 0) {
			/*
			 * start delete clause part
			 */
			deleteClauseBuilder.insert(0, "DELETE \n { \n");

			/*
			 * end deleteClauseBuilder and append it to updateQueryBuilder
			 */
			deleteClauseBuilder.append("} \n");
			updateQueryBuilder.append(deleteClauseBuilder.toString());
		}

		/*
		 * start insert clause part
		 */
		insertClauseBuilder.insert(0, "INSERT \n { \n");
		/*
		 * end insertClauseBuilder and append it to updateQueryBuilder
		 */
		insertClauseBuilder.append("} \n");
		updateQueryBuilder.append(insertClauseBuilder.toString());

		/*
		 * start where clause part
		 */
		whereClauseBuilder.insert(0,
				"WHERE \n { \n ?subject a " + subjectClass.getPrefix().getPrefix() + subjectClass.getName()
						+ " . \n FILTER( ?subject = iot-platform:"
						+ individuleUniqueIdentifier.toLowerCase().replaceAll(" ", "") + " ) \n");

		/*
		 * end whereClauseBuilder and append it to updateQueryBuilder
		 */
		whereClauseBuilder.append("} \n");
		updateQueryBuilder.append(whereClauseBuilder.toString());

		return updateQueryBuilder.toString();
	}

	private static void constructUpdateQueryPatternsForUpdateRequestPart(Class subjectClass,
			ArrayList<UpdatePropertyValueUtility> updatePropertyValueList, StringBuilder deleteClauseBuilder,
			StringBuilder insertClauseBuilder, StringBuilder whereClauseBuilder, int[] varNum) {
		/*
		 * iterate over updatePropertyValueList to construct update patterns for
		 * parsed update request body part
		 */
		boolean start = true;
		for (UpdatePropertyValueUtility updatePropertyValue : updatePropertyValueList) {

			String prefixedPropertyName = updatePropertyValue.getPropertyPrefixedName();
			Object newValue = updatePropertyValue.getNewValue();

			/*
			 * I need to delete the old value and then insert the new value
			 * 
			 * delete the old value
			 */
			if (start) {
				deleteClauseBuilder.append("?subject " + prefixedPropertyName + " ?var" + varNum[0]);

			} else {
				deleteClauseBuilder.append(" ; \n " + prefixedPropertyName + " ?var" + varNum[0]);

			}

			/*
			 * insert new value
			 */
			if (start) {

				insertClauseBuilder.append("?subject " + updatePropertyValue.getPropertyPrefixedName() + " "
						+ getValue(subjectClass, prefixedPropertyName, newValue).toString());

				/*
				 * set start to false
				 */
				start = false;

			} else {
				insertClauseBuilder.append(" ; \n " + updatePropertyValue.getPropertyPrefixedName() + " "
						+ getValue(subjectClass, prefixedPropertyName, newValue).toString());
			}

			/*
			 * check that the updatePropertyValue' propertyPrefixedName map to a
			 * multiValued property
			 * 
			 * for multiValuedProperty the parsed updatePropertyValue will have
			 * the old value the will be deleted and the new value that will be
			 * inserted
			 * 
			 * so I have to filter the values of the property to match the old
			 * value
			 */
			if (updatePropertyValue.isPropertyMultipleValued()) {

				whereClauseBuilder.append("?subject " + prefixedPropertyName + " ?var" + varNum[0] + " . \n");
				whereClauseBuilder.append("FILTER( ?var" + varNum[0] + " = "
						+ getValue(subjectClass, prefixedPropertyName, updatePropertyValue.getOldValue()).toString()
						+ " ) \n");

			} else {
				whereClauseBuilder.append("?subject " + prefixedPropertyName + " ?var" + varNum[0] + " . \n");

			}

			varNum[0]++;
		}

		if (insertClauseBuilder.length() > 0) {

			/*
			 * if added patterns to insert clause then I have to end the
			 * patterns added by dot (.)
			 */
			insertClauseBuilder.append(". \n");
		}

		if (deleteClauseBuilder.length() > 0) {

			/*
			 * if added patterns to delete clause then I have to end the
			 * patterns added by dot (.)
			 */
			deleteClauseBuilder.append(". \n");
		}

	}

	private static void constructUpdateQueryPatternsForInsertRequestPart(Class requestSubjectClass,
			Hashtable<String, ArrayList<ArrayList<InsertionPropertyValue>>> htblClassPropertyValue,
			String applicationModelName, StringBuilder deleteClauseBuilder, StringBuilder insertClauseBuilder,
			StringBuilder whereClauseBuilder, int[] varNum) {

		/*
		 * get the requestSubjectClass individual (which is the list of
		 * InsertionPropertyValue that corresponds to new properties and values
		 * being added to the individual that is requested in the updateRequest)
		 * 
		 * htblRequestIndividualInsertionPropValueList will be the first
		 * individual in the list of individuals of class = requestSubjectClass
		 */
		ArrayList<InsertionPropertyValue> requestIndividualInsertionPropValueList = htblClassPropertyValue
				.get(requestSubjectClass.getName()).get(0);

		/*
		 * construct triples for request individual because It is not a new
		 * object. So the constructed patterns has to only add the new patterns
		 * to the existing object
		 */
		constructInsertTriplesForRequestIndividual(requestSubjectClass, requestIndividualInsertionPropValueList,
				deleteClauseBuilder, insertClauseBuilder, whereClauseBuilder, varNum);

		/*
		 * remove individual requestIndividualInsertionPropValueList to not be
		 * constructed again down
		 */
		htblClassPropertyValue.get(requestSubjectClass.getName()).remove(0);

		/*
		 * iterate on htblClassPropertyValue and construct other insert triples
		 * if there exist other new individuals
		 */
		Iterator<String> htblClassPropertyValueIterator = htblClassPropertyValue.keySet().iterator();
		while (htblClassPropertyValueIterator.hasNext()) {
			String subjectClassName = htblClassPropertyValueIterator.next().toLowerCase();

			Class subjectClass = null;

			/*
			 * get subjectClass from dynamicOntology cache if it exist
			 */
			if ((DynamicOntologyMapper.getHtblappDynamicOntologyClasses().contains(applicationModelName)
					&& DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
							.containsKey(subjectClassName))) {
				subjectClass = DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
						.get(subjectClassName);

			} else {

				if (OntologyMapper.getHtblMainOntologyClassesMappers().containsKey(subjectClassName)) {

					/*
					 * get the objectClass from MainOntologyClassesMapper
					 */
					subjectClass = OntologyMapper.getHtblMainOntologyClassesMappers().get(subjectClassName);

				}
			}

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
			for (int i = 0; i < htblClassPropertyValue.get(subjectClass.getName()).size(); i++) {
				ArrayList<InsertionPropertyValue> instancePropertyValueList = htblClassPropertyValue
						.get(subjectClass.getName()).get(i);
				constructClassInstanceTriples(subjectClass, uniqueIdentifierProperty, instancePropertyValueList,
						insertClauseBuilder);
			}

		}

	}

	/*
	 * constructClassInstanceTriples method constructs triples of an instance of
	 * type subjectClass
	 */
	private static void constructClassInstanceTriples(Class subjectClass, Property uniqueIdentifierProperty,
			ArrayList<InsertionPropertyValue> instancePropertyValueList, StringBuilder insertClauseBuilder) {

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

		for (InsertionPropertyValue propertyValue : instancePropertyValueList) {

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
		insertClauseBuilder.append(Prefix.IOT_PLATFORM.getPrefix() + subjectUniqueIdentifier + "  a  "
				+ subjectClass.getPrefix().getPrefix() + subjectClass.getName() + "  ; \n");

		/*
		 * get all superClasses of subjectClass to identify that the new
		 * instance is also an instance of all superClasses of subjectClass
		 */

		for (Class superClass : subjectClass.getSuperClassesList()) {
			insertClauseBuilder.append("  a  " + superClass.getPrefix().getPrefix() + superClass.getName() + "  ; \n");

		}

		/*
		 * add rest of triples by appending tempBuilder
		 */

		insertClauseBuilder.append(tempBuilder.toString());

	}

	private static void constructInsertTriplesForRequestIndividual(Class subjectClass,
			ArrayList<InsertionPropertyValue> instancePropertyValueList, StringBuilder deleteClauseBuilder,
			StringBuilder insertClauseBuilder, StringBuilder whereClauseBuilder, int[] varNum) {

		/*
		 * iterate over instancePropertyValueList
		 */
		boolean start = true;
		for (InsertionPropertyValue insertionPropertyValue : instancePropertyValueList) {

			/*
			 * check if the insertionPropertyValue's property is a single value
			 * property in order to remove the old value first after insert the
			 * new one
			 */
			if (insertionPropertyValue.isPropertyHasSingleValue()) {

				/*
				 * I need to delete the old value and then insert the new value
				 * 
				 * delete the old value
				 */
				deleteClauseBuilder
						.append("?subject " + insertionPropertyValue.getPropertyName() + " ?var" + varNum[0] + " . \n");

				/*
				 * add it to where clause in order to get the old value that
				 * will be deleted
				 */

				whereClauseBuilder.append("OPTIONAL { \n");
				whereClauseBuilder
						.append("?subject " + insertionPropertyValue.getPropertyName() + " ?var" + varNum[0] + " . \n");
				whereClauseBuilder.append("} \n");

				/*
				 * increment varNum[0] to avoid having the same variable again
				 */
				varNum[0]++;
			}

			/*
			 * checking if the propertyValue is the last one to end the
			 * statement with .
			 */
			if (start) {
				insertClauseBuilder.append("?subject " + insertionPropertyValue.getPropertyName() + "  "
						+ insertionPropertyValue.getValue().toString());
				start = false;
			} else {
				insertClauseBuilder.append("; \n" + insertionPropertyValue.getPropertyName() + "  "
						+ insertionPropertyValue.getValue().toString());
			}

		}

		insertClauseBuilder.append(". \n");

	}

	private static Object getValue(Property property, Object value) {

		if (property instanceof DataTypeProperty) {
			XSDDatatype xsdDataType = ((DataTypeProperty) property).getDataType();
			value = "\"" + value.toString() + "\"" + xsdDataType.getXsdType();
			return value;
		} else {
			return Prefix.IOT_PLATFORM.getPrefix() + value.toString().toLowerCase().replaceAll(" ", "");
		}
	}

	private static Object getValue(Class subjectClass, String prefixedProperyName, Object value) {

		int index = prefixedProperyName.indexOf(":");
		String propertyName = prefixedProperyName.substring(index + 1);

		Property property = subjectClass.getProperties().get(propertyName);

		if (property instanceof DataTypeProperty) {
			XSDDatatype xsdDataType = ((DataTypeProperty) property).getDataType();
			value = "\"" + value.toString() + "\"" + xsdDataType.getXsdType();
			return value;
		} else {
			return Prefix.IOT_PLATFORM.getPrefix() + value.toString().toLowerCase().replaceAll(" ", "");
		}
	}

	/**
	 * constructUpdateQueryToUpdateUniqueIdentifierValue is used to construct
	 * query to update uniqueIdentifier (individual's subject) value of an
	 * individual with param(oldUniqueIdentifier)
	 * 
	 * Here I must update both the value of subject of the individual
	 * 
	 * @param requestSubjectClassName
	 *            className of the updateRequestClass (passed in the
	 *            updateRequest URL)
	 * 
	 * @param newUniqueIdentifierValue
	 *            new Value for uniqueIdentifierProperty and subject
	 * 
	 * @param oldUniqueIdentifier
	 *            old uniqueIdentifier value that will be used to get the target
	 *            individual to be updated
	 * 
	 * @return return update query that is constructed
	 */
	public static String constructUpdateQueryToUpdateUniqueIdentifierValue(Class subjectClass,
			Object newUniqueIdentifierValue, String oldUniqueIdentifier, String applicationModelName) {

		StringBuilder updateQueryBuilder = new StringBuilder();

		String newPrefixedSubjectUniqueIdentfier = Prefix.IOT_PLATFORM.getPrefix()
				+ newUniqueIdentifierValue.toString().toLowerCase().replaceAll(" ", "");

		if (prefixesString == null) {
			StringBuilder prefixStringBuilder = new StringBuilder();
			for (Prefix prefix : Prefix.values()) {
				prefixStringBuilder.append("PREFIX	" + prefix.getPrefix() + "	<" + prefix.getUri() + ">\n");
			}

			prefixesString = prefixStringBuilder.toString();
		}

		updateQueryBuilder.append(prefixesString);
		updateQueryBuilder.append("DELETE  \n ");
		updateQueryBuilder.append("{ ?subject ?property ?value . } \n");
		updateQueryBuilder.append("INSERT \n");
		updateQueryBuilder.append("{ " + newPrefixedSubjectUniqueIdentfier + " a "
				+ subjectClass.getPrefix().getPrefix() + subjectClass.getName() + "; ?property ?value . } \n");
		updateQueryBuilder.append("WHERE \n");
		updateQueryBuilder.append("{ \n  ?subject ?property ?value . \n ");
		updateQueryBuilder.append(
				"FILTER( ?subject = iot-platform:" + oldUniqueIdentifier.toLowerCase().replaceAll(" ", "") + " ) \n");
		updateQueryBuilder.append(" } \n");

		return updateQueryBuilder.toString();

	}

}
