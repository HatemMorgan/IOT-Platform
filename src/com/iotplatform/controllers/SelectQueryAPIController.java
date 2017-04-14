package com.iotplatform.controllers;

import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iotplatform.services.SelectQueryService;

@RestController
public class SelectQueryAPIController {

	@Autowired
	SelectQueryService selectQueryService;
	
	@RequestMapping(value = "/selectQueryAPI/{applicationNameCode}/{instanceType}", method = RequestMethod.POST)
	public Hashtable<String, Object> queryData(@PathVariable(value = "applicationNameCode") String applicationNameCode,
			@PathVariable(value = "instanceType") String instanceType,
			@RequestBody Hashtable<String, Object> htblFieldValue) {

		Hashtable<String, Object> responseJSON = selectQueryService.QueryData(applicationNameCode, instanceType,
				htblFieldValue);

		return responseJSON;
	}

}
