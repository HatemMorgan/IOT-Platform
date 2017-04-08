package com.iotplatform.ontology;

import java.util.ArrayList;
import java.util.Hashtable;

/*
 * It defines Classes in ontologies which is one of the main requirements for a semantic ontology
 */
public class Class {
	private String name;
	private String uri;
	private Prefix prefix;

	/*
	 * uniqueIdentifier of the class it will be null if the plarform will
	 * generate UUID
	 * 
	 * or it will have a value if uniqueIdentifier is a current property of the
	 * class instance where the value will be passed by the user
	 */
	private String uniqueIdentifierPropertyName;

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

	public Class(String name, String uri, Prefix prefix, String uniqueIdentifierPropertyName,
			boolean hasTypeClasses) {
		this.name = name;
		this.uri = uri;
		this.prefix = prefix;
		this.htblPropUriName = new Hashtable<>();
		this.uniqueIdentifierPropertyName = uniqueIdentifierPropertyName;
		this.hasTypeClasses = hasTypeClasses;

		this.classTypesList = new Hashtable<>();
		this.properties = new Hashtable<>();
		this.superClassesList = new ArrayList<>();
		this.hasUniqueIdentifierProperty = uniqueIdentifierPropertyName == null ? false : true;

	}

	public String getUniqueIdentifierPropertyName() {
		return uniqueIdentifierPropertyName;
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

	public Prefix getPrefix() {
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
		this.properties.putAll(properties);
	}

	public void setHtblPropUriName(Hashtable<String, String> htblPropUriName) {
		this.htblPropUriName.putAll(htblPropUriName);
	}

	public void setClassTypesList(Hashtable<String, Class> classTypesList) {
		this.classTypesList = classTypesList;
	}

}
