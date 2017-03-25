package com.iotplatform.exceptions;

import org.springframework.http.HttpStatus;

/*
 * exception raised when fields(properties) are not a a valid fields(not a static 
 * or dynamic properties in the target application domain)
 */
public class InvalidRequestFieldsException extends ErrorObjException {

	/*
	 * 
	 * the classes in your application domain, where this class does not have
	 * this field. \n 2. A common mistake always happened that a your inserted
	 * object is instance of a superClass and the field inserted belongs to one
	 * of the subClasses. Properties are not inherited from a subClass to a
	 * superClass!
	 */
	public InvalidRequestFieldsException(String domain, String fieldName) {
		super(HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.value(),
				"Invalid Field . No field with this name : " + fieldName
						+ ". Check the documentation to know which fields your application domain has."
						+ "This error occurred if \n 1. The new instance inserted belongs to one of"
						+ "this field. \n 2. A common mistake always happened that a your inserted"
						+ "object is instance of a superClass and the field inserted belongs to one"
						+ "of the subClasses. Properties are not inherited from a subClass to a superClass!",
				domain);
	}

	public InvalidRequestFieldsException(String domain) {
		super(HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.value(),
				"Invalid Fields Name . Check the documentation to know which fields your application domain has."
						+ "This error occurred if \n 1. The new instance inserted belongs to one of"
						+ "this field. \n 2. A common mistake always happened that a your inserted"
						+ "object is instance of a superClass and the field inserted belongs to one"
						+ "of the subClasses. Properties are not inherited from a subClass to a superClass!",
				domain);
	}
}
