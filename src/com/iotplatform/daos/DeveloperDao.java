package com.iotplatform.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.jena.update.UpdateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;
import com.iotplatform.ontology.classes.Developer;
import com.iotplatform.utilities.QueryUtility;

import oracle.spatial.rdf.client.jena.ModelOracleSem;
import oracle.spatial.rdf.client.jena.Oracle;

@Repository("developerDao")
public class DeveloperDao {

	private Oracle oracle;
	private Developer developerClass;

	@Autowired
	public DeveloperDao(Oracle oracle, Developer developerClass) {
		System.out.println("ApplicationDAO Created");
		this.oracle = oracle;
		this.developerClass = developerClass;
	}

	public void InsertDeveloper(Hashtable<String, Object> htblPropValue, String applicationModelName) {

		String userName = htblPropValue.get("foaf:userName").toString()
				.replace(XSDDataTypes.string_typed.getXsdType(), "").replaceAll("\"", "");
		String insertQuery = QueryUtility.constructInsertQuery(Prefixes.FOAF.getPrefix() + userName,
				developerClass, htblPropValue);


		try {

			ModelOracleSem model = ModelOracleSem.createOracleSemModel(oracle, applicationModelName);
			System.out.println(insertQuery);
			UpdateAction.parseExecute(insertQuery, model);
			model.close();

		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage(), "Developer");
		}

	}

	public List<Hashtable<String, Object>> getDevelopers(String applicationModelName) {


		String queryString = QueryUtility.constructSelectAllQueryNoFilters(developerClass, applicationModelName);

		System.out.println(queryString);
		Hashtable<String, Object> htblDeveloperPropVal = null;
		List<Hashtable<String, Object>> developersList = new ArrayList<>();
		long startTime = System.currentTimeMillis();

		try {
			ResultSet res = oracle.executeQuery(queryString, 0, 1);
			Object currentSubject = null;
			while (res.next()) {

				Object subject = res.getObject(1);
				if (htblDeveloperPropVal == null) {
					currentSubject = subject;
					htblDeveloperPropVal = new Hashtable<>();
				}

				/*
				 * as long as the current subject equal to subject got from the
				 * results then add the property and value to the developer's
				 * hashtable . If they are not the same this means that this is
				 * a new developer so we have to construct a new hashtable to
				 * hold it data
				 */

				if (currentSubject.equals(subject)) {
					htblDeveloperPropVal.put(res.getString(2), res.getObject(3));
				} else {
					developersList.add(htblDeveloperPropVal);
					htblDeveloperPropVal = new Hashtable<>();
					htblDeveloperPropVal.put(res.getString(2), res.getObject(3));
				}

			}
			
			/*
			 * Add the last htblDeveloperPropVal to the list because it will not
			 * enter the else part as the loop will terminate
			 */
			
			developersList.add(htblDeveloperPropVal);

			System.out.println(
					"test selecting: elapsed time (sec): " + ((System.currentTimeMillis() - startTime) / 1000.0));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return developersList;
	}

	public static void main(String[] args) {
		ArrayList<Prefixes> prefixesList = new ArrayList<>();
		prefixesList.add(Prefixes.FOAF);
		prefixesList.add(Prefixes.IOT_LITE);
		prefixesList.add(Prefixes.IOT_PLATFORM);
		prefixesList.add(Prefixes.XSD);

		Hashtable<String, Object> htblPropValue = new Hashtable<>();
		htblPropValue.put("foaf:age", "\"20\"" + XSDDataTypes.integer_typed.getXsdType());
		htblPropValue.put("foaf:firstName", "\"Hatem\"" + XSDDataTypes.string_typed.getXsdType());
		htblPropValue.put("foaf:lastName", "\"Elsayed\"" + XSDDataTypes.string_typed.getXsdType());
		htblPropValue.put("foaf:familyName", "\"Morgan\"" + XSDDataTypes.string_typed.getXsdType());
		htblPropValue.put("foaf:birthday", "\"27/7/1995\"" + XSDDataTypes.string_typed.getXsdType());
		htblPropValue.put("foaf:gender", "\"Male\"" + XSDDataTypes.string_typed.getXsdType());
		htblPropValue.put("iot-lite:id", "\"1\"" + XSDDataTypes.string_typed.getXsdType());
		htblPropValue.put("foaf:title", "\"Engineer\"" + XSDDataTypes.string_typed.getXsdType());
		htblPropValue.put("foaf:userName", "\"HatemMorgan\"" + XSDDataTypes.string_typed.getXsdType());
		htblPropValue.put("foaf:mbox", "\"hatemmorgan17@gmail.com\"" + XSDDataTypes.string_typed.getXsdType());
		htblPropValue.put("iot-platform:developedApplication", "iot-platform:TESTAPPLICATION");

		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		String szUser = "rdfusr";
		String szPasswd = "rdfusr";

		Oracle oracle = new Oracle(szJdbcURL, szUser, szPasswd);

		DeveloperDao developerDao = new DeveloperDao(oracle, new Developer());
		// developerDao.InsertDeveloper(htblPropValue, "test application",
		// prefixesList);
		System.out.println(developerDao.getDevelopers("Test Application"));
	}

}
