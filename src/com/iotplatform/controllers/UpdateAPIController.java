package com.iotplatform.controllers;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iotplatform.services.UpdateService;

@RestController
public class UpdateAPIController {

	@Autowired
	UpdateService updateService;

	@RequestMapping(value = "/updateAPI/{applicationNameCode}/{instanceType}/{individualUnqiueIdentifier}", method = RequestMethod.PUT)
	public LinkedHashMap<String, Object> updateController(
			@PathVariable(value = "applicationNameCode") String applicationNameCode,
			@PathVariable(value = "instanceType") String instanceType,
			@PathVariable(value = "individualUnqiueIdentifier") String individualUnqiueIdentifier,
			@RequestBody LinkedHashMap<String, Object> htblRequestBody) {

		return updateService.update(applicationNameCode, instanceType, individualUnqiueIdentifier, htblRequestBody);

	}

}
