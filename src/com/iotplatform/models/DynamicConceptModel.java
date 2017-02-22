package com.iotplatform.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DynamicConceptModel {

	private String application_model;
	private String class_name;
	private String class_uri;
	private String class_prefix_uri;
	private String class_prefix_alias;
	private String property_name;
	private String property_uri;
	private String property_prefix_uri;
	private String property_prefix_alias;
	private String property_type;
	private String property_object_type;

	@JsonCreator
	public DynamicConceptModel(@JsonProperty("applicationName") String application_model,
			@JsonProperty("className") String class_name, @JsonProperty("classURI") String class_uri,
			@JsonProperty("classPrefixURI") String class_prefix_uri,
			@JsonProperty("classPrefixAlias") String class_prefix_alias,
			@JsonProperty("propertyName") String property_name, @JsonProperty("propertyURI") String property_uri,
			@JsonProperty("propertyPrefixURI") String property_prefix_uri,
			@JsonProperty("propertyPrefixAlias") String property_prefix_alias,
			@JsonProperty("propertyType") String property_type,
			@JsonProperty("propertyObjectType") String property_object_type) {

		this.application_model = application_model;
		this.class_name = class_name;
		this.class_uri = class_uri;
		this.class_prefix_uri = class_prefix_uri;
		this.class_prefix_alias = class_prefix_alias;
		this.property_name = property_name;
		this.property_uri = property_uri;
		this.property_prefix_uri = property_prefix_uri;
		this.property_prefix_alias = property_prefix_alias;
		this.property_type = property_type;
		this.property_object_type = property_object_type;
	}

	public DynamicConceptModel() {

	}

	public String getApplication_model() {
		return application_model;
	}

	public String getClass_name() {
		return class_name;
	}

	public String getClass_uri() {
		return class_uri;
	}

	public String getClass_prefix_uri() {
		return class_prefix_uri;
	}

	public String getClass_prefix_alias() {
		return class_prefix_alias;
	}

	public String getProperty_name() {
		return property_name;
	}

	public String getProperty_uri() {
		return property_uri;
	}

	public String getProperty_prefix_uri() {
		return property_prefix_uri;
	}

	public String getProperty_prefix_alias() {
		return property_prefix_alias;
	}

	public String getProperty_type() {
		return property_type;
	}

	public String getProperty_object_type() {
		return property_object_type;
	}

	public void setApplication_model(String application_model) {
		this.application_model = application_model;
	}

	public void setClass_name(String class_name) {
		this.class_name = class_name;
	}

	public void setClass_uri(String class_uri) {
		this.class_uri = class_uri;
	}

	public void setClass_prefix_uri(String class_prefix_uri) {
		this.class_prefix_uri = class_prefix_uri;
	}

	public void setClass_prefix_alias(String class_prefix_alias) {
		this.class_prefix_alias = class_prefix_alias;
	}

	public void setProperty_name(String property_name) {
		this.property_name = property_name;
	}

	public void setProperty_uri(String property_uri) {
		this.property_uri = property_uri;
	}

	public void setProperty_prefix_uri(String property_prefix_uri) {
		this.property_prefix_uri = property_prefix_uri;
	}

	public void setProperty_prefix_alias(String property_prefix_alias) {
		this.property_prefix_alias = property_prefix_alias;
	}

	public void setProperty_type(String property_type) {
		this.property_type = property_type;
	}

	public void setProperty_object_type(String property_object_type) {
		this.property_object_type = property_object_type;
	}

	@Override
	public String toString() {
		return "DynamicConceptModel [application_name=" + application_model + ", class_name=" + class_name
				+ ", class_uri=" + class_uri + ", class_prefix_uri=" + class_prefix_uri + ", class_prefix_alias="
				+ class_prefix_alias + ", property_name=" + property_name + ", property_uri=" + property_uri
				+ ", property_prefix_uri=" + property_prefix_uri + ", property_prefix_alias=" + property_prefix_alias
				+ ", property_type=" + property_type + ", property_object_type=" + property_object_type + "]";
	}

}
