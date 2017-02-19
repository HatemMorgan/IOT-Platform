package com.iotplatform.daos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.update.UpdateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;
import com.iotplatform.ontology.classes.Application;

import oracle.spatial.rdf.client.jena.ModelOracleSem;
import oracle.spatial.rdf.client.jena.Oracle;
import oracle.spatial.rdf.client.jena.OracleUtils;

/*
 *  Application Data Access Object which is responsible for dealing with application graph in database
 */
@Component
public class ApplicationDAO {
	private Oracle oracle;
	private final String suffix = "_MODEL";
	private Application applicationClass;

	@Autowired
	public ApplicationDAO(Oracle oracle, Application applicationClass) {
		System.out.println("ApplicationDAO Created");
		this.oracle = oracle;
		this.applicationClass = applicationClass;
	}

	public boolean checkIfApplicationModelExsist(String applicationName) {

		try {
			String modelConventionName = applicationName.replaceAll(" ", "").toUpperCase() + suffix;
			String queryString = "SELECT COUNT(*) FROM  MDSYS.SEM_MODEL$ WHERE MODEL_NAME= ? ";

			PreparedStatement stat = oracle.getConnection().prepareStatement(queryString);
			stat.setString(1, modelConventionName);
			ResultSet resultSet = stat.executeQuery();
			resultSet.next();
			int result = resultSet.getInt(1);
			if (result == 1) {
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
	public int insertApplication(Hashtable<String, Object> htblPropValue, String applicationName) {
		String applicationModelName = applicationName.replaceAll(" ", "").toUpperCase() + suffix;

		ModelOracleSem model;
		try {
			model = ModelOracleSem.createOracleSemModel(oracle, applicationModelName);
			Set<String> propertyNameList = htblPropValue.keySet();
			Iterator<String> iterator = propertyNameList.iterator();

			String subject = Prefixes.IOT_PLATFORM.getPrefix() + applicationName.replaceAll(" ", "").toLowerCase();
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(
					"PREFIX " + Prefixes.IOT_PLATFORM.getPrefix() + "<" + Prefixes.IOT_PLATFORM.getUri() + ">\n");
			stringBuilder.append("PREFIX " + Prefixes.XSD.getPrefix() + "<" + Prefixes.XSD.getUri() + ">\n");
			stringBuilder.append("INSERT DATA { \n");
			stringBuilder.append(subject + " a " + "<" + applicationClass.getUri() + "> ; \n");
			while (iterator.hasNext()) {
				String property = iterator.next();
				Object value = htblPropValue.get(property);

				/*
				 * check if it is the last propertyValue to be inserted inorder
				 * to end the query
				 */

				if (iterator.hasNext()) {
					stringBuilder.append(property + " " + value + " ; \n");
				} else {
					stringBuilder.append(property + " " + value + " . \n }");
				}

			}
			System.out.println(stringBuilder.toString());
			UpdateAction.parseExecute(stringBuilder.toString(), model);
			model.close();
			return 1;
		} catch (SQLException e1) {
			e1.printStackTrace();

		}

		return 0;
	}

	/*
	 * getApplication method perform a query on application data graph
	 */
	public Hashtable<String, Object> getApplication(String applicationName){
		return null;
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

		ApplicationDAO applicationDAO = new ApplicationDAO(new Oracle(szJdbcURL, szUser, szPasswd), new Application());

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
		// htblPropValue.put("iot-platform:description", "\"Test Application for
		// testing purpose\""+XSDDataTypes.string_typed.getXsdType());
		// htblPropValue.put("iot-platform:name", "\"Test
		// Application\""+XSDDataTypes.string_typed.getXsdType());
		// System.out.println("");
		// applicationDAO.insertApplication(htblPropValue, "Test App");

	}
}
