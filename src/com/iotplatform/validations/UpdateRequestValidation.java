package com.iotplatform.validations;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.daos.DynamicOntologyDao;
import com.iotplatform.daos.ValidationDao;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Property;
import com.iotplatform.utilities.UpdatePropertyValueUtility;
import com.iotplatform.utilities.UpdateRequestValidationResultUtility;
import com.iotplatform.utilities.ValueOfTypeClassUtility;
import com.iotplatform.utilities.NotMappedInsertRequestFieldUtility;

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

	private DynamicOntologyDao dynamicOntologyDao;

	@Autowired
	public UpdateRequestValidation(DynamicOntologyDao dynamicOntologyDao) {
		this.dynamicOntologyDao = dynamicOntologyDao;
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

	public UpdateRequestValidationResultUtility validateUpdateRequest(String applicationName,
			LinkedHashMap<String, Object> htblRequestBody, Class subjectClass) {

		/*
		 * validationResult is a list of UpdatePropertyValue that is the result
		 * of parsing requestBody and it will be used by UpdateQuery class to
		 * generate update query
		 */
		ArrayList<UpdatePropertyValueUtility> validationResult = new ArrayList<>();

		/*
		 * notMappedFieldsList is a list of all requestBody fields that has not
		 * mapping to a property in the application(with applicationName) domain
		 * ontology.
		 * 
		 * These fields will be validated again after loading dynamicProperties
		 * of the requested class (subjectClass)
		 */
		ArrayList<String> notMappedFieldsList = new ArrayList<>();

		/*
		 * uniquePropValueList is a LikedHashMap of key prefixedClassName and
		 * value LinkedHashMap<String,ArrayList<PropertyValue>> with key
		 * prefixedPropertyName and value list of propertyValue object that
		 * holds the unique propertyName and value I used LinkedHashMap to
		 * ensure that the property will not be duplicated for the
		 * prefixedClassName (this will improve efficiency by reducing graph
		 * patterns as there will never be duplicated properties)
		 * 
		 * This DataStructure instance is used in uniqueConstraintValidation
		 * 
		 * ex: {
		 * 
		 * foaf:Person={foaf:userName=[HaythamIsmailss, AhmedMorganls,
		 * HatemMorganss]},
		 * 
		 * foaf:Agent={foaf:mbox=[haytham.ismailss@gmail.com,
		 * haytham.ismailss@student.guc.edu.eg, ahmedmorganlss@gmail.com,
		 * hatemmorgan17ss@gmail.com, hatem.el-sayedss@student.guc.edu.eg]}
		 * 
		 * }
		 */
		LinkedHashMap<String, LinkedHashMap<String, ArrayList<Object>>> htblUniquePropValueList = new LinkedHashMap<>();

		/*
		 * classValueList is list of ValueOfTypeClass instances (holds
		 * objectValue and its classType). it will be used to check
		 * dataIntegrity constraints
		 */
		ArrayList<ValueOfTypeClassUtility> classValueList = new ArrayList<>();

		/*
		 * Iterate over htblRequestBody to validate that the fields(key) maps to
		 * an actual property in the applicationDomain ontology and also check
		 * that no value of any of the fields (value) violates any constraints
		 * (unique constraints and data integrity constraints )
		 */
		Iterator<String> htblRequestBodyIter = htblRequestBody.keySet().iterator();
		while (htblRequestBodyIter.hasNext()) {
			String field = htblRequestBodyIter.next();

			if (isFieldMapsToProperty(subjectClass, field, notMappedFieldsList)) {

				/*
				 * get fieldValue
				 */
				Object fieldValue = htblRequestBody.get(field);

				/*
				 * get property with name = field
				 */
				Property property = subjectClass.getProperties().get(field);
				
				if(property instanceof ObjectProperty){
					
					
					
				}

			}

		}

		UpdateRequestValidationResultUtility updateRequestValidationResult = new UpdateRequestValidationResultUtility(
				validationResult, classValueList, htblUniquePropValueList);

		return updateRequestValidationResult;

	}

	/**
	 * isFieldMapsToProperty checks if a field maps to a static property ( has
	 * map in the list of properties of passed subject class) in the
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
	private boolean isFieldMapsToProperty(Class subjectClass, String field, ArrayList<String> notMappedFieldsList) {

		/*
		 * check that field maps to a property in subjectClass and return true
		 * if it maps
		 */
		if (subjectClass.getProperties().containsKey(field)) {
			return true;
		} else {

			/*
			 * field does not map to a valid property so I will add it to
			 * notMappedFieldsList to be checked again after loading dynamic
			 * properteis of subjectClass
			 */
			notMappedFieldsList.add(field);

			/*
			 * return false becuase no mapping found
			 */
			return false;
		}
	}

}
