package com.iotplatform.exceptions;

import org.springframework.http.HttpStatus;

/**
 * 
 * @author HatemMorgan
 *
 *         NotSuppliedObligatoryFieldsException raised when the passed insert
 *         request does not contain some/all obligatory fields
 *
 */
public class NotSuppliedObligatoryFieldsException extends ErrorObjException {

	/**
	 * 
	 * @param obligatoryFields
	 *            Missing obligatory Fields for class with invalidClassName
	 * @param invalidClassName
	 *            Name of the class that has missing obligatory fields
	 * @param domain
	 *            is the insertRequest className passed by the user in the
	 *            request URL
	 */
	public NotSuppliedObligatoryFieldsException(String obligatoryFields, String invalidClassName, String domain) {
		super(HttpStatus.BAD_REQUEST.name(),
				HttpStatus.BAD_REQUEST.value(), "Invalid insert request body. Missing Obligatory fields: "
						+ obligatoryFields + " for " + invalidClassName + " class  which must be" + "added to body ",
				domain);
	}

}
