package com.iotplatform.exceptions;

import org.springframework.http.HttpStatus;

/*
 * exception raised when fields(properties) are not a a valid fields(not a static 
 * or dynamic properties in the target application domain)
 */
public class InvalidRequestFieldsException extends ErrorObjException {

	public InvalidRequestFieldsException(String domain,String fieldName) {
		super(HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.value(),
				"Invalid Field . No field with this name : "+fieldName+ ". Check the documentation to know which fields your application domain has.",
				domain);
	}

	
	public InvalidRequestFieldsException(String domain) {
		super(HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.value(),
				"Invalid Fields Name . Check the documentation to know which fields your application domain has.",
				domain);
	}
}
