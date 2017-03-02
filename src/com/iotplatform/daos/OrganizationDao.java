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
import com.iotplatform.utilities.QueryResultUtility;
import com.iotplatform.utilities.QueryUtility;

import oracle.spatial.rdf.client.jena.ModelOracleSem;
import oracle.spatial.rdf.client.jena.Oracle;

@Repository("organizationDao")
public class OrganizationDao {

	private Oracle oracle;
	private QueryResultUtility queryResultUtility;
	private Organization organizationClass;

	@Autowired
	public OrganizationDao(Oracle oracle, QueryResultUtility queryResultUtility, Organization organizationClass) {
		this.oracle = oracle;
		this.queryResultUtility = queryResultUtility;
		this.organizationClass = organizationClass;
	}

	/*
	 * insertOrganization method inserts a new organization to the passed
	 * application model
	 */

	public void insertOrganization(Hashtable<String, Object> htblPropValue, String applicationModelName) {

		String organizationName = htblPropValue.get("foaf:name").toString()
				.replace(XSDDataTypes.string_typed.getXsdType(), "").replaceAll("\"", "").replaceAll(" ", "");

		/*
		 * get all superClasses of organization class to identify that the new instance
		 * is also an instance of all super classes of organizationClass
		 */
		
		for (Class superClass : organizationClass.getSuperClassesList()) {
			htblPropValue.put("a", superClass.getPrefix().getPrefix() + superClass.getName());
		}

		String insertQuery = QueryUtility.constructInsertQuery(
				Prefixes.IOT_PLATFORM.getPrefix() + organizationName.toLowerCase(), organizationClass, htblPropValue);

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

		String queryString = QueryUtility.constructSelectAllQueryNoFilters(organizationClass, applicationModelName);
		List<Hashtable<String, Object>> organizationsList = new ArrayList<>();

		try {
			ResultSet res = oracle.executeQuery(queryString, 0, 1);
			Hashtable<Object, Hashtable<String, Object>> temp = new Hashtable<>();
			while (res.next()) {

				Object subject = res.getObject(1);
				if (temp.size() == 0) {
					Hashtable<String, Object> htblOrganizationPropVal = new Hashtable<>();
					temp.put(subject, htblOrganizationPropVal);
					organizationsList.add(htblOrganizationPropVal);
				}

				/*
				 * as long as the current subject equal to subject got from the
				 * results then add the property and value to the organization's
				 * hashtable . If they are not the same this means that this is
				 * a new organization so we have to construct a new hashtable to hold
				 * it data
				 */

				// skip rdf:type property
				if (res.getString(2).equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
					continue;
				}
				Object[] preparedPropVal = queryResultUtility.constructQueryResult(applicationName, res.getString(2),
						res.getString(3), organizationClass);

				String propertyName = preparedPropVal[0].toString();
				Object value = preparedPropVal[1];

				if (temp.containsKey(subject)) {
					temp.get(subject).put(propertyName, value);
				} else {

					Hashtable<String, Object> htblOrganizationPropVal = new Hashtable<>();
					temp.put(subject, htblOrganizationPropVal);

					temp.get(subject).put(propertyName, value);

					organizationsList.add(htblOrganizationPropVal);

				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), "Organization");
		}

		return organizationsList;
	}

}
