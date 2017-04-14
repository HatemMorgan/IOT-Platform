package com.iotplatform.daos;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.iotplatform.exceptions.DatabaseException;

import oracle.spatial.rdf.client.jena.Oracle;

/*
 * MainDao is used to insert triples to application model
 */

public class MainDao {

	private Oracle oracle;

	public MainDao(Oracle oracle) {
		this.oracle = oracle;
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

		MainDao mainDao = new MainDao(oracle);

		mainDao.test();
	}
}
