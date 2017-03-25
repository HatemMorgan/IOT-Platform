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
import com.iotplatform.ontology.classes.Organization;
import com.iotplatform.utilities.PropertyValue;
import com.iotplatform.utilities.SelectionUtility;
import com.iotplatform.utilities.QueryUtility;

import oracle.spatial.rdf.client.jena.ModelOracleSem;
import oracle.spatial.rdf.client.jena.Oracle;

@Repository("organizationDao")
public class OrganizationDao {

	private Oracle oracle;
	private SelectionUtility selectionUtility;
	private Organization organizationClass;

	@Autowired
	public OrganizationDao(Oracle oracle, SelectionUtility selectionUtility, Organization organizationClass) {
		this.oracle = oracle;
		this.selectionUtility = selectionUtility;
		this.organizationClass = organizationClass;
	}

	/*
	 * insertOrganization method inserts a new organization to the passed
	 * application model
	 */

	public void insertOrganization(ArrayList<PropertyValue> prefixedPropertyValue, String applicationModelName,
			String organizationName) {

		organizationName = organizationName.replace(XSDDataTypes.string_typed.getXsdType(), "").replaceAll("\"", "")
				.replaceAll(" ", "");

		/*
		 * get all superClasses of organization class to identify that the new
		 * instance is also an instance of all super classes of
		 * organizationClass
		 */

		for (Class superClass : organizationClass.getSuperClassesList()) {
			prefixedPropertyValue
					.add(new PropertyValue("a", superClass.getPrefix().getPrefix() + superClass.getName()));
		}

		String insertQuery = QueryUtility.constructInsertQuery(
				Prefixes.IOT_PLATFORM.getPrefix() + organizationName.toLowerCase(), organizationClass,
				prefixedPropertyValue);

		try {

			ModelOracleSem model = ModelOracleSem.createOracleSemModel(oracle, applicationModelName);
			UpdateAction.parseExecute(insertQuery, model);
			model.close();

		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage(), "Organization");
		}

	}

	/*
	 * getOrganizations method returns all the organizations in the passed
	 * application model
	 */

	public List<Hashtable<String, Object>> getOrganizations(String applicationModelName) {

		String applicationName = applicationModelName.replaceAll(" ", "").toUpperCase().substring(0,
				applicationModelName.length() - 6);

		String queryString = QueryUtility.constructSelectAllQueryNoFilters(Organization.getOrganizationInstance(),
				applicationModelName);
		List<Hashtable<String, Object>> organizationsList = new ArrayList<>();

		try {

			ResultSet results = oracle.executeQuery(queryString, 0, 1);

			/*
			 * call constractResponeJsonObjectForListSelection method in
			 * selectionUtility class to construct the response json
			 */

			organizationsList = selectionUtility.constractResponeJsonObjectForListSelection(applicationName, results,
					Organization.getOrganizationInstance());

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), "Organization");
		}

		return organizationsList;
	}

}
