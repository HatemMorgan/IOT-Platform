package com.iotplatform.models;

import java.util.Hashtable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;



public class ApplicationModel {
	@JsonView(View.Summary.class)
	Hashtable<String,Object> list;

	@JsonCreator
	public ApplicationModel(Hashtable<String, Object>list) {
		this.list = list;
	}
}
