package com.iotplatform.daos;

import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.jena.update.UpdateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.ontology.Class;
import com.iotplatform.queries.DeleteQuery;
import com.iotplatform.utilities.DeletePropertyValueUtility;

import oracle.spatial.rdf.client.jena.DatasetGraphOracleSem;
import oracle.spatial.rdf.client.jena.GraphOracleSem;
import oracle.spatial.rdf.client.jena.Oracle;

@Repository("deleteDao")
public class DeleteDao {

	private Oracle oracle;

	@Autowired
	public DeleteDao(Oracle oracle) {
		this.oracle = oracle;
	}

	public void deletePatternOfIndividual(String applicationModelName, String individualUniqueIdentifier,
			Class requestedSubjectClass, ArrayList<DeletePropertyValueUtility> deletePropValueList) {

		try {

			String deleteQuery = DeleteQuery.constructDeleteQuery(deletePropValueList, individualUniqueIdentifier,
					requestedSubjectClass);

			System.out.println(deleteQuery);

			GraphOracleSem graphOracleSem = new GraphOracleSem(oracle, applicationModelName);
			DatasetGraphOracleSem dsgos = DatasetGraphOracleSem.createFrom(graphOracleSem);

			UpdateAction.parseExecute(deleteQuery, dsgos);
			dsgos.close();

		} catch (SQLException ex) {
			throw new DatabaseException(ex.getMessage(), "Update API");
		}

	}

	public void deleteIndividual(String applicationModelName, String individualUniqueIdentifier,
			Class requestedSubjectClass) {

		try {

			String deleteQuery = DeleteQuery.constructDeleteQueryToDeleteFullIndivdual(individualUniqueIdentifier,
					requestedSubjectClass);

			System.out.println(deleteQuery);

			GraphOracleSem graphOracleSem = new GraphOracleSem(oracle, applicationModelName);
			DatasetGraphOracleSem dsgos = DatasetGraphOracleSem.createFrom(graphOracleSem);

			UpdateAction.parseExecute(deleteQuery, dsgos);
			dsgos.close();

		} catch (SQLException ex) {
			throw new DatabaseException(ex.getMessage(), "Update API");
		}

	}

}
