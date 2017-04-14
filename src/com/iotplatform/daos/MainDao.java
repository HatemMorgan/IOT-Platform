package com.iotplatform.daos;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.jena.update.UpdateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.ontology.Class;

import com.iotplatform.queries.InsertionQuery;
import com.iotplatform.queries.SelectionQuery;
import com.iotplatform.query.results.SelectionQueryResults;
import com.iotplatform.utilities.PropertyValue;
import com.iotplatform.utilities.QueryField;
import com.iotplatform.utilities.QueryVariable;

import oracle.spatial.rdf.client.jena.ModelOracleSem;
import oracle.spatial.rdf.client.jena.Oracle;

/*
 * MainDao is used to insert triples to application model
 */

@Repository("mainDao")
public class MainDao {

	private Oracle oracle;

	private SelectionQueryResults selectionUtility;

	@Autowired
	public MainDao(Oracle oracle, SelectionQueryResults selectionUtility) {
		this.oracle = oracle;
		this.selectionUtility = selectionUtility;
	}

	/*
	 * insertData method insert new triples to passed application model
	 */
	public void insertData(String applicationModelName, String requestSubjectClassName,
			Hashtable<Class, ArrayList<ArrayList<PropertyValue>>> htblClassPropertyValue) {
		try {

			String insertQuery = InsertionQuery.constructInsertQuery(applicationModelName, requestSubjectClassName,
					htblClassPropertyValue);

			System.out.println(insertQuery);

			/*
			 * execute query
			 */
			ModelOracleSem model = ModelOracleSem.createOracleSemModel(oracle, applicationModelName);
			UpdateAction.parseExecute(insertQuery, model);
			model.close();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), requestSubjectClassName);
		}

	}

	/*
	 * queryData method is used :
	 * 
	 * 1- create a dynamic select query from passed htblClassNameProperty by
	 * calling SelectionQuery.constructSelectQuery method
	 * 
	 * 2- it calls the SelectionUtility.constructQueryResult to construct the
	 * results in the form of List<Hashtable<String, Object>> to be returned to
	 * the user
	 */
	public List<Hashtable<String, Object>> queryData(
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<QueryField>>> htblClassNameProperty,
			String applicationModelName) {

		Iterator<String> htblClassNamePropertyIterator = htblClassNameProperty.keySet().iterator();

		/*
		 * get first prefixedClassName which is the prefixedName of the passed
		 * request className because LinkedHashMap keep the order of the
		 * insertion unchanged and GetQueryRequestValiation insert the
		 * requestClassPrefixName firstly
		 */
		String prefixedClassName = htblClassNamePropertyIterator.next();
		String mainInstanceUniqueIdentifier = htblClassNameProperty.get(prefixedClassName).keySet().iterator().next();

		Object[] returnObject = SelectionQuery.constructSelectQuery(htblClassNameProperty, prefixedClassName,
				mainInstanceUniqueIdentifier, applicationModelName);

		String queryString = returnObject[0].toString();
		Hashtable<String, QueryVariable> htblSubjectVariables = (Hashtable<String, QueryVariable>) returnObject[1];

		try {

			ResultSet results = oracle.executeQuery(queryString, 0, 1);

			return SelectionQueryResults.constructQueryResult(applicationModelName, results, prefixedClassName,
					htblSubjectVariables);

			// ResultSetMetaData rsmd = results.getMetaData();
			// int columnsNumber = rsmd.getColumnCount();
			// while (results.next()) {
			// for (int i = 1; i <= columnsNumber; i++) {
			// if (i > 1)
			// System.out.print(", ");
			// String columnValue = results.getString(i);
			// System.out.print(columnValue + " " + rsmd.getColumnName(i));
			// }
			// System.out.println("");
			// }
			// return null;

		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage(), prefixedClassName);
		}

	}

	public List<Hashtable<String, Object>> selectAll(String applicationModelName, Class subjectClass) {

		String applicationName = applicationModelName.replaceAll(" ", "").toUpperCase().substring(0,
				applicationModelName.length() - 6);

		String queryString = SelectionQuery.constructSelectAllQueryNoFilters(subjectClass, applicationModelName);
		List<Hashtable<String, Object>> subjectClassIndividualsList = new ArrayList<>();

		try {
			ResultSet results = oracle.executeQuery(queryString, 0, 1);

			/*
			 * call constractResponeJsonObjectForListSelection method in
			 * selectionUtility class to construct the response json
			 */

			subjectClassIndividualsList = selectionUtility.constractResponeJsonObjectForListSelection(applicationName,
					results, subjectClass);

		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage(), subjectClass.getName());
		}

		return subjectClassIndividualsList;
	}

	public void test() {
		try {

			// String queryString = "select name,
			// person,userName,gr,memberGroupName,orgainzation,description from
			// table (sem_match( 'select ?name ?person ?userName ?gr
			// ?memberGroupName ?orgainzation ?description where{ ?g a
			// foaf:Group ; foaf:name ?name ; foaf:member ?m. Optional{ ?m a
			// foaf:Person ; foaf:userName ?userName. BIND (?m AS ?person )}
			// Optional{?m a foaf:Group ; foaf:name ?memberGroupName. BIND (?m
			// AS ?gr )} Optional{?m a foaf:Organization;
			// iot-platform:description ?description. BIND (?m AS ?orgainzation
			// )} }
			// ',sem_models('TESTAPPLICATION_MODEL'),null,SEM_ALIASES(SEM_ALIAS('iot-platform','http://iot-platform#'),SEM_ALIAS('foaf','http://xmlns.com/foaf/0.1/')),null))";
			String queryString = "select name, person,userName,gr,memberGroupName,orgainzation,description,member,memberType from table (sem_match( 'select ?name ?person ?userName ?gr ?memberGroupName ?orgainzation ?description ?member ?memberType  where{ ?g a foaf:Group ; foaf:name ?name ; foaf:member ?m.  Optional{ ?m a foaf:Person ;  foaf:userName ?userName. BIND (?m AS ?person )} Optional{?m a foaf:Group ; foaf:name ?memberGroupName.  BIND (?m AS ?gr )}  optional{?m a ?class  filter(?class != foaf:Person && ?class != iot-platform:Developer && ?class != foaf:Agent && ?class != iot-platform:Admin && ?class != iot-platform:NormalUser && ?class != foaf:Group) BIND (?m AS ?member ) BIND (?class AS ?memberType ) } }  ',sem_models('TESTAPPLICATION_MODEL'),null,SEM_ALIASES(SEM_ALIAS('iot-platform','http://iot-platform#'),SEM_ALIAS('foaf','http://xmlns.com/foaf/0.1/')),null))";
			ResultSet results = oracle.executeQuery(queryString, 0, 1);

			ResultSetMetaData rsmd = results.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			while (results.next()) {
				for (int i = 1; i <= columnsNumber; i++) {
					if (i > 1)
						System.out.print(", ");
					String columnValue = results.getString(i);
					System.out.print(columnValue + " " + rsmd.getColumnName(i));
				}
				System.out.println("");
			}
			// return null;

		} catch (

		SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), "test");
		}
	}

	public static void main(String[] args) {

		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		String szUser = "rdfusr";
		String szPasswd = "rdfusr";

		Oracle oracle = new Oracle(szJdbcURL, szUser, szPasswd);

		MainDao mainDao = new MainDao(oracle, null);

		mainDao.test();
	}
}
