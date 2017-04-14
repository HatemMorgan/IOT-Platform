package com.iotplatform.controllers;

import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iotplatform.services.DynamicInsertionService;

@RestController
public class DynamicAPIController {

	DynamicInsertionService dynamicInsertionService;

	@Autowired
	public DynamicAPIController(DynamicInsertionService dynamicInsertionService) {
		this.dynamicInsertionService = dynamicInsertionService;
		System.out.println("DynamicAPIController Created");
	}

	
	
}
