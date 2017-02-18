package com.iotplatform.daos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;

/*
 *  Application Data Access Object which is responsible for dealing with application graph in database
 */
@Component
public class ApplicationDAO extends JdbcDaoSupport {
	private JdbcTemplate jdbcTemplate;

	@Autowired
	public ApplicationDAO(DataSource dataSource) {
		System.out.println("ApplicationDAO Created");
		this.setDataSource(dataSource);
		this.jdbcTemplate = this.getJdbcTemplate();
	}

	public boolean checkIfApplicationModelExsist(String applicationName) {

		try {
			String queryString = "SELECT COUNT(*) FROM  MDSYS.SEM_MODEL$ WHERE MODEL_NAME= ? ";
			PreparedStatement stat = jdbcTemplate.getDataSource().getConnection().prepareStatement(queryString);
			stat.setString(1, applicationName);
			ResultSet resultSet = stat.executeQuery();
			resultSet.next();
			int result = resultSet.getInt(1);
			System.out.println(result);

			/*
			 * testing using oracle insted of jdbc and it works the same stat=
			 * oracle.getConnection().prepareStatement(queryString);
			 * stat.setString(1, applicationName); resultSet =
			 * stat.executeQuery(); resultSet.next(); result =
			 * resultSet.getInt(1); System.out.println(result);
			 */

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
	
	public boolean createNewApplicationModel(String applicationName){
		
		return false;
	}

//	public static void main(String[] args) {
//		String szJdbcDriver = "oracle.jdbc.driver.OracleDriver";
//		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
//		String szUser = "rdfusr";
//		String szPasswd = "rdfusr";
//		BasicDataSource dataSource = new BasicDataSource();
//		dataSource.setDriverClassName(szJdbcDriver);
//		dataSource.setUrl(szJdbcURL);
//		dataSource.setUsername(szUser);
//		dataSource.setPassword(szPasswd);
//
//		ApplicationDAO applicationDAO = new ApplicationDAO(dataSource);
//		applicationDAO.checkIfApplicationModelExsist("TEST");
//	}
}
