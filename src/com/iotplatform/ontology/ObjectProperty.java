package com.iotplatform.ontology;

/*
 * It reperesents a Data Property that is a subclass of Property .
 * It must have a subject of type class and a value of instance of Class
 */
public class ObjectProperty extends Property {
	private String objectClassName;

	public ObjectProperty(Class subjectClass, String name, Prefix prefix, String objectClassName,
			boolean mulitpleValues, boolean unique) {
		super(subjectClass, name, prefix, mulitpleValues, unique);
		this.objectClassName = objectClassName;
	}

	public ObjectProperty(Class subjectClass, String name, Prefix prefix, String objectClassName,
			String applicationName, int mulitpleValues, int unique) {
		super(subjectClass, name, prefix, applicationName, mulitpleValues, unique);
		this.objectClassName = objectClassName;
	}

	public String getObjectClassName() {
		return objectClassName;
	}

	@Override
	public String toString() {
		return "ObjectProperty [ getObject()=" + getObjectClassName() + ", getName()=" + getName() + ", getPrefix()="
				+ getPrefix() + ", getApplicationName()=" + getApplicationName() + ", isMulitpleValues()="
				+ isMulitpleValues() + ", isUnique()=" + isUnique() + ", getSubjectClass()=" + getSubjectClass() + "]";
	}

}
