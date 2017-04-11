package com.iotplatform.controllers;

import java.util.Hashtable;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iotplatform.services.AdminService;

@RestController
public class AdminAPIController {

	@Autowired
	AdminService adminService;

	@RequestMapping(value = "/admin/{applicationNameCode}", method = RequestMethod.POST)
	public Hashtable<String, Object> insertNewAdmin(@PathVariable("applicationNameCode") String applicationNameCode,
			@RequestBody Hashtable<String, Object> htblPropValue) {
		Hashtable<String, Object> responseJSON = adminService.insertAdmin(htblPropValue, applicationNameCode);
		return responseJSON;
	}

	@RequestMapping(value = "/admin/{applicationNameCode}", method = RequestMethod.GET, produces = "application/json")
	public Hashtable<String, Object> getAdmins(@PathVariable("applicationNameCode") String applicationNameCode) {

		return adminService.getAdmins(applicationNameCode);
	}

	@RequestMapping(value = "/test", method = RequestMethod.POST)
	public Hashtable<String, Object> test(@RequestBody Hashtable<String, Object> htblPropValue) {
		Iterator<String> iterator = htblPropValue.keySet().iterator();
		while(iterator.hasNext()){
			String key = iterator.next();
			Object value = htblPropValue.get(key);
			System.out.println(value.getClass().getName());
		}
		
		return new Hashtable<>();
	}
}
