package com.iotplatform.controllers;

import java.util.Hashtable;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iotplatform.configs.AppConfig;
import com.iotplatform.services.SelectQueryService;

@RestController
public class SelectQueryAPIController {

	@Autowired
	SelectQueryService selectQueryService;

	@CrossOrigin("*")
	@RequestMapping(value = "/selectQueryAPI/{applicationNameCode}/{instanceType}", method = RequestMethod.POST)
	public LinkedHashMap<String, Object> queryData(
			@PathVariable(value = "applicationNameCode") String applicationNameCode,
			@PathVariable(value = "instanceType") String instanceType,
			@RequestBody LinkedHashMap<String, Object> htblFieldValue) {

		LinkedHashMap<String, Object> responseJSON = selectQueryService.QueryData(applicationNameCode, instanceType,
				htblFieldValue);

		// ResponseEntity<LinkedHashMap<String, Object>> response = new
		// ResponseEntity<>(HttpStatus.OK);
		// response.getHeaders().putAll(AppConfig.HTTP_HEADERS);
		// response.getBody().putAll(responseJSON);
		//
		// return response;

		return responseJSON;
	}

}
