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
import com.iotplatform.utilities.QueryResultUtility;
import com.iotplatform.utilities.QueryUtility;

import oracle.spatial.rdf.client.jena.ModelOracleSem;
import oracle.spatial.rdf.client.jena.Oracle;

@Repository("adminDao")
public class AdminDao {

	private Oracle oracle;
	private QueryResultUtility queryResultUtility;
	private Admin adminClass;

	@Autowired
	public AdminDao(Oracle oracle, QueryResultUtility queryResultUtility, Admin adminClass) {
		this.oracle = oracle;
		this.queryResultUtility = queryResultUtility;
		this.adminClass = adminClass;
	}

	/*
	 *  insertAdmin method inserts a new admin to the passed application model
	 */
	
	public void insertAdmin(Hashtable<String, Object> htblPropValue, String applicationModelName) {

		String userName = htblPropValue.get("foaf:userName").toString()
				.replace(XSDDataTypes.string_typed.getXsdType(), "").replaceAll("\"", "");

		/*
		 * Identifying that the admin instance is also a person instance
		 */
		for (Class superClass : adminClass.getSuperClassesList()) {
			htblPropValue.put("a", superClass.getPrefix().getPrefix() + superClass.getName());
		}

		String insertQuery = QueryUtility.constructInsertQuery(
				Prefixes.IOT_PLATFORM.getPrefix() + userName.toLowerCase(), adminClass, htblPropValue);

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

		String queryString = QueryUtility.constructSelectAllQueryNoFilters(adminClass, applicationModelName);
		List<Hashtable<String, Object>> adminsList = new ArrayList<>();
		long startTime = System.currentTimeMillis();

		try {
			ResultSet res = oracle.executeQuery(queryString, 0, 1);
			Hashtable<Object, Hashtable<String, Object>> temp = new Hashtable<>();
			while (res.next()) {

				Object subject = res.getObject(1);
				if (temp.size() == 0) {
					Hashtable<String, Object> htblAdminPropVal = new Hashtable<>();
					temp.put(subject, htblAdminPropVal);
					adminsList.add(htblAdminPropVal);
				}

				/*
				 * as long as the current subject equal to subject got from the
				 * results then add the property and value to the admin's
				 * hashtable . If they are not the same this means that this is
				 * a new admin so we have to construct a new hashtable to hold
				 * it data
				 */

				// skip rdf:type property
				if (res.getString(2).equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
					continue;
				}
				Object[] preparedPropVal = queryResultUtility.constructQueryResult(applicationName, res.getString(2),
						res.getString(3), adminClass);
				String propertyName = preparedPropVal[0].toString();
				Object value = preparedPropVal[1];

				if (temp.containsKey(subject)) {
					temp.get(subject).put(propertyName, value);
				} else {

					Hashtable<String, Object> htblAdminPropVal = new Hashtable<>();
					temp.put(subject, htblAdminPropVal);
					temp.get(subject).put(propertyName, value);
					adminsList.add(htblAdminPropVal);

				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), "Admin");
		}

		return adminsList;
	}

}
