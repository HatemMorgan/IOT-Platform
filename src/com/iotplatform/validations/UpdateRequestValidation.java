package com.iotplatform.validations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.daos.ValidationDao;

/**
 * 
 *
 *
 * UpdateRequestValidation class is used to validate update put request body and
 * parse it.
 * 
 * Update Request is like : { "insert":{ } }, "update":{ } }
 * 
 * Where insert part contains the inserted fields that user will insert new.
 * Update part contains the update fields that the user need to update
 * 
 * 1- For insert part. System must make sure that it never insert a new value
 * for a property that has a singleValue
 * 
 * 2- For update part. System must make sure to update the right value for a
 * property that has multipleValues
 * 
 * UpdateRequestValidation will validate :
 * 
 * 1- It checks that fields passed by the request are valid fields by checking
 * that they maps to existing properties in the passed subject class (which maps
 * the ontology classes). It also load dynamic properties or classes that was
 * created for the requested application domain, to check if the fields that
 * does not mapped to a property in the mainOntology that it maps to a dynamic
 * one
 * 
 * 2- it checks that there is no unique constraint or data integrity constrains
 * Violations
 * 
 * 3- It parse request body (JSON) into classes and properties in order to
 * perform a mapping from JSON to semantic web structure to be used by
 * UpdatngQuery class to create update query
 * 
 * @author HatemMorgan
 */

@Component
public class UpdateRequestValidation {

	private ValidationDao validationDao;

	@Autowired
	public UpdateRequestValidation(ValidationDao validationDao) {
		this.validationDao = validationDao;
	}

}
