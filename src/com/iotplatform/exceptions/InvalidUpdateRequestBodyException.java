package com.iotplatform.exceptions;

import org.springframework.http.HttpStatus;

/**
 * 
 * InvalidUpdateRequestBodyException raised when the update request body is
 * invalid
 * 
 * @author HatemMorgan
 *
 */
public class InvalidUpdateRequestBodyException extends ErrorObjException {

	public InvalidUpdateRequestBodyException(String exceptionMessage) {
		super(HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.value(),
				"Invalid update request body format. " + exceptionMessage, "Update API");
	}

}
