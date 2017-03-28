package com.iotplatform.validations;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.UUID;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.stereotype.Component;

import com.iotplatform.daos.DynamicConceptDao;
import com.iotplatform.daos.MainDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.InvalidQueryRequestBodyFormatException;
import com.iotplatform.exceptions.InvalidRequestFieldsException;
import com.iotplatform.exceptions.InvalidTypeValidationException;
import com.iotplatform.models.DynamicConceptModel;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.classes.Admin;
import com.iotplatform.utilities.DynamicPropertiesUtility;
import com.iotplatform.utilities.NotMappedDynamicQueryFields;
import com.iotplatform.utilities.QueryField;

@Component
public class GetQueryRequestValidations {

	private DynamicPropertiesUtility dynamicPropertiesUtility;

	public GetQueryRequestValidations(DynamicPropertiesUtility dynamicPropertiesUtility) {
		this.dynamicPropertiesUtility = dynamicPropertiesUtility;
	}

	public LinkedHashMap<String, LinkedHashMap<String, ArrayList<QueryField>>> validateRequest(String applicationName,
			Hashtable<String, Object> htblFieldValue, Class subjectClass) {

		/*
		 * List of classes that need to get their dynamic properties to check if
		 * the fields maps to one of them or these fields are invalid fields
		 */
		Hashtable<String, Class> htblNotMappedFieldsClasses = new Hashtable<>();

		/*
		 * list of NotMappedDynamicQueryFields which holds the unmapped fields
		 * to be checked again after loading dynamicProperties
		 */
		ArrayList<NotMappedDynamicQueryFields> notMappedFieldList = new ArrayList<>();

		/*
		 * htblClassNameProperty holds the constructed QueryField
		 */
		LinkedHashMap<String, LinkedHashMap<String, ArrayList<QueryField>>> htblClassNameProperty = new LinkedHashMap<>();

		/*
		 * get projection fields from htblFieldValue
		 */
		if (htblFieldValue.containsKey("fields")) {

			/*
			 * generate uniqueIdntifier
			 */
			String randomUUID = UUID.randomUUID().toString();

			String prefixedClassName = subjectClass.getPrefix().getPrefix() + subjectClass.getName();

			ArrayList<QueryField> queryFieldsList = new ArrayList<>();

			LinkedHashMap<String, ArrayList<QueryField>> htblUniqueIdentierQueryFieldsList = new LinkedHashMap<>();
			htblUniqueIdentierQueryFieldsList.put(randomUUID, queryFieldsList);
			htblClassNameProperty.put(prefixedClassName, htblUniqueIdentierQueryFieldsList);

			validateProjectionFields(subjectClass, htblNotMappedFieldsClasses, notMappedFieldList,
					htblFieldValue.get("fields"), htblClassNameProperty, randomUUID, subjectClass.getName());

			if (htblNotMappedFieldsClasses.size() > 0) {
				validateNotMappedFieldsValues(applicationName, subjectClass, htblNotMappedFieldsClasses,
						notMappedFieldList, htblClassNameProperty, subjectClass.getName());
			}

		}
		return htblClassNameProperty;
	}

	/*
	 * validateProjectionFields is a recursive method that is used to validated
	 * the passed projection fields dynamically
	 * 
	 * It takes list of fields (field may be a single field name or an object
	 * that express an objectField and the projected fields of the value of the
	 * objectField)
	 * 
	 * subjectClass that is the passed request class name
	 * 
	 * htblNotMappedFieldsClasses reference which is passed to
	 * isFieldMapsToStaticProperty method to add class of the notMapped fields
	 * that will be passed to dynamicPropertiesUtility class to load dynamic
	 * property
	 * 
	 * eg. {"fields":["userName","firstName",{"fieldName":"knows","fields":[
	 * "lastName","age"]}]}
	 * 
	 * notMappedFieldList that holds NotMappedDynamicQueryFields to be checked
	 * again after loading dynamicProperties
	 * 
	 * htblClassNameProperty holds prefixedClassName as a key and list of
	 * QueryFields
	 * 
	 */
	private void validateProjectionFields(Class subjectClass, Hashtable<String, Class> htblNotMappedFieldsClasses,
			ArrayList<NotMappedDynamicQueryFields> notMappedFieldList, Object field,
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<QueryField>>> htblClassNameProperty,
			String uniqueIdentifier, String requestClassName) {

		/*
		 * String field which represents that the fieldValue will be the direct
		 * value associated to it (literal value)
		 */
		if (field instanceof String) {
			/*
			 * check if the field has a property mapping
			 */
			if (isFieldMapsToStaticProperty(subjectClass, field.toString(), null, uniqueIdentifier,
					htblNotMappedFieldsClasses, notMappedFieldList)) {

				/*
				 * get prefixedPropertyName
				 */
				Property fieldMappedProperty = subjectClass.getProperties().get(field.toString());
				String prefixedPropertyName = fieldMappedProperty.getPrefix().getPrefix()
						+ fieldMappedProperty.getName();

				/*
				 * Create queryField object that take prefixedPropertyName and
				 * null for objectValueTypeClassName because the field has
				 * literal value (not an object)
				 */
				QueryField queryField = new QueryField(prefixedPropertyName, null, null);

				/*
				 * add constructed query field to htblClassNameProperty
				 */
				String prefixedClassName = subjectClass.getPrefix().getPrefix() + subjectClass.getName();
				htblClassNameProperty.get(prefixedClassName).get(uniqueIdentifier).add(queryField);

			}
		} else {

			/*
			 * field is an (LinkedHashMap<String, Object>) so it has a value
			 * which is an object so it will need further checking by
			 * recursively calling validateProjectionFields
			 */
			if (field instanceof java.util.LinkedHashMap<?, ?>) {
				LinkedHashMap<String, Object> htblfieldObject = (LinkedHashMap<String, Object>) field;
				Class objectValueClassType = null;
				/*
				 * generate uniqueIdntifier
				 */
				String randomUUID = UUID.randomUUID().toString();

				/*
				 * check if htblfieldObject contains fieldName key
				 */
				if (htblfieldObject.containsKey("fieldName")) {
					String fieldName = htblfieldObject.get("fieldName").toString();

					if (isFieldMapsToStaticProperty(subjectClass, fieldName, field, uniqueIdentifier,
							htblNotMappedFieldsClasses, notMappedFieldList)) {

						Property property = subjectClass.getProperties().get(fieldName);
						if (property instanceof ObjectProperty) {

							/*
							 * check if the user wants a specific classType not
							 * the objectValueClassType of mapped property so
							 * he/she will add key classType with a value
							 * representing the classType (eg. he may need a
							 * subClass type where the propertyObjectClassType
							 * is the superClass) eg. knows property has
							 * propertyObjectClassType foaf:Person and the user
							 * want to have the classType foaf:developer
							 */

							if (htblfieldObject.containsKey("classType")) {
								objectValueClassType = ((ObjectProperty) property).getObject();

								/*
								 * check if the passed type is a valid
								 * type(subClasss) of objectValueClassType
								 */
								if (objectValueClassType.getClassTypesList()
										.containsKey(htblfieldObject.get("classType"))) {
									objectValueClassType = objectValueClassType.getClassTypesList()
											.get(htblfieldObject.get("classType"));

								} else {
									/*
									 * invalid Type so throw an exception
									 */

									throw new InvalidTypeValidationException(requestClassName,
											objectValueClassType.getClassTypesList().keySet(),
											objectValueClassType.getName());
								}

							} else {

								/*
								 * get objectValue class and its prefixed class
								 * name
								 */
								objectValueClassType = ((ObjectProperty) property).getObject();

							}

							/*
							 * Create queryField object that take
							 * prefixedPropertyName and
							 * objectValueTypeClassPrefixedName for
							 * objectValueTypeClassName because the field has
							 * object value (not an literal)
							 */
							String objectValueTypeClassPrefixedName = objectValueClassType.getPrefix().getPrefix()
									+ objectValueClassType.getName();
							QueryField queryField = new QueryField(
									property.getPrefix().getPrefix() + property.getName(),
									objectValueTypeClassPrefixedName, randomUUID);

							/*
							 * add objectProperty to subjectClass individual
							 * with uniqueIdentifier
							 */
							String prefixedClassName = subjectClass.getPrefix().getPrefix() + subjectClass.getName();
							htblClassNameProperty.get(prefixedClassName).get(uniqueIdentifier).add(queryField);

							/*
							 * add new Object with generated
							 * uniqueIdentifier(randomUUID) and
							 * objectValueTypeClassPrefixedName as the
							 * prefixedName of this objectType
							 */
							if (htblClassNameProperty.containsKey(objectValueTypeClassPrefixedName)) {

								ArrayList<QueryField> queryFieldsList = new ArrayList<>();
								htblClassNameProperty.get(objectValueTypeClassPrefixedName).put(randomUUID,
										queryFieldsList);

							} else {

								/*
								 * new subjectClass not exist in
								 * htblClassNameProperty
								 */

								ArrayList<QueryField> queryFieldsList = new ArrayList<>();

								LinkedHashMap<String, ArrayList<QueryField>> htblUniqueIdentierQueryFieldsList = new LinkedHashMap<>();
								htblUniqueIdentierQueryFieldsList.put(randomUUID, queryFieldsList);
								htblClassNameProperty.put(objectValueTypeClassPrefixedName,
										htblUniqueIdentierQueryFieldsList);

							}

							/*
							 * check if the object has fields key that must have
							 * list value
							 */
							if (htblfieldObject.containsKey("fields")
									&& htblfieldObject.get("fields") instanceof java.util.ArrayList) {

								/*
								 * get fieldList and recursively call
								 * validateProjectionFields to validate the
								 * fieldsList
								 */
								ArrayList<Object> objectFieldList = (ArrayList<Object>) htblfieldObject.get("fields");

								validateProjectionFields(objectValueClassType, htblNotMappedFieldsClasses,
										notMappedFieldList, objectFieldList, htblClassNameProperty, randomUUID,
										requestClassName);
							} else {

								/*
								 * fields key was not available so the request
								 * body has invalid format so I will throw an
								 * exception
								 */
								throw new InvalidQueryRequestBodyFormatException(
										"fields key not found. fields key is used to "
												+ "express the projected fields for the object field's value."
												+ " It must be a list of one or more fields ");
							}

						} else {
							/*
							 * DataTypeProperty which is not valid because its
							 * value is not object so throw an exception
							 */

							throw new InvalidQueryRequestBodyFormatException("Invalid fieldName: " + fieldName
									+ " . fieldName key has to be a valid fieldName that has an object value. "
									+ "Check documentation to know which fields are valid for your application"
									+ " schema");
						}
					}
				} else {

					/*
					 * htblfieldObject has no type field so the request body has
					 * invalid format so I will throw exception
					 */
					throw new InvalidQueryRequestBodyFormatException(
							"fieldName key not found. fieldName key is used to " + "know to field with object value");
				}

			}

			else {

				if (field instanceof java.util.ArrayList) {
					ArrayList<Object> fieldList = (ArrayList<Object>) field;

					/*
					 * iterate over fieldList
					 */
					for (Object fieldkey : fieldList) {
						validateProjectionFields(subjectClass, htblNotMappedFieldsClasses, notMappedFieldList, fieldkey,
								htblClassNameProperty, uniqueIdentifier, requestClassName);
					}

				} else {

					/*
					 * if the value is not a datatype or an object or a list
					 * then raise InvalidRequestBodyException that tells the
					 * user that this value has an invalid format
					 */
					throw new InvalidQueryRequestBodyFormatException("");
				}
			}
		}
	}

	/*
	 * isFieldMapsToStaticProperty checks if a field maps to a static property (
	 * has map in the list of properties of passed subject class)
	 * 
	 * it returns true if there is a mapping
	 * 
	 * return false if there is no mapping and add subject class to passed
	 * classList in order to get dynamic properties of it and it will add the
	 * field and value to htblNotFoundFieldValue hashtable to be checked again
	 * after laading dynamic properties
	 * 
	 * uniqueIdentifer is a random generated id that is used in
	 * uniqueConstraintValidation as a reference to uniquePopertyValues of an
	 * instance
	 */
	private boolean isFieldMapsToStaticProperty(Class subjectClass, String fieldName, Object fieldObject,
			String uniqueIdentifier, Hashtable<String, Class> htblNotMappedFieldsClasses,
			ArrayList<NotMappedDynamicQueryFields> notMappedFieldList) {
		if (subjectClass.getProperties().containsKey(fieldName)) {
			return true;
		} else {

			htblNotMappedFieldsClasses.put(subjectClass.getUri(), subjectClass);
			NotMappedDynamicQueryFields notMappedDynamicQueryField;

			if (fieldObject == null) {
				notMappedDynamicQueryField = new NotMappedDynamicQueryFields(subjectClass, fieldName, uniqueIdentifier);
			} else {
				notMappedDynamicQueryField = new NotMappedDynamicQueryFields(subjectClass, fieldName, fieldObject,
						uniqueIdentifier);
			}

			notMappedFieldList.add(notMappedDynamicQueryField);
			return false;
		}
	}

	/*
	 * parseAndConstructNotMappedFieldsValues method is used to parse values of
	 * fields that has no static mapping and may have mapping after loading
	 * dynamic properties
	 */
	private void validateNotMappedFieldsValues(String applicationName, Class subjectClass,
			Hashtable<String, Class> htblNotMappedFieldsClasses,
			ArrayList<NotMappedDynamicQueryFields> notMappedFieldList,
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<QueryField>>> htblClassNameProperty,
			String requestClassName) {

		/*
		 * get Dynamic Properties of the classes in the classList which contains
		 * the domain class of the fields in the request that are not mapped to
		 * static properties
		 */

		if (htblNotMappedFieldsClasses.size() > 0) {
			Hashtable<String, DynamicConceptModel> loadedDynamicProperties = dynamicPropertiesUtility
					.getDynamicProperties(applicationName, htblNotMappedFieldsClasses, new Hashtable<>());
			/*
			 * Check that the fields that had no mappings are valid or not
			 * 
			 * This nested Object may have some fields that has no static
			 * mapping and need to checked after loading the dynamicProperties
			 * related to objectClassType and applicationDomain so I have to add
			 * this fields to notMappedFieldList and recursively call again this
			 * method
			 */
			int size = notMappedFieldList.size();
			for (int i = 0; i < size; i++) {
				NotMappedDynamicQueryFields notFoundField = notMappedFieldList.get(i);

				String field = notFoundField.getFieldName();
				/*
				 * After loading dynamic Properties, I am caching all the loaded
				 * properties so If field does not mapped to one of the
				 * properties(contains static ones and cached dynamic ones) of
				 * the subjectClass , I will throw InvalidRequestFieldsException
				 * to indicate the field is invalid
				 */
				if (!notFoundField.getSubjectClass().getProperties().containsKey(field)) {
					throw new InvalidRequestFieldsException(subjectClass.getName(), field);
				} else {

					/*
					 * passed field is a static property so add it to
					 * htblStaticProperty so check that the property is valid
					 * for this application domain
					 *
					 * if the applicationName is null so this field maps a
					 * property in the main ontology .
					 *
					 * if the applicationName is equal to passed applicationName
					 * so it is a dynamic added property to this application
					 * domain
					 *
					 * else it will be a dynamic property in another application
					 * domain which will happen rarely
					 */
					Class dynamicPropertyClass = dynamicPropertiesUtility.getHtblAllStaticClasses()
							.get(loadedDynamicProperties.get(field).getClass_uri());
					Property property = dynamicPropertyClass.getProperties().get(field);

					if (property.getApplicationName() == null
							|| property.getApplicationName().equals(applicationName.replace(" ", "").toUpperCase())) {

						if (notFoundField.getFieldObject() == null) {
							/*
							 * Field is valid dynamic property and has a mapping
							 * for a dynamic property. So I will parse the value
							 * and add prefixes
							 */
							validateProjectionFields(notFoundField.getSubjectClass(), new Hashtable<>(),
									new ArrayList<>(), field, htblClassNameProperty,
									notFoundField.getIndividualUniqueIdintifier(), requestClassName);

						} else {
							/*
							 * if property is an object property so I have to
							 * pass to validateProjectionField an new
							 * notMappedFieldList instance and a new
							 * htblNotMappedFieldsClasses instance in order to
							 * add any unMapped fields for the
							 * nestedObjectValues (if the value is object or
							 * list)
							 */
							ArrayList<NotMappedDynamicQueryFields> notMappedFieldListTemp = new ArrayList<>();
							Hashtable<String, Class> htblNotMappedFieldsClassesTemp = new Hashtable<>();

							/*
							 * pass fieldObject of nonFoundField. This object is
							 * either a list or an object(contains two keys
							 * fieldName and fields)
							 */
							validateProjectionFields(notFoundField.getSubjectClass(), htblNotMappedFieldsClassesTemp,
									notMappedFieldListTemp, notFoundField.getFieldObject(), htblClassNameProperty,
									notFoundField.getIndividualUniqueIdintifier(), requestClassName);

							/*
							 * check if there was unmapped fields in the
							 * nestedObjectValue to load the dynamicProperties
							 * and check those fields by recursively calling
							 * this method again
							 */
							if (htblNotMappedFieldsClassesTemp.size() > 0) {
								validateNotMappedFieldsValues(applicationName, subjectClass,
										htblNotMappedFieldsClassesTemp, notMappedFieldListTemp, htblClassNameProperty,
										requestClassName);

							}

						}

					} else {

						/*
						 * this means that this class has a property with the
						 * same name but it is not for the specified application
						 * domain
						 */

						throw new InvalidRequestFieldsException(subjectClass.getName(), field);

					}
				}

			}
		}

	}

	public static void main(String[] args) {

		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		String szUser = "rdfusr";
		String szPasswd = "rdfusr";
		String szJdbcDriver = "oracle.jdbc.driver.OracleDriver";

		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(szJdbcDriver);
		dataSource.setUrl(szJdbcURL);
		dataSource.setUsername(szUser);
		dataSource.setPassword(szPasswd);

		DynamicConceptDao dynamicConceptDao = new DynamicConceptDao(dataSource);

		Hashtable<String, Object> htblFieldValue = new Hashtable<>();

		ArrayList<Object> fieldsList = new ArrayList<>();

		LinkedHashMap<String, Object> lovesPersonFieldMap = new LinkedHashMap<>();
		ArrayList<Object> lovesPersonFieldList = new ArrayList<>();
		lovesPersonFieldList.add("title");
		lovesPersonFieldList.add("job");

		lovesPersonFieldMap.put("fieldName", "love");
		lovesPersonFieldMap.put("classType", "Developer");
		lovesPersonFieldMap.put("fields", lovesPersonFieldList);

		ArrayList<Object> hatesPersonFieldList = new ArrayList<>();
		hatesPersonFieldList.add("firstName");
		hatesPersonFieldList.add("birthday");
//		hatesPersonFieldList.add(lovesPersonFieldMap);

		LinkedHashMap<String, Object> personFieldMap = new LinkedHashMap<>();
		personFieldMap.put("fieldName", "hates");
		personFieldMap.put("classType", "Developer");
		personFieldMap.put("fields", hatesPersonFieldList);

		LinkedHashMap<String, Object> knowsPersonFieldMap = new LinkedHashMap<>();
		ArrayList<Object> knowsPersonFieldList = new ArrayList<>();
		knowsPersonFieldList.add("age");
		knowsPersonFieldList.add("familyName");

		knowsPersonFieldMap.put("fieldName", "knows");
		knowsPersonFieldMap.put("fields", knowsPersonFieldList);

		fieldsList.add("firstName");
		fieldsList.add("title");
		fieldsList.add("middleName");
		fieldsList.add(knowsPersonFieldMap);
//		fieldsList.add(personFieldMap);

		htblFieldValue.put("fields", fieldsList);

		System.out.println(htblFieldValue);

		GetQueryRequestValidations getQueryRequestValidations = new GetQueryRequestValidations(
				new DynamicPropertiesUtility(dynamicConceptDao));

		try {
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<QueryField>>> htblClassNameProperty = getQueryRequestValidations
					.validateRequest("TESTAPPLICATION", htblFieldValue, Admin.getAdminInstance());
			System.out.println(htblClassNameProperty.toString());
			System.out.println("============================================");
			System.out.println(MainDao.constructSelectQuery(htblClassNameProperty, "TESTAPPLICATION"));
		} catch (ErrorObjException e) {
			System.out.println(e.getExceptionMessage());
		}
	}

}
