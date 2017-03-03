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
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;
import com.iotplatform.ontology.classes.Developer;
import com.iotplatform.utilities.PropertyValue;
import com.iotplatform.utilities.SelectionUtility;
import com.iotplatform.utilities.QueryUtility;

import oracle.spatial.rdf.client.jena.ModelOracleSem;
import oracle.spatial.rdf.client.jena.Oracle;

@Repository("developerDao")
public class DeveloperDao {

	private Oracle oracle;
	private Developer developerClass;
	private SelectionUtility selectionUtility;

	@Autowired
	public DeveloperDao(Oracle oracle, Developer developerClass, SelectionUtility selectionUtility) {
		this.oracle = oracle;
		this.developerClass = developerClass;
		this.selectionUtility = selectionUtility;
	}

	/*
	 * insertDeveloper method inserts a new Developer to the passed application
	 * model
	 */
	public void insertDeveloper(ArrayList<PropertyValue> prefixedPropertyValue, String applicationModelName,
			String userName) {

		userName = userName.replace(XSDDataTypes.string_typed.getXsdType(), "").replaceAll("\"", "");

		/*
		 * get all superClasses of developer class to identify that the new
		 * instance is also an instance of all super classes of developerClass
		 */

		for (Class superClass : developerClass.getSuperClassesList()) {
			prefixedPropertyValue
					.add(new PropertyValue("a", superClass.getPrefix().getPrefix() + superClass.getName()));
		}

		String insertQuery = QueryUtility.constructInsertQuery(
				Prefixes.IOT_PLATFORM.getPrefix() + userName.toLowerCase(), developerClass, prefixedPropertyValue);

		try {

			ModelOracleSem model = ModelOracleSem.createOracleSemModel(oracle, applicationModelName);
			UpdateAction.parseExecute(insertQuery, model);
			model.close();

		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage(), "Developer");
		}

	}

	/*
	 * getDevelopers method returns all the developers in the passed application
	 * model
	 */

	public List<Hashtable<String, Object>> getDevelopers(String applicationModelName) {

		String applicationName = applicationModelName.replaceAll(" ", "").toUpperCase().substring(0,
				applicationModelName.length() - 6);

		String queryString = QueryUtility.constructSelectAllQueryNoFilters(developerClass, applicationModelName);
		System.out.println(queryString);
		List<Hashtable<String, Object>> developersList = new ArrayList<>();

		try {
			ResultSet results = oracle.executeQuery(queryString, 0, 1);

			/*
			 * call constractResponeJsonObjectForListSelection method in
			 * selectionUtility class to construct the response json
			 */

			developersList = selectionUtility.constractResponeJsonObjectForListSelection(applicationName, results,
					developerClass);

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), "Developer");
		}

		return developersList;
	}

	// public static void main(String[] args) {
	//
	// Hashtable<String, Object> htblPropValue = new Hashtable<>();
	// htblPropValue.put("foaf:age", "\"20\"" +
	// XSDDataTypes.integer_typed.getXsdType());
	// htblPropValue.put("foaf:firstName", "\"Hatem\"" +
	// XSDDataTypes.string_typed.getXsdType());
	// htblPropValue.put("foaf:middleName", "\"Elsayed\"" +
	// XSDDataTypes.string_typed.getXsdType());
	// htblPropValue.put("foaf:familyName", "\"Morgan\"" +
	// XSDDataTypes.string_typed.getXsdType());
	// htblPropValue.put("foaf:birthday", "\"27/7/1995\"" +
	// XSDDataTypes.string_typed.getXsdType());
	// htblPropValue.put("foaf:gender", "\"Male\"" +
	// XSDDataTypes.string_typed.getXsdType());
	// htblPropValue.put("iot-lite:id", "\"1\"" +
	// XSDDataTypes.string_typed.getXsdType());
	// htblPropValue.put("foaf:title", "\"Engineer\"" +
	// XSDDataTypes.string_typed.getXsdType());
	// htblPropValue.put("foaf:userName", "\"HatemMorgan\"" +
	// XSDDataTypes.string_typed.getXsdType());
	// htblPropValue.put("foaf:mbox", "\"hatemmorgan17@gmail.com\"" +
	// XSDDataTypes.string_typed.getXsdType());
	// htblPropValue.put("iot-platform:developedApplication",
	// "iot-platform:TESTAPPLICATION");
	//
	// String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
	// String szUser = "rdfusr";
	// String szPasswd = "rdfusr";
	//
	// Oracle oracle = new Oracle(szJdbcURL, szUser, szPasswd);
	//
	//// DeveloperDao developerDao = new DeveloperDao(oracle, new
	// Developer(),new QueryResultUtility());
	// // developerDao.InsertDeveloper(htblPropValue, "TESTAPPLICATION_MODEL");
	//// System.out.println(developerDao.getDevelopers("TESTAPPLICATION_MODEL"));
	// }

}
