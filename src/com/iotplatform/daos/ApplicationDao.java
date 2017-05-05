package com.iotplatform.daos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Hashtable;

import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.ontology.Prefix;

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

	private Hashtable<String, String> htblApplicationNameModelName;

	@Autowired
	public ApplicationDao(Oracle oracle) {
		this.oracle = oracle;
		this.htblApplicationNameModelName = new Hashtable<>();
	}

	public boolean checkIfApplicationModelExsist(String applicationName) {
		applicationName = applicationName.toLowerCase().replaceAll(" ", "");
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
			htblApplicationNameModelName.put(applicationName.toLowerCase().replaceAll(" ", ""), modelConventionName);
			return true;
		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage(), "Application");
		}
	}

	/*
	 * dropApplicationModel method drops an application model and removes all
	 * the data associated with this model
	 */
	public boolean dropApplicationModel(String applicationName) {
		try {
			String modelConventionName = applicationName.replaceAll(" ", "").toUpperCase() + suffix;
			OracleUtils.dropSemanticModel(oracle, modelConventionName);

			if (htblApplicationNameModelName.containsKey(applicationName.toLowerCase().replaceAll(" ", ""))) {
				htblApplicationNameModelName.remove(applicationName.toLowerCase().replaceAll(" ", ""));
			}

			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
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

		ApplicationDao applicationDAO = new ApplicationDao(new Oracle(szJdbcURL, szUser, szPasswd));

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
	}
}
