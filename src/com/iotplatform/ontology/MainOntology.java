package com.iotplatform.ontology;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;

import org.apache.jena.graph.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.*;
import oracle.spatial.rdf.client.jena.*;

@Component
public class MainOntology {

	private Oracle oracle;
	private final String MODEL_NAME = "MAIN_ONTOLOGY_MODEL";

	@Autowired
	public MainOntology(Oracle oracle) {
		System.out.println("Main ontology bean created");
		this.oracle = oracle;
		loadOntology();
	}

	/*
	 * checkIfOntologyLoaded method checks if the ontology was loaded before or
	 * not
	 */
	private boolean checkIfOntologyLoaded() {

		String queryString = "SELECT COUNT(*) FROM  MDSYS.SEM_MODEL$ WHERE MODEL_NAME='" + MODEL_NAME + "'";
		ResultSet resultSet;
		try {

			// java.sql.Statement stmt =
			// jdbcTemplate.getDataSource().getConnection().createStatement();
			// resultSet = stmt.executeQuery(queryString);
			// resultSet.next();
			// int result = resultSet.getInt(1);
			// System.out.println(result);
			resultSet = oracle.executeQuery(queryString);
			resultSet.next();
			int result = resultSet.getInt(1);
			System.out.println(result);
			if (result == 1) {
				return true;
			} else {
				return false;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	/*
	 * loadOntology method loads the ontology into database main_ontology_model
	 * database model
	 */
	private void loadOntology() {

		// checking if the ontology was loaded before
		if (checkIfOntologyLoaded()) {

			System.out.println("Ontology was loaded before");

		} else {
			System.out.println("not loaded");
			// perform batch load to load the ontology to main_ontology_model

			// read ontology turtle file for file system
			Model model = ModelFactory.createDefaultModel();
			try {
				InputStream is = FileManager.get().open("iot-platform.n3");
				model.read(new InputStreamReader(is), "http://iot-platform", "TURTLE");
				is.close();
			} catch (IOException e) {
				System.out.println("Cannot load ontology file : " + e.getMessage());
				e.printStackTrace();
			}

			// create model to load triples
			try {
				Model modelDest = ModelOracleSem.createOracleSemModel(oracle, MODEL_NAME);
				// getting default graph of the created model to load triples
				// into
				// it
				GraphOracleSem g = (GraphOracleSem) modelDest.getGraph();

				// dropping table index to enhance performance of batch loading
				try {
					g.dropApplicationTableIndex();
				} catch (SQLException sqle) {
					System.out.println("Error Cannot drop table index: " + sqle.getMessage());
					modelDest.close();
					sqle.printStackTrace();
				}

				long startTime = System.currentTimeMillis();
				System.out.println("Start batch load");
				try {
					// perform batch loading
					((OracleBulkUpdateHandler) g.getBulkUpdateHandler()).addInBatch(GraphUtil.findAll(model.getGraph()),
							oracle.getConnection().getUserName());
					System.out.println("End size " + modelDest.size());

					// recreate table index again
					g.rebuildApplicationTableIndex();

					System.out.println(
							"testLoadReal: elapsed time (sec): " + ((System.currentTimeMillis() - startTime) / 1000));

				} catch (SQLException e) {

					System.out.println("Cannot batch load ontology : " + e.getMessage());

					try {

						// Drop created model after failing to batch load the
						// ontology
						OracleUtils.dropSemanticModel(oracle, MODEL_NAME);
					} catch (SQLException e1) {
						e1.printStackTrace();
					} finally {
						modelDest.close();
						e.printStackTrace();
					}

				}
				modelDest.close();
			} catch (SQLException e) {

				e.printStackTrace();
			} finally {

			}

		}

	}

//	public static void main(String[] args) throws SQLException {
//
//		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
//		String szUser = "rdfusr";
//		String szPasswd = "rdfusr";
//		Oracle oracle = new Oracle(szJdbcURL, szUser, szPasswd);
//		MainOntology main = new MainOntology(oracle);
//		OracleUtils.dropSemanticModel(oracle, "MAIN_ONTOLOGY_MODEL");
//	}
}
