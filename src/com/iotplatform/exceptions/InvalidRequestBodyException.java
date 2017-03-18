package com.iotplatform.exceptions;

import org.springframework.http.HttpStatus;

/*
 * InvalidRequestBodyException raised when the request body is invalid 
 * 
 * eg. A property that does not have multiple values (list of values) and the request body has this
 *  property with multiple values so this exception will be raised
 *  
 * eg. Wrong formated JSON  
 */
public class InvalidRequestBodyException extends ErrorObjException {

	public InvalidRequestBodyException(String fieldName, String reason, String domain) {
		super(HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.value(),
				"Wrong formated request body. Field with name: " + fieldName + " has invalid format because " + reason
						+ " .",
				domain);
	}

}
