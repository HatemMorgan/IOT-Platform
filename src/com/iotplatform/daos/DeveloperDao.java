package com.iotplatform.daos;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.jena.update.UpdateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;
import com.iotplatform.ontology.classes.Developer;
import com.iotplatform.utilities.QueryUtility;

import oracle.spatial.rdf.client.jena.ModelOracleSem;
import oracle.spatial.rdf.client.jena.Oracle;

@Repository("developerDao")
public class DeveloperDao {

	private Oracle oracle;
	private final String suffix = "_MODEL";
	private Developer developerClass;

	@Autowired
	public DeveloperDao(Oracle oracle, Developer developerClass) {
		System.out.println("ApplicationDAO Created");
		this.oracle = oracle;
		this.developerClass = developerClass;
	}

	public void InsertDeveloper(Hashtable<String, Object> htblPropValue, String applicationName,
			ArrayList<Prefixes> prefixes) {

		String userName = htblPropValue.get("foaf:userName").toString()
				.replace(XSDDataTypes.string_typed.getXsdType(), "").replaceAll("\"", "");
		String insertQuery = QueryUtility.constructInsertQuery(prefixes, Prefixes.FOAF.getPrefix() + userName,
				developerClass, htblPropValue);

		String applicationModelName = applicationName.replaceAll(" ", "").toUpperCase() + suffix;

		try {

			ModelOracleSem model = ModelOracleSem.createOracleSemModel(oracle, applicationModelName);
			System.out.println(insertQuery);
			UpdateAction.parseExecute(insertQuery, model);
			model.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
		htblPropValue.put("iot-platform:developedApplication", "iot-platform:TESTAPPLICATION");

		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		String szUser = "rdfusr";
		String szPasswd = "rdfusr";

		Oracle oracle = new Oracle(szJdbcURL, szUser, szPasswd);

		DeveloperDao developerDao = new DeveloperDao(oracle, new Developer());
		developerDao.InsertDeveloper(htblPropValue, "test application", prefixesList);

	}

}
