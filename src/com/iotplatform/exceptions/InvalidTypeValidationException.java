package com.iotplatform.exceptions;

import java.util.Set;

import org.springframework.http.HttpStatus;

/*
 * InvalidTypeValidationException is raised when type validation failed 
 * 
 * eg: Coverage class which maps the iot-lite:Coverage has three subClasses (Circle,Polygon,Rectangle)
 * 
 * if the request body has a type field for coverage instance with a value which is not one of the above subClasses 
 * then this exception is raised
 */
public class InvalidTypeValidationException extends ErrorObjException {

	/*
	 * className represents the superClass eg.Coverage
	 * 
	 * domain represents the request insttance domain (which new instance the
	 * user is trying to insert)
	 */
	public InvalidTypeValidationException(String domain, Set<String> types, String className) {
		super(HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.value(), "Invalid type list for " + className
				+ " instance. The type field has value that must be one of " + types.toString(), domain);
	}

}
