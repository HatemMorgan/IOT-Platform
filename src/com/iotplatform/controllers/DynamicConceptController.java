package com.iotplatform.controllers;

import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iotplatform.models.DynamicConceptModel;
import com.iotplatform.services.DynamicConceptService;

@RestController
public class DynamicConceptController {

	@Autowired
	DynamicConceptService dynamicConceptService;

	@RequestMapping(value = "/ontology/{applicationCode}", method = RequestMethod.POST, produces = "application/json")
	public Hashtable<String, Object> createDynamicOntologyConcept(@PathVariable("applicationCode") String applicationCode,
			@RequestBody DynamicConceptModel newConcept) {

		return dynamicConceptService.insertNewConcept(applicationCode, newConcept);

	}

	@RequestMapping(value = "/ontology/{applicationCode}", method = RequestMethod.GET, produces = "application/json")
	public Hashtable<String, Object> getApplicationDynamicConcept(
			@PathVariable("applicationCode") String applicaitonCode) {
		return dynamicConceptService.getApplicationDynamicConcepts(applicaitonCode);

	}
}
