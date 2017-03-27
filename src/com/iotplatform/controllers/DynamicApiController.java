package com.iotplatform.controllers;

import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iotplatform.services.DynamicInsertionService;

@RestController
public class DynamicApiController {

	DynamicInsertionService dynamicInsertionService;

	@Autowired
	public DynamicApiController(DynamicInsertionService dynamicInsertionService) {
		this.dynamicInsertionService = dynamicInsertionService;
		System.out.println("DynamicAPIController Created");
	}

	@RequestMapping(value = "/dynamicInsertionAPI/{applicationNameCode}/{instanceType}", method = RequestMethod.POST)
	public Hashtable<String, Object> insertNewAdmin(
			@PathVariable(value = "applicationNameCode") String applicationNameCode,
			@PathVariable(value = "instanceType") String instanceType,
			@RequestBody Hashtable<String, Object> htblFieldValue) {
		Hashtable<String, Object> responseJSON = dynamicInsertionService.insertNewFieldValueList(htblFieldValue,
				applicationNameCode, instanceType);
		return responseJSON;
	}

	@RequestMapping(value = "/dynamicInsertionAPI/{applicationNameCode}", method = RequestMethod.POST)
	public String test(@PathVariable("applicationNameCode") String applicationNameCode,
			@RequestBody Hashtable<String, Object> htblFieldValue) {
		return "response";
	}

	@RequestMapping(value = "/dynamicInsertionAPI/{applicationNameCode}/{instanceType}", method = RequestMethod.GET)
	public String test2(@RequestParam(value = "applicationNameCode", required = true) String applicationNameCode,
			@RequestParam(value = "instanceType", required = false) String instanceType,
			@RequestBody Hashtable<String, Object> htblFieldValue) {
		return "response";
	}

	@RequestMapping(value = "/dynamicInsertionAPI/{applicationNameCode}/{instanceType}", method = RequestMethod.DELETE)
	public String test3(@PathVariable(value = "applicationNameCode") String applicationNameCode,
			@PathVariable(value = "instanceType") String instanceType,
			@RequestBody Hashtable<String, Object> htblFieldValue) {
		return "response";
	}
}
