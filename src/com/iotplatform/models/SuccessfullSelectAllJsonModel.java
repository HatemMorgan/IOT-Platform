package com.iotplatform.models;

import java.util.Hashtable;
import java.util.List;

public class SuccessfullSelectAllJsonModel {

	Hashtable<String, Object> errJson;
	List<Hashtable<String, Object>> resultsJson;

	public SuccessfullSelectAllJsonModel(Hashtable<String, Object> errJson) {
		this.errJson = errJson;
		this.resultsJson = null;
	}

	public SuccessfullSelectAllJsonModel(List<Hashtable<String, Object>> resultsJson) {
		this.resultsJson = resultsJson;
		this.errJson = null;
	}

	@Override
	public String toString() {
		if(errJson != null){
			return errJson.toString();
		}
		
		if(resultsJson != null){
			return resultsJson.toString();
		}
		return "SuccessfullSelectAllJsonModel [errJson=" + errJson + ", resultsJson=" + resultsJson + "]";
	}

	
	
}
