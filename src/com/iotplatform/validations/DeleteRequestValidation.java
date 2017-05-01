package com.iotplatform.validations;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.daos.DynamicOntologyDao;
import com.iotplatform.exceptions.InvalidDeleteRequestBodyException;
import com.iotplatform.exceptions.InvalidRequestFieldsException;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.XSDDatatype;
import com.iotplatform.ontology.mapers.DynamicOntologyMapper;
import com.iotplatform.utilities.DeletePropertyValueUtility;

/**
 * DeleteRequestValidation is used to validate delete request body by :
 * 
 * 1- Checking that the deleted field mapped correctly to a valid property in
 * the application domain ontology.
 * 
 * 2- Parse delete request body to be used by DeleteQuery class to generate
 * delete query
 * 
 * @author HatemMorgan
 * 
 */

@Component
public class DeleteRequestValidation {
	private DynamicOntologyDao dynamicOntologyDao;

	@Autowired
	public DeleteRequestValidation(DynamicOntologyDao dynamicOntologyDao) {
		this.dynamicOntologyDao = dynamicOntologyDao;
	}

	/**
	 * validateDeleteRequest validates the delete request and do the above
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
	 * @return ArrayList<DeletePropertyValueUtility>
	 * 
	 *         which is a list of DeletePropertyValueUtility that is the result
	 *         of parsing requestBody and it will be used by DeleteQuery class
	 *         to generate delete query
	 */

	public ArrayList<DeletePropertyValueUtility> validateDeleteRequest(String applicationModelName,
			LinkedHashMap<String, Object> htblRequestBody, Class subjectClass) {

		/*
		 * deletePropValueList is a list of DeletePropertyValueUtility that is
		 * the result of parsing requestBody and it will be used by DeleteQuery
		 * class to generate delete query
		 */
		ArrayList<DeletePropertyValueUtility> deletePropValueList = new ArrayList<>();

		/*
		 * Hashtable of classes' name that need to get their dynamic properties
		 * to check if the fields maps to one of them or these fields are
		 * invalid fields
		 */
		Hashtable<String, String> htbNotMappedFieldsClasses = new Hashtable<>();

		/*
		 * htblNotMappedFields is a Hashtable of all requestBody fields that has
		 * not mapping to a property in the application(with applicationName)
		 * domain ontology and the values if exist.
		 * 
		 * These fields will be validated again after loading dynamicProperties
		 * of the requested class (subjectClass)
		 */
		Hashtable<String, Object> htblNotMappedFields = new Hashtable<>();

		if (htblRequestBody.containsKey("delete") && htblRequestBody.get("delete") instanceof ArrayList<?>) {

			ArrayList<Object> deleteFieldsList = (ArrayList<Object>) htblRequestBody.get("delete");

			/*
			 * iterate over the deleteFieldsList to validate and parse it
			 */

			for (Object deleteField : deleteFieldsList) {

				if (deleteField instanceof String) {

					String field = deleteField.toString();
					/*
					 * get fieldValue
					 */
					Object fieldValue = htblRequestBody.get(field);

					if (isFieldMapsToProperty(subjectClass, field, fieldValue, htblNotMappedFields,
							htbNotMappedFieldsClasses)) {

						/*
						 * get property with name = field
						 */
						Property property = subjectClass.getProperties().get(field);

						if (property.isMulitpleValues()) {
							throw new InvalidDeleteRequestBodyException("Invalid field " + "with name: " + field
									+ ". This " + "field maps to a property that has multiple"
									+ " values so you must specify which value to delete. "
									+ "See documentation for more details on how to use deleteAPI");
						} else {

							/*
							 * create a new UpdatePropertyValueUtility to hold
							 * parsed value
							 */
							DeletePropertyValueUtility deletePropertyValue = new DeletePropertyValueUtility(
									property.getPrefix().getPrefix() + property.getName());
							deletePropValueList.add(deletePropertyValue);

						}

					}
				} else {

					if (deleteField instanceof LinkedHashMap<?, ?>) {

						LinkedHashMap<String, Object> valueObject = (LinkedHashMap<String, Object>) deleteField;

						validateDeleteListObjectField(valueObject, subjectClass, deletePropValueList,
								htbNotMappedFieldsClasses, htblNotMappedFields, applicationModelName);
					} else {

						throw new InvalidDeleteRequestBodyException("The delete field list must have either "
								+ "1- A string value that maps to a valid property that has "
								+ "a single value . 2- An object value that has fieldName "
								+ "key (obligatory) where its value maps to a valid multiValued property and value key (optional) where its value maps to an existing value that needs to be deleted");
					}

				}

			}
		}
		return deletePropValueList;

	}

	/**
	 * 
	 * validateMultiValuePropertyField validate the value of delete list in
	 * requestBodyField that is instance of an LinkedHashMap
	 * 
	 * 1- it checks that this value is valid ( LinkedHashMap value contains 1-
	 * fieldName (obligatory) and value to be deleted (optional) )
	 * 
	 * 2- parse the value
	 * 
	 * @param valueObject
	 *            is LinkedHashMap<String, Object> which is the value of the
	 *            field
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
	private void validateDeleteListObjectField(LinkedHashMap<String, Object> valueObject, Class subjectClass,
			ArrayList<DeletePropertyValueUtility> deletePropValueList,
			Hashtable<String, String> htbNotMappedFieldsClasses, Hashtable<String, Object> htblNotMappedFields,
			String applicationModelName) {
		/*
		 * check that the valueObject is not empty
		 */
		if (valueObject.isEmpty())
			throw new InvalidDeleteRequestBodyException(
					"You cannot add an empty object in the delete list fieldValue in the "
							+ "request body. The object must contains 1- field(obligatory) " + "2-value(optional) ");

		/*
		 * check that the valueObject contains the obligatory field (fieldName)
		 * which represents the name that must map to a valid property
		 */
		if (!(valueObject.containsKey("fieldName") && valueObject.get("fieldName") instanceof String))
			throw new InvalidDeleteRequestBodyException(
					"The object value must contains a field with name field. fieldName "
							+ "represents a valid name for a valid property in the application semantic model (Schema)");

		/*
		 * get fieldName
		 */
		String field = valueObject.get("fieldName").toString();

		/*
		 * check that the field maps to a valid property
		 */
		if (isFieldMapsToProperty(subjectClass, field, valueObject, htblNotMappedFields, htbNotMappedFieldsClasses)) {

			Property property = subjectClass.getProperties().get(field);

			/*
			 * check if the property is not a multiValued property in order to
			 * throw error
			 */
			if (!property.isMulitpleValues())
				throw new InvalidDeleteRequestBodyException("The field fieldName with value: " + field
						+ " must maps to a property that has multiple values.");

			/*
			 * check if valueObject contains a key = value which represents that
			 * the user want to remove a specific value from multiple values of
			 * the property
			 */
			if (valueObject.containsKey("value")) {

				/*
				 * check that the value key has a valid value which must not be
				 * a list or an object
				 */
				if (!((valueObject.get("value") instanceof String) || (valueObject.get("value") instanceof Integer)
						|| (valueObject.get("value") instanceof Float) || (valueObject.get("value") instanceof Double)
						|| (valueObject.get("value") instanceof Boolean)))

					throw new InvalidDeleteRequestBodyException("Invalid value for fieldValue for fieldName: " + field
							+ ". It must be a single value not an object or a list.");

				Object valueToBeDeleted = valueObject.get("value");

				/*
				 * check if valueToBeDeleted has a valid dataType based on the
				 * property type
				 */
				if (!(property instanceof ObjectProperty && valueToBeDeleted instanceof String))
					throw new InvalidDeleteRequestBodyException("The fieldName : " + field
							+ " maps to an objectProperty where its value is a reference to another object node. So the value must be of type String ");

				if (!(property instanceof DataTypeProperty
						&& isDataValueValid((DataTypeProperty) property, valueToBeDeleted)))
					throw new InvalidDeleteRequestBodyException("The fieldName : " + field
							+ " maps to DataTypeProperty where its value must have a valid dataType which is : "
							+ ((DataTypeProperty) property).getDataType().getDataType());

				DeletePropertyValueUtility deletePropertyValue = new DeletePropertyValueUtility(
						property.getPrefix().getPrefix() + property.getName(), true, valueToBeDeleted);

				deletePropValueList.add(deletePropertyValue);

			} else {

				/*
				 * user did not specify a specific value to be deleted for a
				 * multiValued Property. So the system will delete all the
				 * values of this property
				 */
				DeletePropertyValueUtility deletePropertyValue = new DeletePropertyValueUtility(
						property.getPrefix().getPrefix() + property.getName());
				deletePropValueList.add(deletePropertyValue);
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
	private void reValidateNotMappedFields(Class subjectClass,
			ArrayList<DeletePropertyValueUtility> deletePropValueList,
			Hashtable<String, String> htbNotMappedFieldsClasses, ArrayList<String> notMappedFieldsList,
			String applicationModelName, LinkedHashMap<String, Object> htblRequestBody) {

		/*
		 * check that there is a need dynamic properties and/or classes needed
		 * to be loaded. by checking that the notMappedFieldsClassesList has
		 * classNames and notMappedFieldsList has notMappedFieldsName
		 */
		if (htbNotMappedFieldsClasses.size() > 0 && notMappedFieldsList.size() > 0) {

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

					throw new InvalidRequestFieldsException("Delete API", notMappedField);

				} else {

					/*
					 * set subjectClass pointer to cached class in
					 * dynamicOntology cache
					 * (DynamicOntologyMapper.getHtblappDynamicOntologyClasses()
					 * .get(applicationModelName))
					 */
					subjectClass = DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
							.get(subjectClass.getName().toLowerCase());

					/*
					 * get dynamic property that is the mapping of
					 * notMappedField
					 */
					Property property = DynamicOntologyMapper.getHtblappDynamicOntologyClasses()
							.get(applicationModelName).get(subjectClass.getName().toLowerCase()).getProperties()
							.get(notMappedField);

					validateDeleteRequest(applicationModelName, htblRequestBody, subjectClass);

				}

			}

			/*
			 * check if there are new not mapped fields added to reload dynamic
			 * properties and/or classes from dynamic ontology of the requested
			 * application
			 */
			if (newNotMappedFieldsList.size() > 0 && htblNewNotMappedFieldsClasses.size() > 0) {

				reValidateNotMappedFields(subjectClass, deletePropValueList, htblNewNotMappedFieldsClasses,
						newNotMappedFieldsList, applicationModelName, htblRequestBody);
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
	 * @param htblNotMappedFields
	 *            is a Hashtable that holds the not mapped fields and their
	 *            values to be checked again after loading dynamic added
	 *            properties to @param(subjectClass) in the requested
	 *            applicationDomain ontology
	 * 
	 * @param htbNotMappedFieldsClasses
	 *            Hashtable of classes' name that need to get their dynamic
	 *            properties to check if the fields maps to one of them or these
	 *            fields are invalid fields
	 * 
	 * 
	 * 
	 * @return true if there is a mapping.
	 * 
	 *         false if there is no mapping and add subject class to passed
	 *         classList in order to get dynamic properties of it and it will
	 *         add the field and value to htblNotFoundFieldValue hashtable to be
	 *         checked again after loading dynamic properties
	 */
	private boolean isFieldMapsToProperty(Class subjectClass, String field, Object value,
			Hashtable<String, Object> htblNotMappedFields, Hashtable<String, String> htbNotMappedFieldsClasses) {

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
			htblNotMappedFields.put(field, value);

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

	/*
	 * isDataValueValid checks that the datatype of the values passed with the
	 * property are valid to maintain data integrity and consistency.
	 * 
	 */
	private boolean isDataValueValid(DataTypeProperty dataProperty, Object value) {
		XSDDatatype xsdDataType = dataProperty.getDataType();
		switch (xsdDataType) {
		case boolean_type:
			if (value instanceof Boolean) {
				return true;
			} else {
				return false;
			}
		case decimal_typed:
			if (value instanceof Double) {
				return true;
			} else {
				return false;
			}
		case float_typed:
			if (value instanceof Float) {
				return true;
			} else {
				return false;
			}
		case integer_typed:
			if (value instanceof Integer) {
				return true;
			} else {
				return false;
			}
		case string_typed:
			if (value instanceof String) {
				return true;
			} else {
				return false;
			}
		case dateTime_typed:
			if (value instanceof XMLGregorianCalendar) {
				return true;
			} else {
				return false;
			}
		case double_typed:
			if (value instanceof Double) {
				return true;
			} else {
				return false;
			}
		default:
			return false;
		}

	}

}
