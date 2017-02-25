package com.iotplatform.models;

import java.util.Hashtable;
import java.util.List;

import org.springframework.http.HttpStatus;

public class SuccessfullSelectAllJsonModel {

	Hashtable<String, Object> json;

	public SuccessfullSelectAllJsonModel(Hashtable<String, Object> errJson) {
		this.json = errJson;
	}

	public SuccessfullSelectAllJsonModel(List<Hashtable<String, Object>> resultsJson, double timeTaken) {
		json = new Hashtable<>();
		json.put("code", HttpStatus.OK);
		json.put("message", "Successfull Request");
		json.put("results", resultsJson);
		json.put("time", timeTaken + " sec");
	}

	public Hashtable<String, Object> getJson() {
		return json;
	}

}
