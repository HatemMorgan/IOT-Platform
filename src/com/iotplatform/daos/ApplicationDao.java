package com.iotplatform.daos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.update.UpdateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.classes.Application;
import com.iotplatform.utilities.PropertyValue;
import com.iotplatform.utilities.QueryUtility;

import oracle.spatial.rdf.client.jena.ModelOracleSem;
import oracle.spatial.rdf.client.jena.Oracle;
import oracle.spatial.rdf.client.jena.OracleUtils;

/*
 *  Application Data Access Object which is responsible for dealing with application graph in database
 */

@Repository("applicationDao")
public class ApplicationDao {
	private Oracle oracle;
	private final String suffix = "_MODEL";
	private Application applicationClass;

	private Hashtable<String, String> htblApplicationNameModelName;

	@Autowired
	public ApplicationDao(Oracle oracle, Application applicationClass) {
		this.oracle = oracle;
		this.applicationClass = applicationClass;
		this.htblApplicationNameModelName = new Hashtable<>();
	}

	public boolean checkIfApplicationModelExsist(String applicationName) {

		try {

			if (htblApplicationNameModelName.containsKey(applicationName)) {
				return true;
			}

			String modelConventionName = applicationName.replaceAll(" ", "").toUpperCase() + suffix;
			String queryString = "SELECT COUNT(*) FROM  MDSYS.SEM_MODEL$ WHERE MODEL_NAME= ? ";

			PreparedStatement stat = oracle.getConnection().prepareStatement(queryString);
			stat.setString(1, modelConventionName);
			ResultSet resultSet = stat.executeQuery();
			resultSet.next();
			int result = resultSet.getInt(1);
			if (result == 1) {
				htblApplicationNameModelName.put(applicationName, modelConventionName);
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;

	}

	/*
	 * createNewApplicationModel method creates a new application model in the
	 * database that will hold the application data represented in a graph
	 */
	public boolean createNewApplicationModel(String applicationName) {
		try {
			String modelConventionName = applicationName.replaceAll(" ", "").toUpperCase() + suffix;
			Model newModel = ModelOracleSem.createOracleSemModel(oracle, modelConventionName);
			newModel.close();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * dropApplicationModel method drops an application model and removes all
	 * the data associated with this model
	 */
	public boolean dropApplicationModel(String applicationName) {
		try {
			String modelConventionName = applicationName.replaceAll(" ", "").toUpperCase() + suffix;
			OracleUtils.dropSemanticModel(oracle, modelConventionName);
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * insertApplication method inserts a new Application and the input is
	 * Application name to ge the model name from it and a hashtable with the
	 * property name appended to its prefix as a key
	 * (eg:iot-plaform:applicationDescription) and the object of the property as
	 * the value
	 */
	public void insertApplication(ArrayList<PropertyValue> prefixedPropertyValue, String applicationName) {
		String applicationModelName = applicationName.replaceAll(" ", "").toUpperCase() + suffix;

		String subject = applicationName.replaceAll(" ", "").toLowerCase();
		String insertQuery = QueryUtility.constructInsertQuery(Prefixes.IOT_PLATFORM.getPrefix() + subject,
				applicationClass, prefixedPropertyValue);
		try {

			ModelOracleSem model = ModelOracleSem.createOracleSemModel(oracle, applicationModelName);
			UpdateAction.parseExecute(insertQuery, model);
			model.close();

		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage(), "Application");
		}

	}

	/*
	 * getApplication method perform a query on application data graph
	 */
	public Hashtable<String, Object> getApplication(String applicationName) {
		String applicationModelName = applicationName.replaceAll(" ", "").toUpperCase() + suffix;
		long startTime = System.currentTimeMillis();
		System.out.println("Started at : " + startTime / 1000);

		String subject = Prefixes.IOT_PLATFORM.getPrefix() + applicationName.replaceAll(" ", "").toLowerCase();
		Hashtable<String, Object> results = new Hashtable<>();

		ResultSetMetaData metadata;
		try {
			String queryString = "select p,o from table(sem_match('select  ?p ?o where {" + subject + "  ?p ?o.}',"
					+ "SEM_Models('" + applicationModelName + "'), null,"
					+ "SEM_ALIASES(SEM_ALIAS('iot-platform','http://iot-platform#')),null))";

			System.out.println(queryString);
			java.sql.ResultSet res = oracle.executeQuery(queryString, 0, 1);
			metadata = res.getMetaData();
			// int columnCount = metadata.getColumnCount();

			while (res.next()) {

				results.put(res.getString(1), res.getObject(2));

			}
			System.out.println(
					"test selecting: elapsed time (sec): " + ((System.currentTimeMillis() - startTime) / 1000));
			return results;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public Hashtable<String, Object> getOntology(String applicationName) {
		String applicationModelName = applicationName.replaceAll(" ", "").toUpperCase() + suffix;
		long startTime = System.currentTimeMillis();
		System.out.println("Started at : " + startTime / 1000);

		String subject = Prefixes.IOT_PLATFORM.getPrefix() + applicationName.replaceAll(" ", "").toLowerCase();
		Hashtable<String, Object> results = new Hashtable<>();

		ResultSetMetaData metadata;
		try {
			String queryString = "select s,o from table(sem_match(' select  ?s ?o where {?s ?p ?o.}',SEM_Models('MAIN_ONTOLOGY_MODEL'), null,SEM_ALIASES(SEM_ALIAS('iot-platform','http://iot-platform#')),null))";
			System.out.println(queryString);
			java.sql.ResultSet res = oracle.executeQuery(queryString, 0, 1);
			metadata = res.getMetaData();
			int columnCount = metadata.getColumnCount();

			while (res.next()) {
				results.put(res.getString(1), res.getObject(2));

			}
			System.out.println(
					"test selecting: elapsed time (sec): " + ((System.currentTimeMillis() - startTime) / 1000));
			return results;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public Hashtable<String, String> getHtblApplicationNameModelName() {
		return htblApplicationNameModelName;
	}

	public static void main(String[] args) {
		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		String szUser = "rdfusr";
		String szPasswd = "rdfusr";

		// String szJdbcDriver = "oracle.jdbc.driver.OracleDriver";
		// BasicDataSource dataSource = new BasicDataSource();
		// dataSource.setDriverClassName(szJdbcDriver);
		// dataSource.setUrl(szJdbcURL);
		// dataSource.setUsername(szUser);
		// dataSource.setPassword(szPasswd);

		ApplicationDao applicationDAO = new ApplicationDao(new Oracle(szJdbcURL, szUser, szPasswd), new Application());

		// test creation and dropping models
		// System.out.println("Application Found :" +
		// applicationDAO.checkIfApplicationModelExsist("test"));
		// System.out.println("Application Created :" +
		// applicationDAO.createNewApplicationModel("test"));
		// System.out.println("Application Found :" +
		// applicationDAO.checkIfApplicationModelExsist("test"));
		// System.out.println("Application Drooped : " +
		// applicationDAO.dropApplicationModel("test"));
		// System.out.println("Application Found :" +
		// applicationDAO.checkIfApplicationModelExsist("test"));

		// test inserting a new application
		// System.out.println("Application dropped :" +
		// applicationDAO.dropApplicationModel("Test App"));
		// System.out.println("Application Found :" +
		// applicationDAO.checkIfApplicationModelExsist("Test App"));
		// System.out.println("Application Created :" +
		// applicationDAO.createNewApplicationModel("Test App"));
		//
		// Hashtable<String, Object> htblPropValue = new Hashtable<>();
		// htblPropValue.put("iot-platform:description",
		// "\"Test Application for testing purpose\"" +
		// XSDDataTypes.string_typed.getXsdType());
		// htblPropValue.put("iot-platform:name", "\"Test Application\"" +
		// XSDDataTypes.string_typed.getXsdType());
		// System.out.println("");
		// applicationDAO.insertApplication(htblPropValue, "Test App");

		// Testing select query of an application
		// System.out.println("Application Found :" +
		// applicationDAO.checkIfApplicationModelExsist("Test App"));
		// Hashtable<String, Object> res = applicationDAO.getApplication("Test
		// App");
		// System.out.println(res.toString());
	}
}
