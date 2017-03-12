package com.iotplatform.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidDynamicConceptException extends ErrorObjException {

	public InvalidDynamicConceptException(String exceptionMessage) {
		super(HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.value(), exceptionMessage, "Ontology");
	}

}
