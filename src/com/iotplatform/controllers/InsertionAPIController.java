package com.iotplatform.controllers;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iotplatform.services.InsertionService;

@RestController
public class InsertionAPIController {

	@Autowired
	InsertionService insertionService;

	@CrossOrigin("*")
	@RequestMapping(value = "/insertionAPI/{applicationNameCode}/{instanceType}", method = RequestMethod.POST)
	public LinkedHashMap<String, Object> insertNewdata(
			@PathVariable(value = "applicationNameCode") String applicationNameCode,
			@PathVariable(value = "instanceType") String instanceType,
			@RequestBody LinkedHashMap<String, Object> htblFieldValue) {

		LinkedHashMap<String, Object> responseJSON = insertionService.insertNewFieldValueList(htblFieldValue,
				applicationNameCode, instanceType);
		//
		// ResponseEntity<LinkedHashMap<String, Object>> response = new
		// ResponseEntity<>(HttpStatus.OK);
		// response.getHeaders().putAll(AppConfig.HTTP_HEADERS);
		// response.getBody().putAll(responseJSON);
		//
		// return response;

		return responseJSON;

		// return new
		// ResponseEntity.headers(AppConfig.HTTP_HEADERS).body(responseJSON);
		// return responseJSON;
	}

}
