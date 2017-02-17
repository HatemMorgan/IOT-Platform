package com.iotplatform.ontology;

/*
 * It defines a literal which has a value and a dataType
 */

public class Literal {
	private Object value;
	private DataType dataType;

	public Literal(Object value, DataType dataType) {
		this.value = value;
		this.dataType = dataType;
	}

	public Object getValue() {
		return value;
	}

	public DataType getDataType() {
		return dataType;
	}

}
