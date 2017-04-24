package com.iotplatform.models;

import java.util.LinkedHashMap;

import org.springframework.http.HttpStatus;

public class SuccessfullInsertionModel {

	private LinkedHashMap<String, Object> responseJson;

	public SuccessfullInsertionModel(String domain) {

		this.responseJson = new LinkedHashMap<>();
		responseJson.put("code", HttpStatus.CREATED.value());
		responseJson.put("message", HttpStatus.CREATED.name());
		responseJson.put("domain", domain);
	}

	public SuccessfullInsertionModel(String domain, double time) {

		this(domain);
		responseJson.put("time", time + " sec");
	}

	public LinkedHashMap<String, Object> getResponseJson() {
		return responseJson;
	}

}
