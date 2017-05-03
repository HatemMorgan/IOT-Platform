package com.iotplatform.exceptions;

import org.springframework.http.HttpStatus;

/*
 * InvalidQueryRequestBodyException raised when the request body has invalid format
 */
public class InvalidQueryRequestBodyFormatException extends ErrorObjException {

	public InvalidQueryRequestBodyFormatException(String exceptionMessage) {
		super(HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.value(),
				"Invalid request body format. " + exceptionMessage, "Query API");
	}

}
