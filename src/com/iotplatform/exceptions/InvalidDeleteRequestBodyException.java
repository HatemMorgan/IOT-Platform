package com.iotplatform.exceptions;

import org.springframework.http.HttpStatus;

/**
 * 
 * InvalidDeleteRequestBodyException raised when the delete request body is
 * invalid
 * 
 * @author HatemMorgan
 *
 */
public class InvalidDeleteRequestBodyException extends ErrorObjException {

	public InvalidDeleteRequestBodyException(String exceptionMessage) {
		super(HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.value(),
				"Invalid delete request body format. " + exceptionMessage, "Delete API");
	}

}
