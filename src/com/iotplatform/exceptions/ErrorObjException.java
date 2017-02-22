package com.iotplatform.exceptions;

import java.util.Hashtable;


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

	public Hashtable<String, Object> getExceptionHashTable() {
		Hashtable<String, Object> htblException = new Hashtable<>();
		htblException.put("code", code);
		htblException.put("message", message);

		Hashtable<String, Object>[] errorsArr = (Hashtable<String, Object>[]) new Hashtable<?, ?>[1];
		Hashtable<String, Object> error = new Hashtable<>();
		error.put("domain", domain);
		error.put("message", exceptionMessage);

		errorsArr[0] = error;

		htblException.put("errors", errorsArr);
		return htblException;
	}

}
