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
import com.iotplatform.ontology.classes.Admin;
import com.iotplatform.utilities.PropertyValue;
import com.iotplatform.utilities.SelectionUtility;
import com.iotplatform.utilities.QueryUtility;

import oracle.spatial.rdf.client.jena.ModelOracleSem;
import oracle.spatial.rdf.client.jena.Oracle;

@Repository("adminDao")
public class AdminDao {

	private Oracle oracle;
	private SelectionUtility selectionUtility;
	private Admin adminClass;

	@Autowired
	public AdminDao(Oracle oracle, SelectionUtility selectionUtility, Admin adminClass) {
		this.oracle = oracle;
		this.selectionUtility = selectionUtility;
		this.adminClass = adminClass;
	}

	/*
	 * insertAdmin method inserts a new admin to the passed application model
	 */

	public void insertAdmin(ArrayList<PropertyValue> prefixedPropertyValue, String applicationModelName,
			String userName) {

		userName = userName.replace(XSDDataTypes.string_typed.getXsdType(), "").replaceAll("\"", "");

		/*
		 * get all superClasses of admin class to identify that the new instance
		 * is also an instance of all super classes of adminClass
		 */

		for (Class superClass : adminClass.getSuperClassesList()) {
			prefixedPropertyValue
					.add(new PropertyValue("a", superClass.getPrefix().getPrefix() + superClass.getName()));
		}

		String insertQuery = QueryUtility.constructInsertQuery(
				Prefixes.IOT_PLATFORM.getPrefix() + userName.toLowerCase(), adminClass, prefixedPropertyValue);

		try {

			ModelOracleSem model = ModelOracleSem.createOracleSemModel(oracle, applicationModelName);
			UpdateAction.parseExecute(insertQuery, model);
			model.close();

		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage(), "Admin");
		}

	}

	/*
	 * getAdmins method returns all the admins in the passed application model
	 */

	public List<Hashtable<String, Object>> getAdmins(String applicationModelName) {

		String applicationName = applicationModelName.replaceAll(" ", "").toUpperCase().substring(0,
				applicationModelName.length() - 6);

		String queryString = QueryUtility.constructSelectAllQueryNoFilters(Admin.getAdminInstance(),
				applicationModelName);
		List<Hashtable<String, Object>> adminsList = new ArrayList<>();

		try {
			ResultSet results = oracle.executeQuery(queryString, 0, 1);

			/*
			 * call constractResponeJsonObjectForListSelection method in
			 * selectionUtility class to construct the response json
			 */

			adminsList = selectionUtility.constractResponeJsonObjectForListSelection(applicationName, results,
					Admin.getAdminInstance());

		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage(), "Admin");
		}

		return adminsList;
	}

}
