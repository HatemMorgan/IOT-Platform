package com.iotplatform.exceptions;

import org.springframework.http.HttpStatus;

public class DatabaseException extends ErrorObjException {
	public DatabaseException(String exceptionMessage, String domain) {
		super("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value(), exceptionMessage,
				domain);
	}

}
