package com.iotplatform.controllers;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iotplatform.services.OntologyService;

@RestController
public class OntologyAPIController {

	@Autowired
	OntologyService ontologyService;

	
	@RequestMapping(value = "/ontology/{applicationName}", method = RequestMethod.GET)
	public LinkedHashMap<String, Object> insertNewdata(
			@PathVariable(value = "applicationName") String applicationName) {

		LinkedHashMap<String, Object> responseJSON = ontologyService.getApplicationOntology(applicationName);
		return responseJSON;
	}

}
