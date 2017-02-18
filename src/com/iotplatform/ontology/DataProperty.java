package com.iotplatform.ontology;

/*
 * It reperesents a Data Property that is a subclass of Property .
 * It must have a subject of type class and a value of literal (can be typed literal or not typed literal)
 */
public class DataProperty extends Property {

	private XSDDataTypes dataType;

	public DataProperty(String name, Prefixes prefix, XSDDataTypes dataType) {
		super(name, prefix);
		this.dataType = dataType;
	}

	public XSDDataTypes getDataType() {
		return dataType;
	}

}
