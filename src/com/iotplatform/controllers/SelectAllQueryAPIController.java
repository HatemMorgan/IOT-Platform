package com.iotplatform.controllers;

import java.util.Hashtable;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iotplatform.configs.AppConfig;
import com.iotplatform.services.SelectAllQueryService;

@RestController
public class SelectAllQueryAPIController {

	@Autowired
	SelectAllQueryService selectAllQueryService;

	@CrossOrigin("*")
	@RequestMapping(value = "/selectAllQueryAPI/{applicationNameCode}/{instanceType}", method = RequestMethod.GET)
	public LinkedHashMap<String, Object> getData(
			@PathVariable(value = "applicationNameCode") String applicationNameCode,
			@PathVariable(value = "instanceType") String instanceType) {

		LinkedHashMap<String, Object> responseJSON = selectAllQueryService.selectAll(applicationNameCode, instanceType);

//		ResponseEntity<LinkedHashMap<String, Object>> response = new ResponseEntity<>(HttpStatus.OK);
//		response.getHeaders().putAll(AppConfig.HTTP_HEADERS);
//		response.getBody().putAll(responseJSON);
		
		return responseJSON;
	}

}
