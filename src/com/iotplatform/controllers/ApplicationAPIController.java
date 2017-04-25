package com.iotplatform.controllers;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iotplatform.services.ApplicationService;

@RestController
public class ApplicationAPIController {
	@Autowired
	ApplicationService applicationService;

	@RequestMapping(value = "/application", method = RequestMethod.POST)
	public LinkedHashMap<String, Object> insertNewApplication(@RequestBody LinkedHashMap<String, Object> htblPropValue) {
		LinkedHashMap<String, Object> responseJSON = applicationService.insertApplication(htblPropValue);
		return responseJSON;
	}

}
