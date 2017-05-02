package com.iotplatform.models;

import java.util.LinkedHashMap;

import org.springframework.http.HttpStatus;

public class SuccessfullDeleteJSONModel {

	private LinkedHashMap<String, Object> responseJson;

	public SuccessfullDeleteJSONModel(String domain) {

		this.responseJson = new LinkedHashMap<>();
		responseJson.put("code", HttpStatus.ACCEPTED.value());
		responseJson.put("message", HttpStatus.ACCEPTED.name());
		responseJson.put("domain", domain);
	}

	public SuccessfullDeleteJSONModel(String domain, double time) {

		this(domain);
		responseJson.put("time", time + " sec");
	}

	public LinkedHashMap<String, Object> getResponseJson() {
		return responseJson;
	}

}
