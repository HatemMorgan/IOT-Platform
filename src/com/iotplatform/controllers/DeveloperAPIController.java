package com.iotplatform.controllers;

import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iotplatform.services.DeveloperService;

@RestController
public class DeveloperAPIController {

	@Autowired
	DeveloperService developerService;

	@RequestMapping(value = "/developer/{applicationNameCode}", method = RequestMethod.POST)
	public Hashtable<String, Object> insertNewDeveloper(@PathVariable("applicationNameCode") String applicationNameCode,
			@RequestBody Hashtable<String, Object> htblPropValue) {
		System.out.println(htblPropValue);
		Hashtable<String, Object> responseJSON = developerService.insertDeveloper(htblPropValue, applicationNameCode);
		return responseJSON;
	}

	@RequestMapping(value = "/developer/{applicationNameCode}", method = RequestMethod.GET, produces = "application/json")
	public Hashtable<String, Object> getDevelopers(
			@PathVariable("applicationNameCode") String applicationNameCode) {
		
		return developerService.getDevelopers(applicationNameCode);
	}
}
