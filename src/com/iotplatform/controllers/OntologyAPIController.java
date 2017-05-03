package com.iotplatform.controllers;

import java.util.Hashtable;
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
import com.iotplatform.services.OntologyService;

@RestController
public class OntologyAPIController {

	@Autowired
	OntologyService ontologyService;

	@CrossOrigin("*")
	@RequestMapping(value = "/ontology/{applicationName}", method = RequestMethod.GET)
	public LinkedHashMap<String, Object> loadOntology(@PathVariable(value = "applicationName") String applicationName) {

		LinkedHashMap<String, Object> responseJSON = ontologyService.getApplicationOntology(applicationName);

		// ResponseEntity<LinkedHashMap<String, Object>> response = new
		// ResponseEntity<>(HttpStatus.OK);
		// response.getHeaders().putAll(AppConfig.HTTP_HEADERS);
		// response.getBody().putAll(responseJSON);
		//
		// return response;

		return responseJSON;
	}

	@CrossOrigin("*")
	@RequestMapping(value = "/ontology/newClass/{applicationNameCode}", method = RequestMethod.POST)
	public LinkedHashMap<String, Object> insertNewClass(
			@PathVariable(value = "applicationNameCode") String applicationNameCode,
			@RequestBody Hashtable<String, Object> htblRequestBody) {

		LinkedHashMap<String, Object> responseJSON = ontologyService.insertNewClass(applicationNameCode,
				htblRequestBody);

		// ResponseEntity<LinkedHashMap<String, Object>> response = new
		// ResponseEntity<>(HttpStatus.OK);
		// response.getHeaders().putAll(AppConfig.HTTP_HEADERS);
		// response.getBody().putAll(responseJSON);
		//
		// return response;
		return responseJSON;
	}

	@CrossOrigin("*")
	@RequestMapping(value = "/ontology/newObjectProperty/{applicationNameCode}", method = RequestMethod.POST)
	public LinkedHashMap<String, Object> insertNewObjectProperty(
			@PathVariable(value = "applicationNameCode") String applicationNameCode,
			@RequestBody Hashtable<String, Object> htblRequestBody) {

		LinkedHashMap<String, Object> responseJSON = ontologyService.insertNewObjectProperty(applicationNameCode,
				htblRequestBody);

		// ResponseEntity<LinkedHashMap<String, Object>> response = new
		// ResponseEntity<>(HttpStatus.OK);
		// response.getHeaders().putAll(AppConfig.HTTP_HEADERS);
		// response.getBody().putAll(responseJSON);
		//
		// return response;

		return responseJSON;
	}

	@CrossOrigin("*")
	@RequestMapping(value = "/ontology/newDataTypeProperty/{applicationNameCode}", method = RequestMethod.POST)
	public LinkedHashMap<String, Object> insertNewDataTypeProperty(
			@PathVariable(value = "applicationNameCode") String applicationNameCode,
			@RequestBody Hashtable<String, Object> htblRequestBody) {

		LinkedHashMap<String, Object> responseJSON = ontologyService.insertNewDataTypeProperty(applicationNameCode,
				htblRequestBody);

		// ResponseEntity<LinkedHashMap<String, Object>> response = new
		// ResponseEntity<>(HttpStatus.OK);
		// response.getHeaders().putAll(AppConfig.HTTP_HEADERS);
		// response.getBody().putAll(responseJSON);
		//
		// return response;

		return responseJSON;
	}

}
