package com.iotplatform.controllers;

import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Autowired;
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

	@RequestMapping(value = "/insertionAPI/{applicationNameCode}/{instanceType}", method = RequestMethod.POST)
	public Hashtable<String, Object> insertNewdata(
			@PathVariable(value = "applicationNameCode") String applicationNameCode,
			@PathVariable(value = "instanceType") String instanceType,
			@RequestBody Hashtable<String, Object> htblFieldValue) {

		Hashtable<String, Object> responseJSON = insertionService.insertNewFieldValueList(htblFieldValue,
				applicationNameCode, instanceType);

		return responseJSON;
	}

}
