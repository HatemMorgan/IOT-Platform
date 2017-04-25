package com.iotplatform.validations;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.daos.ValidationDao;
import com.iotplatform.ontology.Class;
import com.iotplatform.utilities.UpdatePropertyValue;
import com.iotplatform.utilities.ValueOfFieldNotMappedToStaticProperty;

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

	/**
	 * validateUpdateRequest validates the update request and do the above
	 * describes validation
	 * 
	 * @param applicationName
	 *            applicationName of the requested applicationDomain. Every
	 *            application has a unique name and it is needed to load its
	 *            dynamic added classes and properties to the mainOntology and
	 *            also to validate that no constraints violations on the
	 *            applicationModel that stores its data
	 * 
	 * @param htblRequestBody
	 *            htblRequestBody is a map LinkedHashMap that holds the fields
	 *            and values. The value can be: 1- The new value only if the
	 *            field maps to a single value property. 2- An object in the
	 *            form of {"oldValue": , "newValue": } if the field maps to a
	 *            multiValued property
	 * 
	 * @param subjectClass
	 *            subjectClass is the type of the individual that is updated. It
	 *            is used to make sure that the individual maps correctly to the
	 *            semantic model defined by ontology of this application domain
	 *            with applicationName passed.
	 * 
	 * @return ArrayList<UpdatePropertyValue>
	 * 
	 *         which is a list of UpdatePropertyValue that is the result of
	 *         parsing requestBody and it will be used by UpdateQuery class to
	 *         generate update query
	 */
	public ArrayList<UpdatePropertyValue> validateUpdateRequest(String applicationName,
			LinkedHashMap<String, Object> htblRequestBody, Class subjectClass) {

		/*
		 * validationResult is a list of UpdatePropertyValue that is the result
		 * of parsing requestBody and it will be used by UpdateQuery class to
		 * generate update query
		 */
		ArrayList<UpdatePropertyValue> validationResult = new ArrayList<>();

		/*
		 * Iterate over htblRequestBody to validate that the fields(key) maps to
		 * an actual property in the applicationDomain ontology and also check
		 * that no value of any of the fields (value) violates any constraints
		 * (unique constraints and data integrity constraints )
		 */
		Iterator<String> htblRequestBodyIter = htblRequestBody.keySet().iterator();
		while (htblRequestBodyIter.hasNext()) {
			String field = htblRequestBodyIter.next();

		}

		return validationResult;

	}

	/**
	 * isFieldMapsToStaticProperty checks if a field maps to a static property (
	 * has map in the list of properties of passed subject class) in the
	 * mainOntology or the dynamicOntology of the requested application
	 * 
	 * it
	 * 
	 * return false if there is no mapping and add subject class to passed
	 * classList in order to get dynamic properties of it and it will add the
	 * field and value to htblNotFoundFieldValue hashtable to be checked again
	 * after laading dynamic properties
	 * 
	 * uniqueIdentifer is a random generated id that is used in
	 * uniqueConstraintValidation as a reference to uniquePopertyValues of an
	 * instance
	 * 
	 * 
	 * @param subjectClass
	 *            subjectClass is the type of the individual that is updated. It
	 *            is used to make sure that the individual maps correctly to the
	 *            semantic model defined by ontology of this application domain
	 *            with applicationName passed.
	 * 
	 * @param fieldName
	 *            is the fieldName in requestBody that are checked if it maps to
	 *            a valid property from the propertiesList
	 *            of @param(subjectClass) in the application's ontology
	 * 
	 * @param value
	 *            The value of the field with @param(fieldName). It will be
	 *            added to a new instance from
	 *            ValueOfFieldNotMappedToStaticProperty class in
	 *            notFoundFieldValueList to be checked later after loading
	 *            dynamic added properties of @param(subjectClass)
	 * 
	 * @param notFoundFieldValueList
	 *            is a list of ValueOfFieldNotMappedToStaticProperty instances
	 *            that holds the not mapped fields and their values to be
	 *            checked again after loading dynamic added properties
	 *            to @param(subjectClass) in the requested applicationDomain
	 *            ontology
	 * 
	 * @return true if there is a mapping.
	 * 
	 *         false if there is no mapping and add subject class to passed
	 *         classList in order to get dynamic properties of it and it will
	 *         add the field and value to htblNotFoundFieldValue hashtable to be
	 *         checked again after loading dynamic properties
	 */
	private boolean isFieldMapsToStaticProperty(Class subjectClass, String fieldName, Object value,
			ArrayList<ValueOfFieldNotMappedToStaticProperty> notFoundFieldValueList) {

		/*
		 * check that fieldName maps to a property in subjectClass and return
		 * true if it maps
		 */
		if (subjectClass.getProperties().containsKey(fieldName)) {
			return true;
		} else {

			/*
			 * create a new ValueOfFieldNotMappedToStaticProperty instance to
			 * hold fieldName, fieldValue and subjectClass
			 * 
			 * add new ValueOfFieldNotMappedToStaticProperty instance to
			 * notFoundFieldValueList to be checked later after loading dynamic
			 * properties of subjectClass
			 * 
			 */
			ValueOfFieldNotMappedToStaticProperty notMappedFieldValue = new ValueOfFieldNotMappedToStaticProperty(
					subjectClass, value, fieldName);
			notFoundFieldValueList.add(notMappedFieldValue);

			/*
			 * return false becuase no mapping found
			 */
			return false;
		}
	}

}
