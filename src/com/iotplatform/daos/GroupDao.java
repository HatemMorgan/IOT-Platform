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
import com.iotplatform.ontology.classes.Group;
import com.iotplatform.utilities.PropertyValue;
import com.iotplatform.utilities.SelectionUtility;
import com.iotplatform.utilities.QueryUtility;

import oracle.spatial.rdf.client.jena.ModelOracleSem;
import oracle.spatial.rdf.client.jena.Oracle;

@Repository("groupDao")
public class GroupDao {

	private Oracle oracle;
	private SelectionUtility selectionUtility;
	private Group groupClass;

	@Autowired
	public GroupDao(Oracle oracle, SelectionUtility selectionUtility, Group groupClass) {
		this.oracle = oracle;
		this.selectionUtility = selectionUtility;
		this.groupClass = groupClass;
	}

	/*
	 * insertGroup method inserts a new group to the passed application model
	 */

	public void insertGroup(ArrayList<PropertyValue> prefixedPropertyValue, String applicationModelName,
			String groupName) {

		groupName = groupName.replace(XSDDataTypes.string_typed.getXsdType(), "").replaceAll("\"", "").replaceAll(" ",
				"");

		/*
		 * get all superClasses of group class to identify that the new instance
		 * is also an instance of all super classes of groupClass
		 */
		for (Class superClass : groupClass.getSuperClassesList()) {
			prefixedPropertyValue
					.add(new PropertyValue("a", superClass.getPrefix().getPrefix() + superClass.getName()));
		}

		String insertQuery = QueryUtility.constructInsertQuery(
				Prefixes.IOT_PLATFORM.getPrefix() + groupName.toLowerCase(), groupClass, prefixedPropertyValue);
		System.out.println(insertQuery);
		try {

			ModelOracleSem model = ModelOracleSem.createOracleSemModel(oracle, applicationModelName);
			UpdateAction.parseExecute(insertQuery, model);
			model.close();

		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage(), "Group");
		}

	}

	/*
	 * getGroups method returns all the groups in the passed application model
	 */

	public List<Hashtable<String, Object>> getGroups(String applicationModelName) {

		String applicationName = applicationModelName.replaceAll(" ", "").toUpperCase().substring(0,
				applicationModelName.length() - 6);

		String queryString = QueryUtility.constructSelectAllQueryNoFilters(Group.getGroupInstance(),
				applicationModelName);
		List<Hashtable<String, Object>> groupsList = new ArrayList<>();

		try {
			ResultSet results = oracle.executeQuery(queryString, 0, 1);
			/*
			 * call constractResponeJsonObjectForListSelection method in
			 * selectionUtility class to construct the response json
			 */

			groupsList = selectionUtility.constractResponeJsonObjectForListSelection(applicationName, results,
					Group.getGroupInstance());

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), "Group");
		}

		return groupsList;
	}

}
