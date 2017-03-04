package com.iotplatform.exceptions;

import org.springframework.http.HttpStatus;

public class UniqueConstraintViolationException extends ErrorObjException {

	public UniqueConstraintViolationException(String domain) {

		super(HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.value(),
				"Object values passed with some or all object value fields must be unique which violates"
						+ " unique constraints of object fields that have Object Values. "
						+ "Check the documentation of your application domain on the platform "
						+ "to know which unique fields your application domain has. ",
				domain);
	}

}
