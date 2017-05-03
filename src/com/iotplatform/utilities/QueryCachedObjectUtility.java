package com.iotplatform.utilities;

public class QueryCachedObjectUtility {

	private Object[] selectionQueryResult;

	private QueryRequestValidationResultUtility queryRequestValidationResultUtility;

	public QueryCachedObjectUtility(Object[] selectionQueryResult,
			QueryRequestValidationResultUtility queryRequestValidationResultUtility) {
		this.selectionQueryResult = selectionQueryResult;
		this.queryRequestValidationResultUtility = queryRequestValidationResultUtility;
	}

	public Object[] getSelectionQueryResult() {
		return selectionQueryResult;
	}

	public QueryRequestValidationResultUtility getQueryRequestValidationResultUtility() {
		return queryRequestValidationResultUtility;
	}

}
