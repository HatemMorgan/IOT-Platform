package com.iotplatform.models;

import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.http.HttpStatus;

public class SuccessfullSelectAllJsonModel {

	LinkedHashMap<String, Object> json;

	public SuccessfullSelectAllJsonModel(LinkedHashMap<String, Object> errJson) {
		this.json = errJson;
	}

	public SuccessfullSelectAllJsonModel(List<LinkedHashMap<String, Object>> resultsJson, double timeTaken) {
		json = new LinkedHashMap<>();
		json.put("code", HttpStatus.OK);
		json.put("message", "Successfull Request");
		json.put("results", resultsJson);
		json.put("time", timeTaken + " sec");
	}

	public LinkedHashMap<String, Object> getJson() {
		return json;
	}

}
