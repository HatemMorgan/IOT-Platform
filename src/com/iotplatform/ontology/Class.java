package com.iotplatform.ontology;

import java.util.ArrayList;
import java.util.Hashtable;

/*
 * It defines Classes in ontologies which is one of the main requirements for a semantic ontology
 */
public class Class {
	private String name;
	private String uri;
	private Prefixes prefix;

	/*
	 * uniqueIdentifier of the class it will be null if the plarform will
	 * generate UUID
	 * 
	 * or it will have a value if uniqueIdentifier is a current property of the
	 * class instance where the value will be passed by the user
	 */
	private Property uniqueIdentifierProperty;

	/*
	 * boolean represents if uniqueIdentifierProperty is null or not
	 */
	private boolean hasUniqueIdentifierProperty;

	/*
	 * hashtable of class properties with key propertyName and value property
	 */
	private Hashtable<String, Property> properties;

	/*
	 * hashtable of class properties with key propertyUri and value
	 * propertyName. this list is used to get the propertyName when you are
	 * given propertyURI. It is used to construct jsonObject after quering to
	 * remove prefix from property
	 */
	private Hashtable<String, String> htblPropUriName;

	/*
	 * list of superClasses of the class instance
	 */
	private ArrayList<Class> superClassesList;

	/*
	 * this boolean reperesents if the class has types eg. measurementProperties
	 */
	private boolean hasTypeClasses;

	/*
	 * ClassTypesList is a list of classes types
	 */
	private Hashtable<String, Class> classTypesList;

	public Class(String name, String uri, Prefixes prefix, Property uniqueIdentifierProperty) {
		this.name = name;
		this.uri = uri;
		this.prefix = prefix;
		this.htblPropUriName = new Hashtable<>();
		properties = new Hashtable<>();
		superClassesList = new ArrayList<>();

		this.uniqueIdentifierProperty = uniqueIdentifierProperty;
		hasUniqueIdentifierProperty = uniqueIdentifierProperty == null ? false : true;

	}

	public Class(String name, String uri, Prefixes prefix, Property uniqueIdentifierProperty, boolean hasTypeClasses) {
		this.name = name;
		this.uri = uri;
		this.prefix = prefix;
		this.htblPropUriName = new Hashtable<>();
		this.uniqueIdentifierProperty = uniqueIdentifierProperty;
		this.hasTypeClasses = hasTypeClasses;

		this.classTypesList = hasTypeClasses == true ? new Hashtable<>() : null;
		this.properties = new Hashtable<>();
		this.superClassesList = new ArrayList<>();
		this.hasUniqueIdentifierProperty = uniqueIdentifierProperty == null ? false : true;

	}

	public Property getUniqueIdentifierProperty() {
		return uniqueIdentifierProperty;
	}

	public boolean isHasUniqueIdentifierProperty() {
		return hasUniqueIdentifierProperty;
	}

	public String getName() {
		return name;
	}

	public String getUri() {
		return uri;
	}

	public Prefixes getPrefix() {
		return prefix;
	}

	public Hashtable<String, Property> getProperties() {
		return properties;
	}

	public Hashtable<String, String> getHtblPropUriName() {
		return htblPropUriName;
	}

	public ArrayList<Class> getSuperClassesList() {
		return superClassesList;
	}

	public boolean isHasTypeClasses() {
		return hasTypeClasses;
	}

	public Hashtable<String, Class> getClassTypesList() {
		return classTypesList;
	}

	public void setProperties(Hashtable<String, Property> properties) {
		this.properties = properties;
	}

	public void setHtblPropUriName(Hashtable<String, String> htblPropUriName) {
		this.htblPropUriName = htblPropUriName;
	}

	
}
