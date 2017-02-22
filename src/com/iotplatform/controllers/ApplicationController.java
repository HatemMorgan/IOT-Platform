package com.iotplatform.controllers;

import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iotplatform.services.ApplicationService;

@RestController
public class ApplicationController {
	@Autowired
	ApplicationService applicationService;
	
	@RequestMapping(value = "/application", method = RequestMethod.POST)
	public Hashtable<String, Object> update(@RequestBody Hashtable<String,Object> htblPropValue) {
		Hashtable<String, Object> responseJSON =  applicationService.insertApplication(htblPropValue);
		return responseJSON;
	}
	
}
