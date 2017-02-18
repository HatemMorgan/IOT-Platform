package com.iotplatform.daos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;

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

	@Autowired
	public ApplicationDAO(Oracle oracle) {
		System.out.println("ApplicationDAO Created");
		this.oracle = oracle;
	}

	public boolean checkIfApplicationModelExsist(String applicationName) {

		try {
			String modelConventionName = applicationName.toUpperCase() + suffix;
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
			String modelConventionName = applicationName.toUpperCase() + suffix;
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
			String modelConventionName = applicationName.toUpperCase() + suffix;
			OracleUtils.dropSemanticModel(oracle, modelConventionName);
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

//	public static void main(String[] args) {
//		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
//		String szUser = "rdfusr";
//		String szPasswd = "rdfusr";
//		
//		// String szJdbcDriver = "oracle.jdbc.driver.OracleDriver";
//		// BasicDataSource dataSource = new BasicDataSource();
//		// dataSource.setDriverClassName(szJdbcDriver);
//		// dataSource.setUrl(szJdbcURL);
//		// dataSource.setUsername(szUser);
//		// dataSource.setPassword(szPasswd);
//
//		ApplicationDAO applicationDAO = new ApplicationDAO(new Oracle(szJdbcURL, szUser, szPasswd));
//		System.out.println("Application Found :"+applicationDAO.checkIfApplicationModelExsist("test"));
//		System.out.println("Application Created : "+applicationDAO.createNewApplicationModel("test"));
//		System.out.println("Application Found :"+applicationDAO.checkIfApplicationModelExsist("test"));
//		System.out.println("Application Drooped : "+applicationDAO.dropApplicationModel("test"));
//		System.out.println("Application Found :"+applicationDAO.checkIfApplicationModelExsist("test"));
//
//	}
}
