package com.iotplatform.exceptions;

import org.springframework.http.HttpStatus;

public class DatabaseException extends ErrorObjException {
	public DatabaseException(String exceptionMessage, int exceptionCode, String domain) {
		super("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value(), exceptionCode, exceptionMessage,
				domain);
	}

}
