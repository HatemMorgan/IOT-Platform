package com.iotplatform.models;

import java.util.LinkedHashMap;

import org.springframework.http.HttpStatus;

public class SuccessfullUpdateJSONModel {

	private LinkedHashMap<String, Object> responseJson;

	public SuccessfullUpdateJSONModel(String domain) {

		this.responseJson = new LinkedHashMap<>();
		responseJson.put("code", HttpStatus.OK.value());
		responseJson.put("message", HttpStatus.OK.name());
		responseJson.put("domain", domain);
	}

	public SuccessfullUpdateJSONModel(String domain, double time) {

		this(domain);
		responseJson.put("time", time + " sec");
	}

	public LinkedHashMap<String, Object> getResponseJson() {
		return responseJson;
	}

}
