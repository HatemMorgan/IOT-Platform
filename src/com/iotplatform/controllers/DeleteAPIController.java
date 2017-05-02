package com.iotplatform.controllers;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iotplatform.services.DeleteService;

@RestController
public class DeleteAPIController {

	@Autowired
	DeleteService deleteService;

	@RequestMapping(value = "/deleteAPI/{applicationNameCode}/{instanceType}/{individualUnqiueIdentifier}", method = RequestMethod.DELETE)
	public LinkedHashMap<String, Object> updateController(
			@PathVariable(value = "applicationNameCode") String applicationNameCode,
			@PathVariable(value = "instanceType") String instanceType,
			@PathVariable(value = "individualUnqiueIdentifier") String individualUnqiueIdentifier,
			@RequestBody LinkedHashMap<String, Object> htblRequestBody) {

		return deleteService.deleteData(applicationNameCode, instanceType, individualUnqiueIdentifier, htblRequestBody);

	}
}
