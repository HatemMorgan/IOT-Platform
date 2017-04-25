package com.iotplatform.validations;

/*
 * UpdateRequestValidation class is used to validate update put request body and parse it.
 * 
 *  It checks for Obligatory fields exist in the request body (fields that must be exist for that class 
 *  because not all fields are required to be exist)
 *  
 *  It checks that fields passed by the request are valid fields by checking that they maps to existing properties 
 *  in the passed subject class (which maps the ontology classes)
 *  
 *  It parse request body (JSON) into classes and properties in order to perform a mapping from JSON to semantic web
 *  structure so it can then be validated for constraint violation and then be inserted in the appropriate format in 
 *  the graph database
 * 
 */
public class UpdateRequestValidation {

}
