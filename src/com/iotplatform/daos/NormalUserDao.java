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
import com.iotplatform.ontology.classes.NormalUser;
import com.iotplatform.utilities.PropertyValue;
import com.iotplatform.utilities.SelectionUtility;
import com.iotplatform.utilities.QueryUtility;

import oracle.spatial.rdf.client.jena.ModelOracleSem;
import oracle.spatial.rdf.client.jena.Oracle;

@Repository("normalUserDao")
public class NormalUserDao {

	private Oracle oracle;
	private SelectionUtility selectionUtility;
	private NormalUser normalUserClass;

	@Autowired
	public NormalUserDao(Oracle oracle, SelectionUtility selectionUtility, NormalUser normalUserClass) {
		this.oracle = oracle;
		this.selectionUtility = selectionUtility;
		this.normalUserClass = normalUserClass;
	}

	/*
	 * insertNormalUser method inserts a new normal user to the passed
	 * application model
	 */
	public void insertNormalUser(ArrayList<PropertyValue> prefixedPropertyValue, String applicationModelName,
			String userName) {

		userName = userName.replace(XSDDataTypes.string_typed.getXsdType(), "").replaceAll("\"", "");

		/*
		 * get all superClasses of normalUser class to identify that the new
		 * instance is also an instance of all super classes of normalUserClass
		 */

		for (Class superClass : normalUserClass.getSuperClassesList()) {
			prefixedPropertyValue
					.add(new PropertyValue("a", superClass.getPrefix().getPrefix() + superClass.getName()));
		}

		String insertQuery = QueryUtility.constructInsertQuery(
				Prefixes.IOT_PLATFORM.getPrefix() + userName.toLowerCase(), normalUserClass, prefixedPropertyValue);

		try {

			ModelOracleSem model = ModelOracleSem.createOracleSemModel(oracle, applicationModelName);
			UpdateAction.parseExecute(insertQuery, model);
			model.close();

		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage(), "Normal User");
		}

	}

	/*
	 * getNormalUsers method returns all the normal users in the passed
	 * application model
	 */
	public List<Hashtable<String, Object>> getNormalUsers(String applicationModelName) {

		String applicationName = applicationModelName.replaceAll(" ", "").toUpperCase().substring(0,
				applicationModelName.length() - 6);

		String queryString = QueryUtility.constructSelectAllQueryNoFilters(NormalUser.getNormalUserInstance(),
				applicationModelName);
		List<Hashtable<String, Object>> normalUsersList = new ArrayList<>();

		try {
			ResultSet results = oracle.executeQuery(queryString, 0, 1);
			/*
			 * call constractResponeJsonObjectForListSelection method in
			 * selectionUtility class to construct the response json
			 */

			normalUsersList = selectionUtility.constractResponeJsonObjectForListSelection(applicationName, results,
					NormalUser.getNormalUserInstance());

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), "Normal User");
		}

		return normalUsersList;
	}

}
