package com.iotplatform.utilities;

/*
 *   SqlCondition is used to create and sql condition that has column name and column value 
 *   eg: class_name = "Person" 
 *   
 *   the above condition example can be used in dynamic_concepts table query method to construct a dynamic 
 *   where clause
 */

public class SqlCondition {
	private String colName;
	private String colValue;

	public SqlCondition(String colName, String colValue) {
		this.colName = colName;
		this.colValue = colValue;
	}

	public String getColName() {
		return colName;
	}

	public String getColValue() {
		return colValue;
	}

	@Override
	public String toString() {
		return "SqlCondition [colName=" + colName + ", colValue=" + colValue + "]";
	}

}
