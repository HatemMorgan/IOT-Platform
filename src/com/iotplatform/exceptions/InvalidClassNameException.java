package com.iotplatform.exceptions;

import org.springframework.http.HttpStatus;

/*
 * This exception raised when the passed className in dynamicApis has not mapping which means that it is invalid
 */
public class InvalidClassNameException extends ErrorObjException {

	public InvalidClassNameException(String passedClassName) {
		super("Bad Request", HttpStatus.BAD_REQUEST.value(),
				passedClassName + " is not a valid class name. Check the documentation "
						+ "to know which classes your application schema has.",
				"Dynamic Insertion");
	}

}
