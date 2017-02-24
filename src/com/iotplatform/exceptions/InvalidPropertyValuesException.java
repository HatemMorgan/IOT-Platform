package com.iotplatform.exceptions;

import org.springframework.http.HttpStatus;

/*
 * This exception raised when values of the fields has wrong datatype or no instance equal 
 * to the instance passed (when the propery is an objectProperty)
 */
public class InvalidPropertyValuesException extends ErrorObjException {

	public InvalidPropertyValuesException(String domain, String fieldName) {
		super(HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.value(),
				"Wrong values Data type or object does not exist for field : " + fieldName + ". Check documentations ",
				domain);
	}

	
	public InvalidPropertyValuesException(String domain) {
		super(HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.value(),
				"Object value passed with some fields does not exist for field.Query your data to check for valid Objects value or insert these new objects value to the given fields. ",
				domain);
	}

}
