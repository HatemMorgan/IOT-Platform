package com.iotplatform.daos;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import com.iotplatform.models.DynamicConceptModel;

@Repository("dynamicConceptsDao")
public class DynamicConceptDao extends JdbcDaoSupport {

	private DynamicConceptModel dynamicConceptModel;
	private JdbcTemplate jdbcTemplate;

	@Autowired
	public DynamicConceptDao(DataSource dataSource, DynamicConceptModel dynamicConceptModel) {
		this.dynamicConceptModel = dynamicConceptModel;
		this.setDataSource(dataSource);
		this.jdbcTemplate = this.getJdbcTemplate();
		
		if(! wasTableCreated()){
			CreateTable();
			System.out.println("DYNAMIC_CONCEPTS table created successfully");
		}else{
			System.out.println("DYNAMIC_CONCEPTS table was created before");
		}
		
	}

	/*
	 * wasTableCreated method checks if DYNAMIC_CONCEPTS table was created
	 * before or not to avoid raising error if it was created before.
	 */
	private boolean wasTableCreated() {

		String queryString = "SELECT COUNT(*) FROM ALL_TABLES WHERE TABLE_NAME= 'DYNAMIC_CONCEPTS'";

		int count = getJdbcTemplate().queryForObject(queryString, new Object[] {}, Integer.class);

		if (count == 1) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * CreateTable method used to create DYNAMIC_CONCEPTS table to hold data for
	 * dynamic added properties or classes for a specific application domain
	 */
	private void CreateTable() {

		String queryString = "CREATE TABLE DYNAMIC_CONCEPTS (" + "application_name VARCHAR(50) ,"
				+ "class_name VARCHAR(50), class_uri VARCHAR(50), class_prefix_uri VARCHAR(20),"
				+ "class_prefix_alias VARCHAR(20), property_name VARCHAR(50), property_uri VARCHAR(50),"
				+ "property_prefix_uri VARCHAR(50), property_prefix_alias VARCHAR(50),"
				+ "property_type VARCHAR(10), property_object_type VARCHAR(20),"
				+ "CONSTRAINT dynamic_concept_table_uk PRIMARY KEY(application_name,class_uri,property_uri) )";
		try {
			jdbcTemplate.execute(queryString);
		} catch (DataAccessException e) {
			System.out.println(e.getMessage());
			System.out.println(e.getClass());
			System.out.println(e.getStackTrace());
		}

	}

	public static void main(String[] args) {
		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		String szUser = "rdfusr";
		String szPasswd = "rdfusr";

		 String szJdbcDriver = "oracle.jdbc.driver.OracleDriver";
		 BasicDataSource dataSource = new BasicDataSource();
		 dataSource.setDriverClassName(szJdbcDriver);
		 dataSource.setUrl(szJdbcURL);
		 dataSource.setUsername(szUser);
		 dataSource.setPassword(szPasswd);

		DynamicConceptModel dynamicConceptModel = new DynamicConceptModel();

		DynamicConceptDao dao = new DynamicConceptDao(dataSource, dynamicConceptModel);

	}

}
