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

		StringBuilder updateQueryBuilder = new StringBuilder();

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
	public static String constructUpdateQueryToUpdateUniqueIdentifierValue(String requestSubjectClassName,
			Object newUniqueIdentifierValue, String oldUniqueIdentifier, String applicationModelName) {

		StringBuilder updateQueryBuilder = new StringBuilder();

		String newPrefixedSubjectUniqueIdentfier = Prefix.IOT_PLATFORM.getPrefix() + newUniqueIdentifierValue;

		if (prefixesString == null) {
			StringBuilder prefixStringBuilder = new StringBuilder();
			for (Prefix prefix : Prefix.values()) {
				prefixStringBuilder.append("PREFIX	" + prefix.getPrefix() + "	<" + prefix.getUri() + ">\n");
			}

			prefixesString = prefixStringBuilder.toString();
		}

		/*
		 * get subjectClass from dynamicOntology cache if it exist
		 */
		Class subjectClass = null;
		if ((DynamicOntologyMapper.getHtblappDynamicOntologyClasses().contains(applicationModelName)
				&& DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
						.containsKey(requestSubjectClassName.toLowerCase()))) {

			subjectClass = DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
					.get(requestSubjectClassName.toLowerCase());

		} else {

			if (OntologyMapper.getHtblMainOntologyClassesMappers().containsKey(requestSubjectClassName.toLowerCase())) {

				/*
				 * get the objectClass from MainOntologyClassesMapper
				 */
				subjectClass = OntologyMapper.getHtblMainOntologyClassesMappers()
						.get(requestSubjectClassName.toLowerCase());

			}
		}

		updateQueryBuilder.append(prefixesString);
		updateQueryBuilder.append("DELETE  \n ");
		updateQueryBuilder.append("{ ?subject ?property ?value . } \n");
		updateQueryBuilder.append("INSERT \n");
		updateQueryBuilder.append("{ " + newPrefixedSubjectUniqueIdentfier + " a "
				+ subjectClass.getPrefix().getPrefix() + subjectClass.getName() + "; ?property ?value . } \n");
		updateQueryBuilder.append("WHERE \n");
		updateQueryBuilder.append("{ ?subject ?property ?value . } ");

		return updateQueryBuilder.toString();

	}

}
