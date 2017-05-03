package com.iotplatform.controllers;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iotplatform.configs.AppConfig;
import com.iotplatform.services.UpdateService;

@RestController
public class UpdateAPIController {

	@Autowired
	UpdateService updateService;

	@CrossOrigin("*")
	@RequestMapping(value = "/updateAPI/{applicationNameCode}/{instanceType}/{individualUnqiueIdentifier}", method = RequestMethod.PUT)
	public LinkedHashMap<String, Object> updateController(
			@PathVariable(value = "applicationNameCode") String applicationNameCode,
			@PathVariable(value = "instanceType") String instanceType,
			@PathVariable(value = "individualUnqiueIdentifier") String individualUnqiueIdentifier,
			@RequestBody LinkedHashMap<String, Object> htblRequestBody) {

		LinkedHashMap<String, Object> responseJSON = updateService.update(applicationNameCode, instanceType,
				individualUnqiueIdentifier, htblRequestBody);

		// ResponseEntity<LinkedHashMap<String, Object>> response = new
		// ResponseEntity<>(HttpStatus.OK);
		// response.getHeaders().putAll(AppConfig.HTTP_HEADERS);
		// response.getBody().putAll(responseJSON);

		// return response;

		return responseJSON;
	}

}
