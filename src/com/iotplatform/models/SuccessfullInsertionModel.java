package com.iotplatform.models;

import java.util.Hashtable;

import org.springframework.http.HttpStatus;

public class SuccessfullInsertionModel {

	private Hashtable<String, Object> responseJson;

	public SuccessfullInsertionModel(String domain) {

		this.responseJson = new Hashtable<>();
		responseJson.put("code", HttpStatus.CREATED.value());
		responseJson.put("message", HttpStatus.CREATED.name());
		responseJson.put("domain", domain);
	}

	public Hashtable<String, Object> getResponseJson() {
		return responseJson;
	}

}
