package com.iotplatform.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.iotplatform.cache.QueryCache;
import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.queries.SelectionQuery;
import com.iotplatform.query.results.SelectionQueryResults;
import com.iotplatform.utilities.QueryCachedObjectUtility;
import com.iotplatform.utilities.QueryFieldUtility;
import com.iotplatform.utilities.QueryRequestValidationResultUtility;
import com.iotplatform.utilities.QueryVariableUtility;

import oracle.spatial.rdf.client.jena.Oracle;

/*
 *  SelectQueryDao is used to make select queries on data 
 */

@Repository("selectQueryDao")
public class SelectQueryDao {

	private Oracle oracle;

	@Autowired
	public SelectQueryDao(Oracle oracle) {
		this.oracle = oracle;
	}

	/*
	 * queryData method is used :
	 * 
	 * 1- create a dynamic select query from passed htblClassNameProperty by
	 * calling SelectionQuery.constructSelectQuery method
	 * 
	 * 2- it calls the SelectionUtility.constructQueryResult to construct the
	 * results in the form of List<Hashtable<String, Object>> to be returned to
	 * the user
	 */
	public List<LinkedHashMap<String, Object>> queryData(
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<QueryFieldUtility>>> htblClassNameProperty,
			String applicationModelName, Hashtable<String, Boolean> htblOptions,
			QueryCachedObjectUtility queryCachedObject, QueryRequestValidationResultUtility validationResult,
			String requestQueryStr) {

		Iterator<String> htblClassNamePropertyIterator = htblClassNameProperty.keySet().iterator();

		/*
		 * get first prefixedClassName which is the prefixedName of the passed
		 * request className because LinkedHashMap keep the order of the
		 * insertion unchanged and GetQueryRequestValiation insert the
		 * requestClassPrefixName firstly
		 */
		String prefixedClassName = htblClassNamePropertyIterator.next();
		String mainInstanceUniqueIdentifier = htblClassNameProperty.get(prefixedClassName).keySet().iterator().next();

		String queryString;
		Hashtable<String, QueryVariableUtility> htblSubjectVariables;
		if (queryCachedObject == null) {
			Object[] returnObject = SelectionQuery.constructSelectQuery(htblClassNameProperty, prefixedClassName,
					mainInstanceUniqueIdentifier, applicationModelName, htblOptions);
			queryString = returnObject[0].toString();
			htblSubjectVariables = (Hashtable<String, QueryVariableUtility>) returnObject[1];

			QueryCachedObjectUtility queryObject = new QueryCachedObjectUtility(returnObject, validationResult);
			QueryCache.getHtblQueryCache().put(requestQueryStr, queryObject);
		} else {
			Object[] cachedQUeryObject = queryCachedObject.getSelectionQueryResult();
			queryString = cachedQUeryObject[0].toString();
			htblSubjectVariables = (Hashtable<String, QueryVariableUtility>) cachedQUeryObject[1];

		}

		System.out.println(queryString);
		System.out.println(htblSubjectVariables);
		try {

			ResultSet results = oracle.executeQuery(queryString, 0, 1);

			return SelectionQueryResults.constructQueryResult(applicationModelName, results, prefixedClassName,
					htblSubjectVariables);

			// ResultSetMetaData rsmd = results.getMetaData();
			// int columnsNumber = rsmd.getColumnCount();
			// while (results.next()) {
			// for (int i = 1; i <= columnsNumber; i++) {
			// if (i > 1)
			// System.out.print(", ");
			// String columnValue = results.getString(i);
			// System.out.print(columnValue + " " + rsmd.getColumnName(i));
			// }
			// System.out.println("");
			// }
			// return null;

		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage(), prefixedClassName);
		}

	}

}
