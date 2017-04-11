package com.iotplatform.controllers;

import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iotplatform.services.GroupService;

@RestController
public class GroupAPIController {

	@Autowired
	GroupService groupService;

	@RequestMapping(value = "/group/{applicationNameCode}", method = RequestMethod.POST)
	public Hashtable<String, Object> insertNewGroup(@PathVariable("applicationNameCode") String applicationNameCode,
			@RequestBody Hashtable<String, Object> htblPropValue) {
		Hashtable<String, Object> responseJSON = groupService.insertGroup(htblPropValue, applicationNameCode);
		return responseJSON;
	}

	@RequestMapping(value = "/group/{applicationNameCode}", method = RequestMethod.GET, produces = "application/json")
	public Hashtable<String, Object> getGroups(@PathVariable("applicationNameCode") String applicationNameCode) {

		return groupService.getGroups(applicationNameCode);
	}
}
