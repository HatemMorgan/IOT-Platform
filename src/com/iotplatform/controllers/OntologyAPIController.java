package com.iotplatform.controllers;

import java.util.Hashtable;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iotplatform.services.OntologyService;

@RestController
public class OntologyAPIController {

	@Autowired
	OntologyService ontologyService;

	@RequestMapping(value = "/ontology/{applicationName}", method = RequestMethod.GET)
	public LinkedHashMap<String, Object> loadOntology(@PathVariable(value = "applicationName") String applicationName) {

		LinkedHashMap<String, Object> responseJSON = ontologyService.getApplicationOntology(applicationName);
		return responseJSON;
	}

	@RequestMapping(value = "/ontology/newClass/{applicationNameCode}", method = RequestMethod.POST)
	public LinkedHashMap<String, Object> insertNewClass(
			@PathVariable(value = "applicationNameCode") String applicationNameCode,
			@RequestBody Hashtable<String, Object> htblRequestBody) {

		LinkedHashMap<String, Object> responseJSON = ontologyService.insertNewClass(applicationNameCode,
				htblRequestBody);
		return responseJSON;
	}

	@RequestMapping(value = "/ontology/newObjectProperty/{applicationNameCode}", method = RequestMethod.POST)
	public LinkedHashMap<String, Object> insertNewObjectProperty(
			@PathVariable(value = "applicationNameCode") String applicationNameCode,
			@RequestBody Hashtable<String, Object> htblRequestBody) {

		LinkedHashMap<String, Object> responseJSON = ontologyService.insertNewObjectProperty(applicationNameCode,
				htblRequestBody);
		return responseJSON;
	}

	@RequestMapping(value = "/ontology/newDataTypeProperty/{applicationNameCode}", method = RequestMethod.POST)
	public LinkedHashMap<String, Object> insertNewDataTypeProperty(
			@PathVariable(value = "applicationNameCode") String applicationNameCode,
			@RequestBody Hashtable<String, Object> htblRequestBody) {

		LinkedHashMap<String, Object> responseJSON = ontologyService.insertNewDataTypeProperty(applicationNameCode,
				htblRequestBody);
		return responseJSON;
	}

}
