package com.iotplatform.controllers;

import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iotplatform.services.OrganizationService;

@RestController
public class OrganizationAPIController {

	@Autowired
	OrganizationService organizationService;

	@RequestMapping(value = "/organization/{applicationNameCode}", method = RequestMethod.POST)
	public Hashtable<String, Object> insertNewOrganization(
			@PathVariable("applicationNameCode") String applicationNameCode,
			@RequestBody Hashtable<String, Object> htblPropValue) {
		Hashtable<String, Object> responseJSON = organizationService.insertOrganization(htblPropValue,
				applicationNameCode);
		return responseJSON;
	}

	@RequestMapping(value = "/organization/{applicationNameCode}", method = RequestMethod.GET, produces = "application/json")
	public Hashtable<String, Object> getDevelopers(@PathVariable("applicationNameCode") String applicationNameCode) {

		return organizationService.getOrganizations(applicationNameCode);
	}
}
