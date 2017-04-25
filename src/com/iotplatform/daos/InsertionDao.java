package com.iotplatform.daos;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.jena.update.UpdateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.ontology.Class;
import com.iotplatform.queries.InsertionQuery;
import com.iotplatform.utilities.InsertionPropertyValue;

import oracle.spatial.rdf.client.jena.ModelOracleSem;
import oracle.spatial.rdf.client.jena.Oracle;

/*
 * MainDao is used to insert triples to application model
 * 
 * 1- Call InsertionQuery to create the insertQuery from passed input from InsertionRequestValidation
 * 2- Insert data into database
 * 
 */

@Repository("insertDao")
public class InsertionDao {

	private Oracle oracle;

	@Autowired
	public InsertionDao(Oracle oracle) {
		this.oracle = oracle;
	}

	/*
	 * insertData method insert new triples to passed application model
	 * 
	 * I takes Hashtable<Class, ArrayList<ArrayList<PropertyValue>>>
	 * htblClassPropertyValue as an input from InsertRequestValidation that is
	 * used to validate and parse the request body
	 * 
	 * It also takes the applicationModelName and requestSubjectClassName from
	 * InsertionService
	 */
	public void insertData(String applicationModelName, String requestSubjectClassName,
			Hashtable<Class, ArrayList<ArrayList<InsertionPropertyValue>>> htblClassPropertyValue) {
		try {

			String insertQuery = InsertionQuery.constructInsertQuery(applicationModelName, requestSubjectClassName,
					htblClassPropertyValue);

			System.out.println(insertQuery);

			/*
			 * execute query
			 */
			ModelOracleSem model = ModelOracleSem.createOracleSemModel(oracle, applicationModelName);
			UpdateAction.parseExecute(insertQuery, model);
			model.close();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), requestSubjectClassName);
		}

	}

}
