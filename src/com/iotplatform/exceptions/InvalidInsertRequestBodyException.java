package com.iotplatform.exceptions;

import org.springframework.http.HttpStatus;

/**
 * 
 * InvalidInsertRequestBodyException raised when the insert request body is
 * invalid
 * 
 * @author HatemMorgan
 *
 */
public class InvalidInsertRequestBodyException extends ErrorObjException {

	public InvalidInsertRequestBodyException(String exceptionMessage) {
		super(HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.value(),
				"Invalid insert request body format. " + exceptionMessage, "Insert API");
	}

}
