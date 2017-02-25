package com.iotplatform.exceptions;


import org.springframework.http.HttpStatus;

public class NoApplicationModelException extends ErrorObjException {

	static String prefix = "No Application model with this name: ";
	static String suffix = " . You have to register and create a new application with this name or enter the right name.";

	public NoApplicationModelException(String applicationName, String domain) {
		super("Model Not Found", HttpStatus.NOT_FOUND.value(), prefix + applicationName + suffix, domain);
		// TODO Auto-generated constructor stub
	}

}
