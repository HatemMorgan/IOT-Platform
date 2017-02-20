package com.iotplatform.exceptions;

import org.springframework.http.HttpStatus;

public class DatabaseException extends ResponseJsonException {
	public DatabaseException(String exceptionMessage , int exceptionCode) {
		super("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR.value(), exceptionCode, exceptionMessage);
	}

}
