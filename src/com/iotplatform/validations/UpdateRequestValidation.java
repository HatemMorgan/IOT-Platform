package com.iotplatform.validations;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.daos.DynamicOntologyDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.InvalidRequestFieldsException;
import com.iotplatform.exceptions.InvalidUpdateRequestBodyException;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.MainOntology;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.mapers.DynamicOntologyMapper;
import com.iotplatform.ontology.mapers.OntologyMapper;
import com.iotplatform.utilities.UpdatePropertyValueUtility;
import com.iotplatform.utilities.UpdateRequestValidationResultUtility;
import com.iotplatform.utilities.ValueOfTypeClassUtility;

import oracle.spatial.rdf.client.jena.Oracle;

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
 * one.
 * 
 * 2- It constructs the data structures and add any values to it that need to be
 * checked for any data integrity or unique constraints violations. The check
 * will be done by InsertRequestValidation because the update request body
 * consists of two parts (part for updating existing values of an existing
 * individual and part for inserting a new patterns to an existing individual)
 * 
 * 3- It parse request body (JSON) into UpdatePropertyValueUtility instances in
 * order to perform a mapping from JSON to semantic web structure to be used by
 * UpdatngQuery class to create update query
 * 
 * 
 * UpdateRequestValidation loads the dynamicProperties here not passing it to
 * InsertRequestValidation. Because :
 * 
 * 1- It is more efficient to load it here to fully validate that the request
 * maps to the application domain ontology.
 * 
 * 2- It will keep UpdateRequestValidation class independent from
 * InsertRequestValidation because no one will be waiting for the other to
 * perform something to it . The UpdateService will handle passing
 * dataStructures results from UpdateRequestValidation to
 * InsertRequestValidation. to check for any constraints violations.
 * 
 * 3- There will be a little chance to load any dynamic properties or classes in
 * InsertRequestValidation because updateRequest will load any dynamicProperties
 * if exist for the request class which is the most common class that might need
 * to load its dynamicProperties
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

	public UpdateRequestValidationResultUtility validateUpdateRequest(String applicationModelName,
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
		 * Hashtable of classes' name that need to get their dynamic properties
		 * to check if the fields maps to one of them or these fields are
		 * invalid fields
		 */
		Hashtable<String, String> htbNotMappedFieldsClasses = new Hashtable<>();

		/*
		 * construct UpdateRequestValidationResultUtility to be returned to
		 * UpdateService
		 */
		UpdateRequestValidationResultUtility updateRequestValidationResult = new UpdateRequestValidationResultUtility(
				validationResult, classValueList, htblUniquePropValueList);

		/*
		 * Iterate over htblRequestBody to validate that the fields(key) maps to
		 * an actual property in the applicationDomain ontology and also check
		 * that no value of any of the fields (value) violates any constraints
		 * (unique constraints and data integrity constraints )
		 */
		Iterator<String> htblRequestBodyIter = htblRequestBody.keySet().iterator();
		while (htblRequestBodyIter.hasNext()) {
			String field = htblRequestBodyIter.next();

			if (isFieldMapsToProperty(subjectClass, field, notMappedFieldsList, htbNotMappedFieldsClasses)) {

				/*
				 * get fieldValue
				 */
				Object fieldValue = htblRequestBody.get(field);

				/*
				 * get property with name = field
				 */
				Property property = subjectClass.getProperties().get(field);

				validateUpdateRequestFieldValue(property, fieldValue, validationResult, classValueList,
						htblUniquePropValueList, htbNotMappedFieldsClasses, notMappedFieldsList, applicationModelName);

				/*
				 * check if the property is the uniqueIdentifier of the
				 * subjectClass
				 */
				if (subjectClass.isHasUniqueIdentifierProperty()
						&& subjectClass.getUniqueIdentifierPropertyName().equals(property.getName())) {

					/*
					 * set NewUniqueIdentifierValue to the fieldValue in order
					 * to be used by the updateDao to update the
					 * uniqueIdentifier (subject) of the individual being
					 * updated
					 */
					updateRequestValidationResult.setNewUniqueIdentifierValue(fieldValue);
				}

			}
		}

		/*
		 * call reValidateNotMappedFields to load any needed dynamic classes or
		 * properties and revalidates the values of the notMappedFields if the
		 * maps to a dynamic property
		 */
		reValidateNotMappedFields(subjectClass, validationResult, classValueList, htblUniquePropValueList,
				htbNotMappedFieldsClasses, notMappedFieldsList, applicationModelName, htblRequestBody);

		return updateRequestValidationResult;

	}

	/**
	 * validateUpdateRequestFieldValue method is used to validate the value of a
	 * UpdateRequestBodyField
	 * 
	 * @param subjectClass
	 *            subjectClass is the type of the individual that is updated. It
	 *            is used to make sure that the individual maps correctly to the
	 *            semantic model defined by ontology of this application domain
	 *            with applicationName passed.
	 * 
	 * @param property
	 *            mapped property of the udpateRequestBody field
	 * 
	 * @param fieldValue
	 *            value of the udpateRequestBody field
	 * 
	 * @param validationResult
	 *            validationResult is a list of UpdatePropertyValue that is the
	 *            result of parsing requestBody and it will be used by
	 *            UpdateQuery class to generate update query
	 * 
	 * @param classValueList
	 *            classValueList is list of ValueOfTypeClass instances (holds
	 *            objectValue and its classType). it will be used to check
	 *            dataIntegrity constraints
	 * 
	 * @param htblUniquePropValueList
	 *            uniquePropValueList is a LikedHashMap of key prefixedClassName
	 *            and value LinkedHashMap<String,ArrayList<PropertyValue>> with
	 *            key prefixedPropertyName and value list of propertyValue
	 *            object that holds the unique propertyName and value I used
	 *            LinkedHashMap to ensure that the property will not be
	 *            duplicated for the prefixedClassName (this will improve
	 *            efficiency by reducing graph patterns as there will never be
	 *            duplicated properties)
	 * 
	 *            This DataStructure instance is used in
	 *            uniqueConstraintValidation
	 * 
	 *            ex: {
	 * 
	 *            foaf:Person={foaf:userName=[HaythamIsmailss, AhmedMorganls,
	 *            HatemMorganss]},
	 * 
	 *            foaf:Agent={foaf:mbox=[haytham.ismailss@gmail.com,
	 *            haytham.ismailss@student.guc.edu.eg, ahmedmorganlss@gmail.com,
	 *            hatemmorgan17ss@gmail.com,
	 *            hatem.el-sayedss@student.guc.edu.eg]}
	 * 
	 *            }
	 * 
	 * @param htbNotMappedFieldsClasses
	 *            Hashtable of classes' name that need to get their dynamic
	 *            properties to check if the fields maps to one of them or these
	 *            fields are invalid fields
	 * 
	 * @param notMappedFieldsList
	 *            notMappedFieldsList is a list of all requestBody fields that
	 *            has not mapping to a property in the application(with
	 *            applicationName) domain ontology.
	 * 
	 *            These fields will be validated again after loading
	 *            dynamicProperties of the requested class (subjectClass)
	 * 
	 * @param applicationModelName
	 *            applicationName of the requested applicationDomain. Every
	 *            application has a unique name and it is needed to load its
	 *            dynamic added classes and properties to the mainOntology and
	 *            also to validate that no constraints violations on the
	 *            applicationModel that stores its data
	 * 
	 */
	private void validateUpdateRequestFieldValue(Property property, Object fieldValue,
			ArrayList<UpdatePropertyValueUtility> validationResult, ArrayList<ValueOfTypeClassUtility> classValueList,
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<Object>>> htblUniquePropValueList,
			Hashtable<String, String> htbNotMappedFieldsClasses, ArrayList<String> notMappedFieldsList,
			String applicationModelName) {

		if (property instanceof ObjectProperty) {

			/*
			 * if property has multiple values then the user must enter the
			 * oldValue (that will be updated) and the new value
			 * 
			 * value = {oldValue:"" , newValue:""}
			 */

			if (property.isMulitpleValues() && fieldValue instanceof java.util.LinkedHashMap<?, ?>) {
				LinkedHashMap<String, Object> valueObject = (LinkedHashMap<String, Object>) fieldValue;

				/*
				 * call validateMultiValuedPropertyField method to validate the
				 * valueObject
				 */
				validateMultiValuedObjectPropertyField(valueObject, property, fieldValue, validationResult,
						classValueList, htblUniquePropValueList, htbNotMappedFieldsClasses, notMappedFieldsList,
						applicationModelName);

			} else {

				/*
				 * property is not multiValued so the expected fieldValue is to
				 * be string which represents the new value for this property
				 */
				validateSingleValuedObjectPropertyField(fieldValue, property, fieldValue, validationResult,
						classValueList, htblUniquePropValueList, htbNotMappedFieldsClasses, notMappedFieldsList,
						applicationModelName);

			}

		} else {

			/*
			 * DataTypeProperty
			 */

			/*
			 * if property has multiple values then the user must enter the
			 * oldValue (that will be updated) and the new value
			 * 
			 * value = {oldValue:"" , newValue:""}
			 */

			if (property.isMulitpleValues() && fieldValue instanceof java.util.LinkedHashMap<?, ?>) {
				LinkedHashMap<String, Object> valueObject = (LinkedHashMap<String, Object>) fieldValue;

				/*
				 * call validateMultiValuedPropertyField method to validate the
				 * valueObject
				 */
				validateMultiValuedDataTypePropertyField(valueObject, property, fieldValue, validationResult,
						classValueList, htblUniquePropValueList, applicationModelName);

			} else {

				/*
				 * property is not multiValued so the expected fieldValue is to
				 * be string which represents the new value for this property
				 */
				validateSingleValuedDataTypePropertyField(fieldValue, property, fieldValue, validationResult,
						classValueList, htblUniquePropValueList, applicationModelName);

			}

		}
	}

	/**
	 * reValidateNotMappedFields revalidates all the not mapped fields after
	 * loading dynamic properties and classes need from dynamic ontology of the
	 * requested application domain
	 * 
	 * The Parameters are the same as validateUpdateRequestFieldValue method
	 * 
	 * 
	 */
	private void reValidateNotMappedFields(Class subjectClass, ArrayList<UpdatePropertyValueUtility> validationResult,
			ArrayList<ValueOfTypeClassUtility> classValueList,
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<Object>>> htblUniquePropValueList,
			Hashtable<String, String> htbNotMappedFieldsClasses, ArrayList<String> notMappedFieldsList,
			String applicationModelName, LinkedHashMap<String, Object> htblRequestBody) {

		/*
		 * check that there is a need dynamic properties and/or classes needed
		 * to be loaded. by checking that the notMappedFieldsClassesList has
		 * classNames and notMappedFieldsList has notMappedFieldsName
		 */
		if (htbNotMappedFieldsClasses.size() > 0 && notMappedFieldsList.size() > 0) {
			System.out.println("loading dynamic properties from database");
			/*
			 * load dynamic properties and/or classes
			 */
			dynamicOntologyDao.loadAndCacheDynamicClassesofApplicationDomain(applicationModelName,
					htbNotMappedFieldsClasses);

			/*
			 * construct new data structures to hold new unmapped fields
			 */
			Hashtable<String, String> htblNewNotMappedFieldsClasses = new Hashtable<>();
			ArrayList<String> newNotMappedFieldsList = new ArrayList<>();

			/*
			 * iterating over notMappedFieldsList
			 */
			for (String notMappedField : notMappedFieldsList) {

				/*
				 * After loading dynamic Properties, I am caching all the loaded
				 * properties so If field does not mapped to one of the
				 * properties(contains static ones and cached dynamic ones) of
				 * the subjectClass , I will throw InvalidRequestFieldsException
				 * to indicate the field is invalid
				 */
				if (!(DynamicOntologyMapper.getHtblappDynamicOntologyClasses().containsKey(applicationModelName)
						&& DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
								.containsKey(subjectClass.getName().toLowerCase())
						&& DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
								.get(subjectClass.getName().toLowerCase()).getProperties()
								.containsKey(notMappedField))) {

					throw new InvalidRequestFieldsException(subjectClass.getName(), notMappedField);
				} else {

					/*
					 * get dynamic property that is the mapping of
					 * notMappedField
					 */
					Property property = DynamicOntologyMapper.getHtblappDynamicOntologyClasses()
							.get(applicationModelName).get(subjectClass.getName().toLowerCase()).getProperties()
							.get(notMappedField);

					/*
					 * call validateUpdateRequestFieldValue to validate the
					 * value of notMappedField
					 */
					validateUpdateRequestFieldValue(property, htblRequestBody.get(notMappedField), validationResult,
							classValueList, htblUniquePropValueList, htblNewNotMappedFieldsClasses,
							newNotMappedFieldsList, applicationModelName);

				}

			}

			/*
			 * check if there are new not mapped fields added to reload dynamic
			 * properties and/or classes from dynamic ontology of the requested
			 * application
			 */
			if (newNotMappedFieldsList.size() > 0 && htblNewNotMappedFieldsClasses.size() > 0) {

				reValidateNotMappedFields(subjectClass, validationResult, classValueList, htblUniquePropValueList,
						htblNewNotMappedFieldsClasses, newNotMappedFieldsList, applicationModelName, htblRequestBody);
			}
		}

	}

	/**
	 * validateSingleValuedObjectPropertyField validate the value of
	 * requestBodyField that maps to an ObjectProperty that has single value
	 * 
	 * @param value
	 *            the value of the field that maps to an objectProperty with
	 *            singleValue
	 * 
	 * @param subjectClass
	 * @param property
	 * @param fieldValue
	 * @param validationResult
	 * @param classValueList
	 * @param htblUniquePropValueList
	 * @param notMappedFieldsClassesList
	 * @param notMappedFieldsList
	 * @param applicationModelName
	 */
	private void validateSingleValuedObjectPropertyField(Object value, Property property, Object fieldValue,
			ArrayList<UpdatePropertyValueUtility> validationResult, ArrayList<ValueOfTypeClassUtility> classValueList,
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<Object>>> htblUniquePropValueList,
			Hashtable<String, String> htbNotMappedFieldsClasses, ArrayList<String> notMappedFieldsList,
			String applicationModelName) {

		if (property.isMulitpleValues()) {
			throw new InvalidUpdateRequestBodyException(
					" Field with name: " + property.getName() + " has invalid value format because this field maps "
							+ "to a multiValued Property. So to update its value, "
							+ "you must supply the oldValue that will be updated " + "and the new value.");
		}

		if (!((value instanceof String) || (value instanceof Integer) || (value instanceof Float)
				|| (value instanceof Double) || (value instanceof Boolean)))
			throw new InvalidUpdateRequestBodyException("Field with name: " + property.getName()
					+ " has invalid value format because its value must be a string holding "
					+ "the new value that reference an existing object ");

		/*
		 * check that the valueObject is not empty
		 */
		if (value.toString().isEmpty() || value.toString().replaceAll(" ", "").isEmpty()) {

			throw new InvalidUpdateRequestBodyException(" Field with name: " + property.getName()
					+ " has invalid value format because its value must not be empty .");

		}

		/*
		 * I must add the new value to classValueList to be checked that it does
		 * not violate any dataIntegrity constraints (the new value ( which is
		 * an object value reference) is a valid exist object in the model of
		 * the requested application )
		 */
		Class objectPropertyRangeClass = getPropertyObject(property, htbNotMappedFieldsClasses, notMappedFieldsList,
				fieldValue, applicationModelName);

		/*
		 * check that notMappedFieldsList is not null (it will be null if the
		 * object property range class is a dynamic added class to the request
		 * application ontology)
		 * 
		 * if it is not null then I have to add it and the newValue to
		 * classValueList to be checked for any data integrity constraints
		 */
		if (objectPropertyRangeClass != null)
			classValueList.add(new ValueOfTypeClassUtility(objectPropertyRangeClass, value));

		/*
		 * check if the property is unique so the value must be added to
		 * htblUniquePropValueList to be checked for any unique constraint
		 * violation (which checks that the new value is unique)
		 */
		checkIfPropertyIsUnique(property, htblUniquePropValueList, value);

		/*
		 * create a new UpdatePropertyValueUtility to hold parsed value
		 */
		UpdatePropertyValueUtility updatePropertyValueUtility = new UpdatePropertyValueUtility(
				property.getPrefix().getPrefix() + property.getName(), false, null, value);

		/*
		 * add the new UpdatePropertyValueUtility instance to validationResult
		 */
		validationResult.add(updatePropertyValueUtility);

	}

	/**
	 * 
	 * validateMultiValuedObjectPropertyField validate the value of
	 * requestBodyField that maps to an ObjectProperty that has multiple values
	 * 
	 * @param valueObject
	 *            is LinkedHashMap<String, Object> which is the value of the
	 *            field that maps to an objectProperty with multipleValues
	 * 
	 * @param subjectClass
	 * @param property
	 * @param fieldValue
	 * @param validationResult
	 * @param classValueList
	 * @param htblUniquePropValueList
	 * @param notMappedFieldsClassesList
	 * @param notMappedFieldsList
	 * @param applicationModelName
	 */
	private void validateMultiValuedObjectPropertyField(LinkedHashMap<String, Object> valueObject, Property property,
			Object fieldValue, ArrayList<UpdatePropertyValueUtility> validationResult,
			ArrayList<ValueOfTypeClassUtility> classValueList,
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<Object>>> htblUniquePropValueList,
			Hashtable<String, String> htbNotMappedFieldsClasses, ArrayList<String> notMappedFieldsList,
			String applicationModelName) {
		/*
		 * check that the valueObject is not empty
		 */
		if (valueObject.isEmpty()) {

			throw new InvalidUpdateRequestBodyException(" Field with name: " + property.getName()
					+ " has invalid value format because its value must not be an empty object .");

		}

		/*
		 * check that the valueObject contains the appropriate fields (oldValue
		 * and newValue)
		 */
		if (!(valueObject.size() == 2 && valueObject.containsKey("oldValue")
				&& ((valueObject.get("oldValue") instanceof String) || (valueObject.get("oldValue") instanceof Integer)
						|| (valueObject.get("oldValue") instanceof Float)
						|| (valueObject.get("oldValue") instanceof Double)
						|| (valueObject.get("oldValue") instanceof Boolean))
				&& valueObject.containsKey("newValue")
				&& ((valueObject.get("newValue") instanceof String) || (valueObject.get("newValue") instanceof Integer)
						|| (valueObject.get("newValue") instanceof Float)
						|| (valueObject.get("newValue") instanceof Double)
						|| (valueObject.get("newValue") instanceof Boolean)))) {

			throw new InvalidUpdateRequestBodyException(
					" Field with name: " + property.getName() + " has invalid value format because this field maps "
							+ "to a multiValued Property. So to update its value, "
							+ "you must supply the oldValue that will be updated " + "and the new value.");

		}

		/*
		 * valueObject is valid so get oldValue and newValue fieldsValues
		 */
		Object oldValue = valueObject.get("oldValue");
		Object newValue = valueObject.get("newValue");

		/*
		 * I must add the new value to classValueList to be checked that it does
		 * not violate any dataIntegrity constraints (the new value ( which is
		 * an object value reference) is a valid exist object in the model of
		 * the requested application )
		 */
		Class objectPropertyRangeClass = getPropertyObject(property, htbNotMappedFieldsClasses, notMappedFieldsList,
				fieldValue, applicationModelName);

		/*
		 * check that notMappedFieldsList is not null (it will be null if the
		 * object property range class is a dynamic added class to the request
		 * application ontology)
		 * 
		 * if it is not null then I have to add it and the newValue to
		 * classValueList to be checked for any data integrity constraints
		 */
		if (objectPropertyRangeClass != null)
			classValueList.add(new ValueOfTypeClassUtility(objectPropertyRangeClass, newValue));

		/*
		 * check if the property is unique so the value must be added to
		 * htblUniquePropValueList to be checked for any unique constraint
		 * violation (which checks that the new value is unique)
		 */
		checkIfPropertyIsUnique(property, htblUniquePropValueList, newValue);

		/*
		 * create a new UpdatePropertyValueUtility to hold parsed value
		 */
		UpdatePropertyValueUtility updatePropertyValueUtility = new UpdatePropertyValueUtility(
				property.getPrefix().getPrefix() + property.getName(), true, oldValue, newValue);

		/*
		 * add the new UpdatePropertyValueUtility instance to validationResult
		 */
		validationResult.add(updatePropertyValueUtility);
	}

	/**
	 * validateSingleValuedDataTypePropertyField validate the value of
	 * requestBodyField that maps to an DatatypeProperty that has single value
	 * 
	 * @param value
	 *            the value of the field that maps to an DatatypeProperty with
	 *            singleValue
	 * 
	 * @param subjectClass
	 * @param property
	 * @param fieldValue
	 * @param validationResult
	 * @param classValueList
	 * @param htblUniquePropValueList
	 * @param notMappedFieldsClassesList
	 * @param notMappedFieldsList
	 * @param applicationModelName
	 */
	private void validateSingleValuedDataTypePropertyField(Object value, Property property, Object fieldValue,
			ArrayList<UpdatePropertyValueUtility> validationResult, ArrayList<ValueOfTypeClassUtility> classValueList,
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<Object>>> htblUniquePropValueList,

			String applicationModelName) {

		if (property.isMulitpleValues()) {
			throw new InvalidUpdateRequestBodyException(
					" Field with name: " + property.getName() + " has invalid value format because this field maps "
							+ "to a multiValued Property. So to update its value, "
							+ "you must supply the oldValue that will be updated " + "and the new value.");
		}

		if (!((value instanceof String) || (value instanceof Integer) || (value instanceof Float)
				|| (value instanceof Double) || (value instanceof Boolean)))
			throw new InvalidUpdateRequestBodyException("Field with name: " + property.getName()
					+ " has invalid value format because its value must be the propertyDataType value holding "
					+ "the new value that reference an existing object ");

		/*
		 * check that the valueObject is not empty
		 */
		if (value.toString().isEmpty() || value.toString().replaceAll(" ", "").isEmpty()) {

			throw new InvalidUpdateRequestBodyException(" Field with name: " + property.getName()
					+ " has invalid value format because its value must not be empty .");

		}

		/*
		 * check if the property is unique so the value must be added to
		 * htblUniquePropValueList to be checked for any unique constraint
		 * violation (which checks that the new value is unique)
		 */
		checkIfPropertyIsUnique(property, htblUniquePropValueList, value);

		/*
		 * create a new UpdatePropertyValueUtility to hold parsed value
		 */
		UpdatePropertyValueUtility updatePropertyValueUtility = new UpdatePropertyValueUtility(
				property.getPrefix().getPrefix() + property.getName(), false, null, value);

		/*
		 * add the new UpdatePropertyValueUtility instance to validationResult
		 */
		validationResult.add(updatePropertyValueUtility);

	}

	/**
	 * 
	 * validateMultiValuedDataTypePropertyField validate the value of
	 * requestBodyField that maps to a DataTypeProperty that has multiple values
	 * 
	 * @param valueObject
	 *            is LinkedHashMap<String, Object> which is the value of the
	 *            field that maps to an DataTypeProperty with multipleValues
	 * 
	 * @param subjectClass
	 * @param property
	 * @param fieldValue
	 * @param validationResult
	 * @param classValueList
	 * @param htblUniquePropValueList
	 * @param notMappedFieldsClassesList
	 * @param notMappedFieldsList
	 * @param applicationModelName
	 */
	private void validateMultiValuedDataTypePropertyField(LinkedHashMap<String, Object> valueObject, Property property,
			Object fieldValue, ArrayList<UpdatePropertyValueUtility> validationResult,
			ArrayList<ValueOfTypeClassUtility> classValueList,
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<Object>>> htblUniquePropValueList,
			String applicationModelName) {
		/*
		 * check that the valueObject is not empty
		 */
		if (valueObject.isEmpty()) {

			throw new InvalidUpdateRequestBodyException(" Field with name: " + property.getName()
					+ " has invalid value format because its value must not be an empty object .");

		}

		/*
		 * check that the valueObject contains the appropriate fields (oldValue
		 * and newValue)
		 */
		if (!(valueObject.size() == 2 && valueObject.containsKey("oldValue")
				&& ((valueObject.get("oldValue") instanceof String) || (valueObject.get("oldValue") instanceof Integer)
						|| (valueObject.get("oldValue") instanceof Float)
						|| (valueObject.get("oldValue") instanceof Double)
						|| (valueObject.get("oldValue") instanceof Boolean))
				&& valueObject.containsKey("newValue")
				&& ((valueObject.get("newValue") instanceof String) || (valueObject.get("newValue") instanceof Integer)
						|| (valueObject.get("newValue") instanceof Float)
						|| (valueObject.get("newValue") instanceof Double)
						|| (valueObject.get("newValue") instanceof Boolean)))) {

			throw new InvalidUpdateRequestBodyException(
					" Field with name: " + property.getName() + " has invalid value format because this field maps "
							+ "to a multiValued Property. So to update its value, "
							+ "you must supply the oldValue that will be updated " + "and the new value.");

		}

		/*
		 * valueObject is valid so get oldValue and newValue fieldsValues
		 */
		Object oldValue = valueObject.get("oldValue");
		Object newValue = valueObject.get("newValue");

		/*
		 * check if the property is unique so the value must be added to
		 * htblUniquePropValueList to be checked for any unique constraint
		 * violation (which checks that the new value is unique)
		 */
		checkIfPropertyIsUnique(property, htblUniquePropValueList, newValue);

		/*
		 * create a new UpdatePropertyValueUtility to hold parsed value
		 */
		UpdatePropertyValueUtility updatePropertyValueUtility = new UpdatePropertyValueUtility(
				property.getPrefix().getPrefix() + property.getName(), true, oldValue, newValue);

		/*
		 * add the new UpdatePropertyValueUtility instance to validationResult
		 */
		validationResult.add(updatePropertyValueUtility);
	}

	/**
	 * getPropertyObject is used to get the objectPropetyRange class type and if
	 * the class does not exist in the mainOntology or in the dynamicOntology
	 * cache of the requested application so it will added to
	 * notMappedFieldsClassesList to load it if it exist in the dynamicOntology
	 * graph of the requested application
	 * 
	 * @param property
	 * @param subjectClass
	 * @param notMappedFieldsClassesList
	 * @param notMappedFieldList
	 * @param fieldValue
	 * @param applicationModelName
	 * @return
	 */
	private Class getPropertyObject(Property property, Hashtable<String, String> htbNotMappedFieldsClasses,
			ArrayList<String> notMappedFieldList, Object fieldValue, String applicationModelName) {
		/*
		 * get property range objectClass if it is an objectProperty
		 */
		Class objectClass = null;

		String objectClassName = ((ObjectProperty) property).getObjectClassName();

		/*
		 * get objectClass from dynamicOntology cache of the requested
		 * application if it (objectClass) exist
		 */
		if ((DynamicOntologyMapper.getHtblappDynamicOntologyClasses().contains(applicationModelName)
				&& DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
						.containsKey(objectClassName.toLowerCase()))) {
			objectClass = DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
					.get(objectClassName.toLowerCase());
		} else {

			/*
			 * get objectClass from mainOntology if it exist
			 */
			if (OntologyMapper.getHtblMainOntologyClassesMappers().containsKey(objectClassName.toLowerCase())) {

				/*
				 * get the objectClass from MainOntologyClassesMapper
				 */
				objectClass = OntologyMapper.getHtblMainOntologyClassesMappers().get(objectClassName.toLowerCase());
			} else {

				htbNotMappedFieldsClasses.put(objectClassName, objectClassName);

				notMappedFieldList.add(property.getName());

			}

		}

		return objectClass;
	}

	/**
	 * checkIfPropertyIsUnique checks if the property is Unique and if it is
	 * unique add it to htblUniquePropValueList to be checked for any unique
	 * constraints violation
	 * 
	 * @param property
	 *            mapped property of the udpateRequestBody field
	 * 
	 * @param htblUniquePropValueList
	 *            uniquePropValueList is a LikedHashMap of key prefixedClassName
	 *            and value LinkedHashMap<String,ArrayList<PropertyValue>> with
	 *            key prefixedPropertyName and value list of propertyValue
	 *            object that holds the unique propertyName and value I used
	 *            LinkedHashMap to ensure that the property will not be
	 *            duplicated for the prefixedClassName (this will improve
	 *            efficiency by reducing graph patterns as there will never be
	 *            duplicated properties)
	 * 
	 *            This DataStructure instance is used in
	 *            uniqueConstraintValidation
	 * 
	 *            ex: {
	 * 
	 *            foaf:Person={foaf:userName=[HaythamIsmailss, AhmedMorganls,
	 *            HatemMorganss]},
	 * 
	 *            foaf:Agent={foaf:mbox=[haytham.ismailss@gmail.com,
	 *            haytham.ismailss@student.guc.edu.eg, ahmedmorganlss@gmail.com,
	 *            hatemmorgan17ss@gmail.com,
	 *            hatem.el-sayedss@student.guc.edu.eg]}
	 * 
	 *            }
	 * 
	 * @param value
	 *            value of the udpateRequestBody field
	 */
	private void checkIfPropertyIsUnique(Property property,
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<Object>>> htblUniquePropValueList, Object value) {
		/*
		 * check if the property has unique constraint to add the value and the
		 * property to htblUniquePropValueList to be passed to validationDao to
		 * check the no unique constraint violation occured
		 */
		if (property.isUnique()) {

			String prefixedPropertySubjectClassName = property.getSubjectClass().getPrefix().getPrefix()
					+ property.getSubjectClass().getName();

			if (htblUniquePropValueList.containsKey(prefixedPropertySubjectClassName)) {

				/*
				 * check if property of the prefixedPropertySubjectClassName was
				 * added before
				 */
				if (htblUniquePropValueList.get(prefixedPropertySubjectClassName)
						.containsKey(property.getPrefix().getPrefix() + property.getName())) {
					/*
					 * add propertyValue instance to uniquePropertyValueList of
					 * subjectClassInstance
					 */
					htblUniquePropValueList.get(prefixedPropertySubjectClassName)
							.get(property.getPrefix().getPrefix() + property.getName()).add(value);
				} else {
					/*
					 * add prefixedPropertyName and a list to hold propertyValue
					 * objects
					 */

					ArrayList<Object> propertyValueList = new ArrayList<>();
					htblUniquePropValueList.get(prefixedPropertySubjectClassName)
							.put(property.getPrefix().getPrefix() + property.getName(), propertyValueList);

					/*
					 * add propertyValue instance to uniquePropertyValueList of
					 * subjectClassInstance
					 */
					htblUniquePropValueList.get(prefixedPropertySubjectClassName)
							.get(property.getPrefix().getPrefix() + property.getName()).add(value);
				}

			} else {

				/*
				 * Add subjectClass key and new
				 * htblClassInstanceUnqiuePropValueList to
				 * htblClassInstanceUnqiuePropValueList
				 */
				ArrayList<Object> propertyValueList = new ArrayList<>();
				LinkedHashMap<String, ArrayList<Object>> htblClassInstanceUnqiuePropValueList = new LinkedHashMap<>();
				htblClassInstanceUnqiuePropValueList.put(property.getPrefix().getPrefix() + property.getName(),
						propertyValueList);
				htblUniquePropValueList.put(prefixedPropertySubjectClassName, htblClassInstanceUnqiuePropValueList);

				/*
				 * add propertyValue instance to uniquePropertyValueList of
				 * subjectClassInstance
				 */
				htblUniquePropValueList.get(prefixedPropertySubjectClassName)
						.get(property.getPrefix().getPrefix() + property.getName()).add(value);
			}

		}
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
	 * @param field
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
	 * @param htbNotMappedFieldsClasses
	 *            Hashtable of classes' name that need to get their dynamic
	 *            properties to check if the fields maps to one of them or these
	 *            fields are invalid fields
	 * 
	 * @return true if there is a mapping.
	 * 
	 *         false if there is no mapping and add subject class to passed
	 *         classList in order to get dynamic properties of it and it will
	 *         add the field and value to htblNotFoundFieldValue hashtable to be
	 *         checked again after loading dynamic properties
	 */
	private boolean isFieldMapsToProperty(Class subjectClass, String field, ArrayList<String> notMappedFieldsList,
			Hashtable<String, String> htbNotMappedFieldsClasses) {

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
			 * add className to htbNotMappedFieldsClasses
			 */
			htbNotMappedFieldsClasses.put(subjectClass.getName(), subjectClass.getName());

			/*
			 * add superClasses names to htbNotMappedFieldsClasses in order to
			 * load any inherited properties
			 */
			for (Class superClass : subjectClass.getSuperClassesList()) {
				htbNotMappedFieldsClasses.put(superClass.getName(), superClass.getName());

			}

			/*
			 * return false becuase no mapping found
			 */
			return false;
		}
	}

	public static void main(String[] args) {

		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		String szUser = "rdfusr";
		String szPasswd = "rdfusr";

		Oracle oracle = new Oracle(szJdbcURL, szUser, szPasswd);

		System.out.println("Database connected");

		DynamicOntologyDao dynamicOntologyDao = new DynamicOntologyDao(oracle);

		UpdateRequestValidation updateRequestValidation = new UpdateRequestValidation(dynamicOntologyDao);

		LinkedHashMap<String, Object> htblRequestBody = new LinkedHashMap<>();
		// htblRequestBody.put("firstName", "mohamed");

		LinkedHashMap<String, Object> htbUserName = new LinkedHashMap<>();
		htbUserName.put("oldValue", "HatemELsayed");
		htbUserName.put("newValue", "HatemMorgan");

		htblRequestBody.put("userName", htbUserName);

		LinkedHashMap<String, Object> htbMbox = new LinkedHashMap<>();
		htbMbox.put("oldValue", "hatemmorgan17@gmail.com");
		htbMbox.put("newValue", "hatemmorgan@yahoo.com");

		htblRequestBody.put("mbox", htbMbox);

		LinkedHashMap<String, Object> htblhates = new LinkedHashMap<>();
		htblhates.put("oldValue", "MariamMazen");
		htblhates.put("newValue", "AhmedMorgan");

		htblRequestBody.put("hates", htblhates);

		htblRequestBody.put("job", "Computer Engineer");

		try {
			UpdateRequestValidationResultUtility res = updateRequestValidation.validateUpdateRequest(
					"TESTAPPLICATION_MODEL", htblRequestBody,
					OntologyMapper.getOntologyMapper().getHtblMainOntologyClassesMappers().get("developer"));

			System.out.println(res);
		} catch (ErrorObjException e) {
			System.out.println(e.getExceptionMessage());
		}

	}

}
