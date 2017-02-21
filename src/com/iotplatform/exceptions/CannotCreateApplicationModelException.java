package com.iotplatform.exceptions;

import org.springframework.http.HttpStatus;

/*
 * This exception is used when a user want to create a new application 
 * and there is an application in the database that have the same name 
 * the application name has to be unique.
 */
public class CannotCreateApplicationModelException extends ErrorObjException {

	public CannotCreateApplicationModelException( String exceptionMessage,
			String domain) {
		super("Bad Request", HttpStatus.BAD_REQUEST.value(), exceptionMessage, domain);
		
	}
	
}
