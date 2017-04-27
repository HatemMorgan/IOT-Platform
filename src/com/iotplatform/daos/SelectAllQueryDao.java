package com.iotplatform.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.ontology.Class;
import com.iotplatform.queries.SelectionQuery;
import com.iotplatform.query.results.SelectionQueryResults;

import oracle.spatial.rdf.client.jena.Oracle;

/*
 * SelectAllQueryDao is used to make selectAll queries on data
 */

@Repository("selectAllQueryDao")
public class SelectAllQueryDao {

	private Oracle oracle;

	private SelectionQueryResults selectionUtility;

	@Autowired
	public SelectAllQueryDao(Oracle oracle, SelectionQueryResults selectionUtility) {
		this.oracle = oracle;
		this.selectionUtility = selectionUtility;
	}

	public List<LinkedHashMap<String, Object>> selectAll(String applicationModelName, Class subjectClass) {

		/*
		 * call selectionQuery to construct the query
		 */
		String queryString = SelectionQuery.constructSelectAllQueryNoFilters(subjectClass, applicationModelName);
		List<LinkedHashMap<String, Object>> subjectClassIndividualsList = new ArrayList<>();

		try {
			ResultSet results = oracle.executeQuery(queryString, 0, 1);

			/*
			 * call constractResponeJsonObjectForListSelection method in
			 * selectionUtility class to construct the response json
			 */
			subjectClassIndividualsList = selectionUtility.constractResponeJsonObjectForListSelection(results,
					subjectClass, applicationModelName);

		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage(), subjectClass.getName());
		}

		return subjectClassIndividualsList;
	}

}
