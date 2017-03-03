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
	private SelectionUtility queryResultUtility;
	private NormalUser normalUserClass;

	@Autowired
	public NormalUserDao(Oracle oracle, SelectionUtility queryResultUtility, NormalUser normalUserClass) {
		this.oracle = oracle;
		this.queryResultUtility = queryResultUtility;
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

		String queryString = QueryUtility.constructSelectAllQueryNoFilters(normalUserClass, applicationModelName);
		List<Hashtable<String, Object>> normalUsersList = new ArrayList<>();
		long startTime = System.currentTimeMillis();

		try {
			ResultSet res = oracle.executeQuery(queryString, 0, 1);
			Hashtable<Object, Hashtable<String, Object>> temp = new Hashtable<>();
			while (res.next()) {

				Object subject = res.getObject(1);
				if (temp.size() == 0) {
					Hashtable<String, Object> htblNormalUserPropVal = new Hashtable<>();
					temp.put(subject, htblNormalUserPropVal);
					normalUsersList.add(htblNormalUserPropVal);
				}

				/*
				 * as long as the current subject equal to subject got from the
				 * results then add the property and value to the admin's
				 * hashtable . If they are not the same this means that this is
				 * a new normal user so we have to construct a new hashtable to
				 * hold it data
				 */

				// skip rdf:type property
				if (res.getString(2).equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
					continue;
				}

				Object[] preparedPropVal = queryResultUtility.constructQueryResult(applicationName, res.getString(2),
						res.getString(3), normalUserClass);
				String propertyName = preparedPropVal[0].toString();
				Object value = preparedPropVal[1];

				if (temp.containsKey(subject)) {
					temp.get(subject).put(propertyName, value);
				} else {

					Hashtable<String, Object> htblAdminPropVal = new Hashtable<>();
					temp.put(subject, htblAdminPropVal);
					temp.get(subject).put(propertyName, value);
					normalUsersList.add(htblAdminPropVal);

				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), "Normal User");
		}

		return normalUsersList;
	}

}
