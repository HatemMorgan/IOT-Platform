package com.iotplatform.daos;

import java.util.Hashtable;
import java.util.Iterator;
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
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.PropertyType;

@Repository("dynamicConceptsDao")
public class DynamicConceptDao extends JdbcDaoSupport {

	private JdbcTemplate jdbcTemplate;
	private final String suffix = "_MODEL";

	@Autowired
	public DynamicConceptDao(DataSource dataSource) {
		this.setDataSource(dataSource);
		this.jdbcTemplate = this.getJdbcTemplate();

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

		String queryString = "CREATE TABLE DYNAMIC_CONCEPTS (" + "application_model VARCHAR(200) ,"
				+ "class_name VARCHAR(50), class_uri VARCHAR(150), class_prefix_uri VARCHAR(150),"
				+ "class_prefix_alias VARCHAR(20), property_name VARCHAR(50), property_uri VARCHAR(150),"
				+ "property_prefix_uri VARCHAR(150), property_prefix_alias VARCHAR(50),"
				+ "property_type VARCHAR(20), property_object_type VARCHAR(20),"
				+ "CONSTRAINT dynamic_concept_table_uk PRIMARY KEY(application_model,class_uri,property_uri) )";
		try {
			jdbcTemplate.execute(queryString);
		} catch (DataAccessException e) {
			System.out.println(e.getMessage());
			System.out.println(e.getClass());
			System.out.println(e.getStackTrace());
		}

	}

	/*
	 * DynamicConceptModel method insert a new ontology concept for a specific
	 * application domain
	 * 
	 */
	public int insertNewConcept(DynamicConceptModel newDynamicConcept) {
		String applicationModelName = newDynamicConcept.getApplication_model().replaceAll(" ", "").toUpperCase()
				+ suffix;
		String queryString = "INSERT INTO DYNAMIC_CONCEPTS VALUES(?,?,?,?,?,?,?,?,?,?,?)";

		try {
			int count = jdbcTemplate.update(queryString,
					new Object[] { applicationModelName, newDynamicConcept.getClass_name(),
							newDynamicConcept.getClass_uri(), newDynamicConcept.getClass_prefix_uri(),
							newDynamicConcept.getClass_prefix_alias(), newDynamicConcept.getProperty_name(),
							newDynamicConcept.getProperty_uri(), newDynamicConcept.getProperty_prefix_uri(),
							newDynamicConcept.getProperty_prefix_alias(), newDynamicConcept.getProperty_type(),
							newDynamicConcept.getProperty_object_type() });
			return count;
		} catch (DuplicateKeyException duplicateKeyException) {
			System.out.println(duplicateKeyException.getMessage());
			System.out.println(duplicateKeyException.getClass());
			System.out.println(duplicateKeyException.getStackTrace());
			return 0;
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			System.out.println(ex.getClass());
			System.out.println(ex.getStackTrace());
			return 0;
		}

	}

	/*
	 * getConceptsOfApplication methods gets all the dynamic added concepts
	 */
	public List<DynamicConceptModel> getConceptsOfApplication(String applicationName) {
		List<DynamicConceptModel> concepts = null;
		String applicationModelName = applicationName.replaceAll(" ", "").toUpperCase() + suffix;
		try {
			concepts = jdbcTemplate.query("SELECT * FROM DYNAMIC_CONCEPTS WHERE application_model = ?",
					new Object[] { applicationModelName },
					new BeanPropertyRowMapper<DynamicConceptModel>(DynamicConceptModel.class));

		} catch (DataAccessException e) {
			System.out.println(e.getMessage());
			System.out.println(e.getClass());
			System.out.println(e.getStackTrace());
		}

		return concepts;
	}

	/*
	 * getConceptsOfApplicationByFilters method used to create a dynamic where
	 * clause select query .
	 */

	public List<DynamicConceptModel> getConceptsOfApplicationByFilters(String applicationName,
			Hashtable<String, String> htblColNameValue) {
		List<DynamicConceptModel> concepts = null;

		String applicationModelName = applicationName.replaceAll(" ", "").toUpperCase() + suffix;

		/*
		 * Create dynamic filer query
		 */

		Object[] params = new Object[htblColNameValue.size() + 1];
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("SELECT * FROM DYNAMIC_CONCEPTS WHERE ");
		int count = 0;
		Iterator<String> iterator = htblColNameValue.keySet().iterator();

		while (iterator.hasNext()) {
			String colName = iterator.next();
			String value = htblColNameValue.get(colName);
			stringBuilder.append(colName + " = ? AND ");
			params[count] = value;
			count++;
		}
		stringBuilder.append("application_model = ? ");
		params[count] = applicationModelName;

		try {
			concepts = jdbcTemplate.query(stringBuilder.toString(), params,
					new BeanPropertyRowMapper<DynamicConceptModel>(DynamicConceptModel.class));
			return concepts;
		} catch (BadSqlGrammarException ex) {
			throw new InvalidRequestFieldsException("Ontology");
		}

		catch (DataAccessException e) {
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
		System.out.println("connected");

		// DynamicConceptModel newConcept = new DynamicConceptModel("Test
		// Application", "Person",
		// "http://xmlns.com/foaf/0.1/Person", "http://xmlns.com/foaf/0.1/",
		// "foaf:",
		// "hates", "http://xmlns.com/foaf/0.1/Person/hates",
		// "http://xmlns.com/foaf/0.1/", "foaf:",
		// PropertyType.ObjectProperty.toString(), "Person");
		//
		// System.out.println(dao.insertNewConcept(newConcept));
		// System.out.println(dao.getConceptsOfApplication("test
		// application").toString());
		Hashtable<String, String> h = new Hashtable<>();
		h.put("class_name", "Person");
		h.put("property_names", "hates");

		System.out.println(dao.getConceptsOfApplicationByFilters("test application", h).toString());

	}

}
