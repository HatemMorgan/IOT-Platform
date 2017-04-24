package com.iotplatform.exceptions;

import java.util.LinkedHashMap;

public class ErrorObjException extends RuntimeException {

	private String message;
	private int code;
	private String exceptionMessage;
	private String domain;

	public ErrorObjException(String message, int code, String exceptionMessage, String domain) {
		super(exceptionMessage);
		this.message = message;
		this.code = code;
		this.exceptionMessage = exceptionMessage;
		this.domain = domain;
	}

	public String getMessage() {
		return message;
	}

	public int getCode() {
		return code;
	}

	public String getExceptionMessage() {
		return exceptionMessage;
	}

	public LinkedHashMap<String, Object> getExceptionHashTable(double timeTaken) {
		LinkedHashMap<String, Object> htblException = new LinkedHashMap<>();
		htblException.put("code", code);
		htblException.put("message", message);

		LinkedHashMap<String, Object>[] errorsArr = (LinkedHashMap<String, Object>[]) new LinkedHashMap<?, ?>[1];
		LinkedHashMap<String, Object> error = new LinkedHashMap<>();
		error.put("domain", domain);
		error.put("message", exceptionMessage);

		errorsArr[0] = error;

		htblException.put("errors", errorsArr);
		htblException.put("time", timeTaken + " sec");
		return htblException;
	}

}
