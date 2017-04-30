package com.iotplatform.cache;

import java.util.LinkedHashMap;

import com.iotplatform.utilities.QueryCachedObjectUtility;

public class QueryCache {

	/*
	 * htblQueryCache takes the requestBody as the key and the value of the key
	 * is an object array of size 2 which contains the constructed query and
	 * htblSubjectVariables(that is used to parse returned results from
	 * database)
	 */
	private static LinkedHashMap<String, QueryCachedObjectUtility> htblQueryCache;

	public static LinkedHashMap<String, QueryCachedObjectUtility> getHtblQueryCache() {

		if (htblQueryCache == null)
			htblQueryCache = new LinkedHashMap<>();

		return htblQueryCache;
	}

}
