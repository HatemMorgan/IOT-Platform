package com.iotplatform.daos;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.exceptions.InvalidRequestFieldsException;
import com.iotplatform.models.DynamicConceptModel;
import com.iotplatform.ontology.DynamicConceptColumns;
import com.iotplatform.ontology.PropertyType;
import com.iotplatform.utilities.SqlCondition;

@Repository("dynamicConceptsDao")
public class DynamicConceptDao extends JdbcDaoSupport {

	private JdbcTemplate jdbcTemplate;

	@Autowired
	public DynamicConceptDao(DataSource dataSource) {
		this.setDataSource(dataSource);
		this.jdbcTemplate = this.getJdbcTemplate();
		System.out.println("Connected");
		if (!wasTableCreated()) {
			CreateTable();
			System.out.println("DYNAMIC_CONCEPTS table created successfully");
		} else {
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

		String queryString = "CREATE TABLE DYNAMIC_CONCEPTS (" + "application_name VARCHAR(200) ,"
				+ "class_name VARCHAR(50), class_uri VARCHAR(150), class_prefix_uri VARCHAR(150),"
				+ "class_prefix_alias VARCHAR(20), property_name VARCHAR(50), property_uri VARCHAR(150),"
				+ "property_prefix_uri VARCHAR(150), property_prefix_alias VARCHAR(50),"
				+ "property_type VARCHAR(20), property_object_type_uri VARCHAR(150),isUnique NUMBER(1,0) ,"
				+ " hasMultipleValues NUMBER(1,0),"
				+ "CONSTRAINT dynamic_concept_table_uk PRIMARY KEY(application_name,class_uri,property_uri) )";
		try {
			jdbcTemplate.execute(queryString);
		} catch (DataAccessException e) {
			throw new DatabaseException(e.getMessage(), "Ontology");
		}

	}

	/*
	 * DynamicConceptModel method insert a new ontology concept for a specific
	 * application domain
	 * 
	 */
	public int insertNewConcept(DynamicConceptModel newDynamicConcept) {

		String queryString = "INSERT INTO DYNAMIC_CONCEPTS VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
		String modelConventionName = newDynamicConcept.getApplication_name().replaceAll(" ", "").toUpperCase();
		try {
			int count = jdbcTemplate.update(queryString,
					new Object[] { modelConventionName, newDynamicConcept.getClass_name(),
							newDynamicConcept.getClass_uri(), newDynamicConcept.getClass_prefix_uri(),
							newDynamicConcept.getClass_prefix_alias(), newDynamicConcept.getProperty_name(),
							newDynamicConcept.getProperty_uri(), newDynamicConcept.getProperty_prefix_uri(),
							newDynamicConcept.getProperty_prefix_alias(), newDynamicConcept.getProperty_type(),
							newDynamicConcept.getProperty_object_type_uri(), newDynamicConcept.getIsUnique(),
							newDynamicConcept.getHasMultipleValues() });
			return count;
		} catch (DuplicateKeyException duplicateKeyException) {
			throw new DatabaseException(
					"New Class or Property need to be added are already exist. Please check your application domain ontology",
					"Ontology");
		} catch (DataAccessException e) {
			throw new DatabaseException(e.getMessage(), "Ontology");

		}
	}

	/*
	 * getConceptsOfApplication methods gets all the dynamic added concepts
	 */
	public List<DynamicConceptModel> getConceptsOfApplication(String applicationName) {
		List<DynamicConceptModel> concepts = null;
		String modelConventionName = applicationName.replaceAll(" ", "").toUpperCase();
		try {
			concepts = jdbcTemplate.query("SELECT * FROM DYNAMIC_CONCEPTS WHERE application_name = ?",
					new Object[] { modelConventionName },
					new BeanPropertyRowMapper<DynamicConceptModel>(DynamicConceptModel.class));

		} catch (DataAccessException e) {
			throw new DatabaseException(e.getMessage(), "Ontology");

		}
		return concepts;
	}

	/*
	 * getConceptsOfApplicationByFilters method used to create a dynamic where
	 * clause select query .
	 */

	public List<DynamicConceptModel> getConceptsOfApplicationByFilters(String applicationName,
			ArrayList<SqlCondition> andOpColNameValueList, ArrayList<SqlCondition> orOpColNameValueList) {

		List<DynamicConceptModel> concepts = null;
		int andOpColNameValueListSize = 0;
		int orOpColNameValueListSize = 0;

		if (andOpColNameValueList != null) {
			andOpColNameValueListSize = andOpColNameValueList.size();
		}

		if (orOpColNameValueList != null) {
			orOpColNameValueListSize = orOpColNameValueList.size();
		}

		/*
		 * Create dynamic filer query
		 */
		String modelConventionName = applicationName.replaceAll(" ", "").toUpperCase();
		Object[] params = new Object[andOpColNameValueListSize + 1 + orOpColNameValueListSize];

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("SELECT * FROM DYNAMIC_CONCEPTS WHERE ");
		int count = 0;

		/*
		 * Add conditions seperated by AND operator
		 */

		if (andOpColNameValueListSize > 0) {

			for (SqlCondition andCondtion : andOpColNameValueList) {
				String colName = andCondtion.getColName();
				String value = andCondtion.getColValue();
				stringBuilder.append(colName + " = ? AND ");
				params[count] = value;
				count++;
			}

		}
		/*
		 * add application name condition to make sure that we are only querying
		 * dynamic concepts of the specified application domain
		 */
		stringBuilder.append("application_name = ? ");
		params[count] = modelConventionName;
		count++;
		/*
		 * Add conditions seperated by Or operator
		 */
		if (orOpColNameValueListSize > 0) {

			stringBuilder.append("AND ( ");

			int c = 0;

			for (SqlCondition orCondition : orOpColNameValueList) {
				String colName = orCondition.getColName();
				String value = orCondition.getColValue();

				if (c < orOpColNameValueListSize - 1) {
					stringBuilder.append(colName + " = ? OR ");

				} else {
					stringBuilder.append(colName + " = ?  ");

				}
				c++;
				params[count] = value;
				count++;
			}

			stringBuilder.append(" )");
		}

		System.out.println(stringBuilder.toString());

		try {
			concepts = jdbcTemplate.query(stringBuilder.toString(), params,
					new BeanPropertyRowMapper<DynamicConceptModel>(DynamicConceptModel.class));
			return concepts;
		} catch (BadSqlGrammarException ex) {
			ex.printStackTrace();
			throw new InvalidRequestFieldsException("Ontology");
		}

		catch (DataAccessException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), "Ontology");

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

		DynamicConceptDao dao = new DynamicConceptDao(dataSource);

		// DynamicConceptModel newConcept = new
		// DynamicConceptModel("TestApplication", "Person",
		// "http://xmlns.com/foaf/0.1/Person", "http://xmlns.com/foaf/0.1/",
		// "foaf:",
		// "hates", "http://xmlns.com/foaf/0.1/hates",
		// "http://xmlns.com/foaf/0.1/", "foaf:",
		// PropertyType.ObjectProperty.toString(), "Person",0,1);

		// DynamicConceptModel newConcept = new
		// DynamicConceptModel("TestApplication", "Developer",
		// "http://iot-platform#Developer", "http://iot-platform#",
		// "iot-platform:",
		// "love", "http://iot-platform#love",
		// "http://iot-platform#", "iot-platform:",
		// PropertyType.ObjectProperty.toString(), "Person",0,1);
		//
		
		
		DynamicConceptModel newConcept = new DynamicConceptModel("TestApplication", "System",
				"http://purl.oclc.org/NET/ssnx/ssn#System", "http://purl.oclc.org/NET/ssnx/ssn#", "ssn:", "test",
				"http://iot-platform#test", "http://iot-platform#", "iot-platform:",
				PropertyType.ObjectProperty.toString(), "http://purl.oclc.org/NET/ssnx/ssn#System",0,0);
		System.out.println(dao.insertNewConcept(newConcept));
		 System.out.println(dao.getConceptsOfApplication("testapplication").toString());
		
//
//		ArrayList<SqlCondition> orConditionList = new ArrayList<>();
//
//		orConditionList.add(new SqlCondition(DynamicConceptColumns.CLASS_NAME.toString(), "Person"));
//		orConditionList.add(new SqlCondition(DynamicConceptColumns.CLASS_NAME.toString(), "Developer"));
//
//		System.out.println(dao.getConceptsOfApplicationByFilters("TESTAPPLICATION", null, orConditionList).toString());
	}

}
