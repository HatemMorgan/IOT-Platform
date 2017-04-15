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
			// String queryString = "select name,
			// person,userName,gr,memberGroupName,orgainzation,description,member,memberType
			// from table (sem_match( 'select ?name ?person ?userName ?gr
			// ?memberGroupName ?orgainzation ?description ?member ?memberType
			// where{ ?g a foaf:Group ; foaf:name ?name ; foaf:member ?m.
			// Optional{ ?m a foaf:Person ; foaf:userName ?userName. BIND (?m AS
			// ?person )} Optional{?m a foaf:Group ; foaf:name ?memberGroupName.
			// BIND (?m AS ?gr )} optional{?m a ?class filter(?class !=
			// foaf:Person && ?class != iot-platform:Developer && ?class !=
			// foaf:Agent && ?class != iot-platform:Admin && ?class !=
			// iot-platform:NormalUser && ?class != foaf:Group) BIND (?m AS
			// ?member ) BIND (?class AS ?memberType ) } }
			// ',sem_models('TESTAPPLICATION_MODEL'),null,SEM_ALIASES(SEM_ALIAS('iot-platform','http://iot-platform#'),SEM_ALIAS('foaf','http://xmlns.com/foaf/0.1/')),null))";
			// String queryString = "SELECT subject0 , var0 , subject2 , var1 ,
			// var2 , subject3 , var3 , var4 , subject4 , var5 , var6 , var7,
			// member,memberType FROM TABLE( SEM_MATCH ( ' SELECT ?subject0
			// ?var0 ?subject2 ?var1 ?var2 ?subject3 ?var3 ?var4 ?subject4 ?var5
			// ?var6 ?var7 ?member ?memberType WHERE { ?subject0 a
			// <http://xmlns.com/foaf/0.1/Group> ; foaf:name ?var0 ; foaf:member
			// ?subject1 ; iot-platform:description ?var7 . OPTIONAL { ?subject1
			// a <http://xmlns.com/foaf/0.1/Person> ; foaf:userName ?var1 ;
			// foaf:age ?var2 ;foaf:knows ?subject3 . ?subject3 a
			// <http://xmlns.com/foaf/0.1/Person> ; foaf:age ?var3 ;
			// foaf:userName ?var4 . BIND( ?subject1 AS ?subject2 ) } OPTIONAL {
			// ?subject1 a <http://xmlns.com/foaf/0.1/Organization> ; foaf:name
			// ?var5 ; iot-platform:description ?var6 . BIND( ?subject1 AS
			// ?subject4 ) } optional{?subject1 a ?class filter(?class !=
			// foaf:Person && ?class != iot-platform:Developer && ?class !=
			// foaf:Agent && ?class != iot-platform:Admin && ?class !=
			// iot-platform:NormalUser && ?class != foaf:Organization) BIND
			// (?subject1 AS ?member ) BIND (?class AS ?memberType ) } }' ,
			// sem_models('TESTAPPLICATION_MODEL'),null,
			// SEM_ALIASES(SEM_ALIAS('ssn','http://purl.oclc.org/NET/ssnx/ssn#'),SEM_ALIAS('geo','http://www.w3.org/2003/01/geo/wgs84_pos#'),SEM_ALIAS('iot-lite','http://purl.oclc.org/NET/UNIS/fiware/iot-lite#'),SEM_ALIAS('iot-platform','http://iot-platform#'),SEM_ALIAS('foaf','http://xmlns.com/foaf/0.1/'),SEM_ALIAS('xsd','http://www.w3.org/2001/XMLSchema#'),SEM_ALIAS('owl','http://www.w3.org/2002/07/owl#'),SEM_ALIAS('rdfs','http://www.w3.org/2000/01/rdf-schema#'),SEM_ALIAS('rdf','http://www.w3.org/1999/02/22-rdf-syntax-ns#'),SEM_ALIAS('qu','http://purl.org/NET/ssnx/qu/qu#'),SEM_ALIAS('DUL','http://www.loa-cnr.it/ontologies/DUL.owl#')),null))";

			String queryString = "SELECT  subject0 , var0 , subject2 , var1 , var2 , subject3 , var3 , var4 , subject4 , var5 , var6 , var7, member,memberType FROM TABLE( SEM_MATCH ( ' SELECT  ?subject0  ?var0 ?subject2  ?var1  ?var2 ?subject3  ?var3  ?var4 ?subject4  ?var5  ?var6  ?var7 ?member ?memberType WHERE { ?subject0   a <http://xmlns.com/foaf/0.1/Group> ; foaf:name ?var0 ; foaf:member ?subject1 ;  iot-platform:description ?var7 .  OPTIONAL { ?subject1 a <http://xmlns.com/foaf/0.1/Person>  ; foaf:userName ?var1 ; foaf:age ?var2 ;foaf:knows ?subject3 . ?subject3 a <http://xmlns.com/foaf/0.1/Person> ; foaf:age ?var3 ; foaf:userName ?var4 . BIND( ?subject1 AS ?subject2 )  } OPTIONAL { ?subject1 a <http://xmlns.com/foaf/0.1/Organization> ; foaf:name ?var5 ; iot-platform:description ?var6 . BIND( ?subject1 AS ?subject4 )  } optional{?subject1 a ?class  filter(?class != foaf:Person && ?class != iot-platform:Developer && ?class != foaf:Agent && ?class != iot-platform:Admin && ?class != iot-platform:NormalUser && ?class != foaf:Organization) BIND (?subject1 AS ?member ) BIND (?class AS ?memberType ) } filter(bound(?subject2) || bound(?subject4) || bound(?member) )  }' ,  sem_models('TESTAPPLICATION_MODEL'),null, SEM_ALIASES(SEM_ALIAS('ssn','http://purl.oclc.org/NET/ssnx/ssn#'),SEM_ALIAS('geo','http://www.w3.org/2003/01/geo/wgs84_pos#'),SEM_ALIAS('iot-lite','http://purl.oclc.org/NET/UNIS/fiware/iot-lite#'),SEM_ALIAS('iot-platform','http://iot-platform#'),SEM_ALIAS('foaf','http://xmlns.com/foaf/0.1/'),SEM_ALIAS('xsd','http://www.w3.org/2001/XMLSchema#'),SEM_ALIAS('owl','http://www.w3.org/2002/07/owl#'),SEM_ALIAS('rdfs','http://www.w3.org/2000/01/rdf-schema#'),SEM_ALIAS('rdf','http://www.w3.org/1999/02/22-rdf-syntax-ns#'),SEM_ALIAS('qu','http://purl.org/NET/ssnx/qu/qu#'),SEM_ALIAS('DUL','http://www.loa-cnr.it/ontologies/DUL.owl#')),null))";
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
