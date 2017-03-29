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
	public Hashtable<String, Object> insertNewdata(
			@PathVariable(value = "applicationNameCode") String applicationNameCode,
			@PathVariable(value = "instanceType") String instanceType,
			@RequestBody Hashtable<String, Object> htblFieldValue) {

		Hashtable<String, Object> responseJSON = dynamicInsertionService.insertNewFieldValueList(htblFieldValue,
				applicationNameCode, instanceType);

		return responseJSON;
	}

	@RequestMapping(value = "/dynamicSelectAllAPI/{applicationNameCode}/{instanceType}", method = RequestMethod.GET)
	public Hashtable<String, Object> getData(@PathVariable(value = "applicationNameCode") String applicationNameCode,
			@PathVariable(value = "instanceType") String instanceType) {

		Hashtable<String, Object> responseJSON = dynamicInsertionService.selectAll(applicationNameCode, instanceType);

		return responseJSON;
	}

	@RequestMapping(value = "/query/{applicationNameCode}/{instanceType}", method = RequestMethod.POST)
	public Hashtable<String, Object> queryData(@PathVariable(value = "applicationNameCode") String applicationNameCode,
			@PathVariable(value = "instanceType") String instanceType,
			@RequestBody Hashtable<String, Object> htblFieldValue) {

		Hashtable<String, Object> responseJSON = dynamicInsertionService.QueryData(applicationNameCode, instanceType,
				htblFieldValue);

		return responseJSON;
	}

}
