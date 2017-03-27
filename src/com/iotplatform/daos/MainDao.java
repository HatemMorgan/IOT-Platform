package com.iotplatform.daos;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

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

import oracle.spatial.rdf.client.jena.ModelOracleSem;
import oracle.spatial.rdf.client.jena.Oracle;

/*
 * MainDao is used to insert triples to application model
 */

@Repository("mainDao")
public class MainDao {

	private Oracle oracle;
	private static String prefixesString = null;
	// private SelectionUtility selectionUtility;

	@Autowired
	public MainDao(Oracle oracle) {
		this.oracle = oracle;
		// this.selectionUtility = selectionUtility;
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

}
