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

			String quString = " SELECT subject6 , objectType0 FROM TABLE( SEM_MATCH ( ' SELECT ?subject6 ?objectType0 WHERE { ?subject0 a <http://iot-platform#CommunicatingDevice> ;  ssn:hasSurvivalRange ?subject4 . ?subject4 a <http://purl.oclc.org/NET/ssnx/ssn#SurvivalRange> ; ssn:hasSurvivalProperty ?subject6 . ?subject6 a ?objectType0 }' , sem_models('TESTAPPLICATION_MODEL'),null, SEM_ALIASES(SEM_ALIAS('ssn','http://purl.oclc.org/NET/ssnx/ssn#'),SEM_ALIAS('geo','http://www.w3.org/2003/01/geo/wgs84_pos#'),SEM_ALIAS('iot-lite','http://purl.oclc.org/NET/UNIS/fiware/iot-lite#'),SEM_ALIAS('iot-platform','http://iot-platform#'),SEM_ALIAS('foaf','http://xmlns.com/foaf/0.1/'),SEM_ALIAS('xsd','http://www.w3.org/2001/XMLSchema#'),SEM_ALIAS('owl','http://www.w3.org/2002/07/owl#'),SEM_ALIAS('rdfs','http://www.w3.org/2000/01/rdf-schema#'),SEM_ALIAS('rdf','http://www.w3.org/1999/02/22-rdf-syntax-ns#'),SEM_ALIAS('qu','http://purl.org/NET/ssnx/qu/qu#'),SEM_ALIAS('DUL','http://www.loa-cnr.it/ontologies/DUL.owl#')),null))";

			String queryString = " SELECT subject0 , var0 , var1 , var2 , subject1 , var3 , subject2 , var4 ,"
					+ " var5 , var6 , subject3 , var7 , var8 , var9 , subject4 , subject5 , var10 , var11 , "
					+ "subject7 , var12 , subject8 , var13 ,object0 , objectType0 FROM TABLE( SEM_MATCH ( "
					+ "' SELECT ?subject0 ?var0 ?var1 ?var2 ?subject1 ?var3 ?subject2 ?var4 ?var5 ?var6 "
					+ "?subject3 ?var7 ?var8 ?var9 ?subject4 ?subject5 ?var10 ?var11 ?subject7 ?var12 "
					+ "?subject8 ?var13 ?object0 ?objectType0 WHERE { ?subject0 a "
					+ "<http://iot-platform#CommunicatingDevice> ; iot-lite:id ?var0 ;"
					+ " iot-platform:hasTransmissionPower ?var1 ; iot-platform:hasType ?var2 ; "
					+ "iot-lite:hasCoverage ?subject1  ; ssn:hasSurvivalRange"
					+ " ?subject4 . ?subject2 a <http://www.w3.org/2003/01/geo/wgs84_pos#Point> ;"
					+ " iot-lite:id ?var4 ; geo:long ?var5 ; geo:lat ?var6 ."
					+ " ?subject1 a <http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Coverage>;"
					+ " iot-lite:id ?var3 ; geo:location ?subject2 . ?subject5 a "
					+ "<http://purl.oclc.org/NET/ssnx/ssn#Condition> ; iot-lite:id ?var10 ; "
					+ "iot-platform:description ?var11 .  ?subject4 a"
					+ " <http://purl.oclc.org/NET/ssnx/ssn#SurvivalRange> ; ssn:inCondition ?subject5 ;"
					+ " ssn:hasSurvivalProperty ?subject6 . OPTIONAL { ?subject6 a "
					+ "<http://purl.oclc.org/NET/ssnx/ssn#SystemLifetime> ; iot-lite:id ?var12 ; "
					+ "ssn:hasValue ?subject8 . ?subject8 a <http://iot-platform#Amount> ;"
					+ " iot-platform:hasDataValue ?var13 . BIND( ?subject6 AS ?subject7 )} OPTIONAL { ?subject6 a ?class0 "
					+ "FILTER ( ?class0 != <http://purl.oclc.org/NET/ssnx/ssn#SurvivalProperty> && ?class0 != "
					+ "<http://purl.oclc.org/NET/ssnx/ssn#SystemLifetime> && ?class0 != <http://purl.oclc.org/NET/ssnx/ssn#Property>"
					+ ") BIND ( ?subject6 AS ?object0 ) "
					+ "BIND ( ?class0 AS ?objectType0 ) } FILTER ( BOUND ( ?subject7 ) || BOUND ( ?object0 ) ) }'"
					+ " , sem_models('TESTAPPLICATION_MODEL'),null, SEM_ALIASES(SEM_ALIAS('ssn','http://purl.oclc.org/NET/ssnx/ssn#'),SEM_ALIAS('geo','http://www.w3.org/2003/01/geo/wgs84_pos#'),SEM_ALIAS('iot-lite','http://purl.oclc.org/NET/UNIS/fiware/iot-lite#'),SEM_ALIAS('iot-platform','http://iot-platform#'),SEM_ALIAS('foaf','http://xmlns.com/foaf/0.1/'),SEM_ALIAS('xsd','http://www.w3.org/2001/XMLSchema#'),SEM_ALIAS('owl','http://www.w3.org/2002/07/owl#'),SEM_ALIAS('rdfs','http://www.w3.org/2000/01/rdf-schema#'),SEM_ALIAS('rdf','http://www.w3.org/1999/02/22-rdf-syntax-ns#'),SEM_ALIAS('qu','http://purl.org/NET/ssnx/qu/qu#'),SEM_ALIAS('DUL','http://www.loa-cnr.it/ontologies/DUL.owl#')),null))";

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
