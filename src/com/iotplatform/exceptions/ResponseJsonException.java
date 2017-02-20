package com.iotplatform.exceptions;

import org.springframework.http.HttpStatus;

public class ResponseJsonException extends RuntimeException {

	private String message;
	private int code;
	private int exceptionCode;
	private String exceptionMessage;

	public ResponseJsonException(String message, int code, int exceptionCode, String exceptionMessage) {
		super();
		this.message = message;
		this.code = code;
		this.exceptionCode = exceptionCode;
		this.exceptionMessage = exceptionMessage;
	}

	public String getMessage() {
		return message;
	}

	public int getCode() {
		return code;
	}

	public int getExceptionCode() {
		return exceptionCode;
	}

	public String getExceptionMessage() {
		return exceptionMessage;
	}

}
