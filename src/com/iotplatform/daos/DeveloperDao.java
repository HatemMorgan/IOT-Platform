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

@Repository("developerDao")
public class DeveloperDao {

	private Oracle oracle;
	private SelectionQueryResults selectionUtility;

	@Autowired
	public DeveloperDao(Oracle oracle, SelectionQueryResults selectionUtility) {
		this.oracle = oracle;
		this.selectionUtility = selectionUtility;
	}

	/*
	 * getDevelopers method returns all the developers in the passed application
	 * model
	 */

	public List<Hashtable<String, Object>> getDevelopers(String applicationModelName) {

		String applicationName = applicationModelName.replaceAll(" ", "").toUpperCase().substring(0,
				applicationModelName.length() - 6);

		String queryString = SelectionQuery.constructSelectAllQueryNoFilters(
				OntologyMapper.getHtblMainOntologyClassesMappers().get("developer"), applicationModelName);
		System.out.println(queryString);
		List<Hashtable<String, Object>> developersList = new ArrayList<>();

		try {
			ResultSet results = oracle.executeQuery(queryString, 0, 1);

			/*
			 * call constractResponeJsonObjectForListSelection method in
			 * selectionUtility class to construct the response json
			 */

			developersList = selectionUtility.constractResponeJsonObjectForListSelection(applicationName, results,
					OntologyMapper.getHtblMainOntologyClassesMappers().get("developer"));

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), "Developer");
		}

		return developersList;
	}

}
