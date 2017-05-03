package com.iotplatform.controllers;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iotplatform.configs.AppConfig;
import com.iotplatform.services.ApplicationService;

@RestController
public class ApplicationAPIController {
	@Autowired
	ApplicationService applicationService;

	@CrossOrigin("*")
	@RequestMapping(value = "/application", method = RequestMethod.POST)
	public LinkedHashMap<String, Object> insertNewApplication(
			@RequestBody LinkedHashMap<String, Object> htblPropValue) {
		LinkedHashMap<String, Object> responseJSON = applicationService.insertApplication(htblPropValue);

//		ResponseEntity<LinkedHashMap<String, Object>> response = new ResponseEntity<>(HttpStatus.OK);
//		response.getHeaders().putAll(AppConfig.HTTP_HEADERS);
//		response.getBody().putAll(responseJSON);
//
//		return response;

		 return responseJSON;
	}

}
