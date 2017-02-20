package com.iotplatform.controllers;

import java.util.Hashtable;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApplicationController {

	@RequestMapping(value = "/cars", method = RequestMethod.POST)
	public String update(@RequestBody Hashtable<String,Object> body) {
		System.out.println("here");
		System.out.println(body.toString());
		System.out.println(body.get("name"));
		System.out.println(((Integer)body.get("age"))+20);
		return "request submitted ";
	}

}
