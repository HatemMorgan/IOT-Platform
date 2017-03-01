package com.iotplatform.controllers;

import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iotplatform.services.NormalUserService;

@RestController
public class NormalUserController {

	@Autowired
	NormalUserService normalUserService;
	
	@RequestMapping(value = "/normalUser/{applicationNameCode}", method = RequestMethod.POST)
	public Hashtable<String, Object> insertNewNormalUser(@PathVariable("applicationNameCode") String applicationNameCode,
			@RequestBody Hashtable<String, Object> htblPropValue) {
		Hashtable<String, Object> responseJSON = normalUserService.insertNormalUser(htblPropValue, applicationNameCode);
		return responseJSON;
	}

	@RequestMapping(value = "/normalUser/{applicationNameCode}", method = RequestMethod.GET, produces = "application/json")
	public Hashtable<String, Object> getDevelopers(
			@PathVariable("applicationNameCode") String applicationNameCode) {
		
		return normalUserService.getNormalUsers(applicationNameCode);
	}
}
