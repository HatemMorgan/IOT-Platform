package com.iotplatform.queries;

import java.util.ArrayList;
import java.util.Hashtable;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.mapers.DynamicOntologyMapper;
import com.iotplatform.ontology.mapers.OntologyMapper;
import com.iotplatform.utilities.InsertionPropertyValue;
import com.iotplatform.utilities.UpdatePropertyValueUtility;

public class UpdateQuery {

	private static String prefixesString = null;

	public static String constructUpdateQuery(String applicationModelName, String requestSubjectClassName,
			ArrayList<UpdatePropertyValueUtility> updatePropertyValueList,
			Hashtable<String, ArrayList<ArrayList<InsertionPropertyValue>>> htblClassInsertionPropertyValue) {

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
		StringBuilder insertClauseBuiler = new StringBuilder();

		/*
		 * whereClauseBuilder is the where part builder
		 */
		StringBuilder whereClauseBuilder = new StringBuilder();

		return updateQueryBuilder.toString();
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
				+ newUniqueIdentifierValue.toString().toLowerCase();

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
		updateQueryBuilder.append("FILTER( ?subject = iot-platform:" + oldUniqueIdentifier + " ) \n");
		updateQueryBuilder.append(" } \n");

		return updateQueryBuilder.toString();

	}

}
