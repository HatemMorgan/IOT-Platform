package com.iotplatform.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.ontology.mapers.OntologyMapper;
import com.iotplatform.queries.SelectionQuery;
import com.iotplatform.query.results.SelectionQueryResults;

import oracle.spatial.rdf.client.jena.Oracle;

@Repository("normalUserDao")
public class NormalUserDao {

	private Oracle oracle;
	private SelectionQueryResults selectionUtility;

	@Autowired
	public NormalUserDao(Oracle oracle, SelectionQueryResults selectionUtility) {
		this.oracle = oracle;
		this.selectionUtility = selectionUtility;
	}

	/*
	 * getNormalUsers method returns all the normal users in the passed
	 * application model
	 */
	public List<Hashtable<String, Object>> getNormalUsers(String applicationModelName) {

		String applicationName = applicationModelName.replaceAll(" ", "").toUpperCase().substring(0,
				applicationModelName.length() - 6);

		String queryString = SelectionQuery.constructSelectAllQueryNoFilters(
				OntologyMapper.getHtblMainOntologyClassesMappers().get("normaluser"), applicationModelName);
		List<Hashtable<String, Object>> normalUsersList = new ArrayList<>();

		try {
			ResultSet results = oracle.executeQuery(queryString, 0, 1);
			/*
			 * call constractResponeJsonObjectForListSelection method in
			 * selectionUtility class to construct the response json
			 */

			normalUsersList = selectionUtility.constractResponeJsonObjectForListSelection(applicationName, results,
					OntologyMapper.getHtblMainOntologyClassesMappers().get("normaluser"));

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), "Normal User");
		}

		return normalUsersList;
	}

}
