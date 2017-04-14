package com.iotplatform.controllers;

import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iotplatform.services.SelectAllQueryService;

@RestController
public class SelectAllQueryAPIController {

	@Autowired
	SelectAllQueryService selectAllQueryService;

	@RequestMapping(value = "/selectAllQueryAPI/{applicationNameCode}/{instanceType}", method = RequestMethod.GET)
	public Hashtable<String, Object> getData(@PathVariable(value = "applicationNameCode") String applicationNameCode,
			@PathVariable(value = "instanceType") String instanceType) {

		Hashtable<String, Object> responseJSON = selectAllQueryService.selectAll(applicationNameCode, instanceType);

		return responseJSON;
	}

}
