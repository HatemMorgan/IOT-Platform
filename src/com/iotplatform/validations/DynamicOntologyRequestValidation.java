package com.iotplatform.validations;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.InvalidDynamicOntologyException;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.XSDDatatype;

/*
 * DynamicOntologyRequestValidation class is used to validate and parse dynamic ontology request body 
 * to be used to after that to generate SPARQL-in-SQL query to add new concepts as turtles to
 *  mainOntology of a specific application domain 
 *  
 * It checks that the request is valid by checking that the request body format is acceptable and
 * checks that the new concept does not violate any constraints:
 * 
 * 1- to insert a new class :
 * 
 * 	 insert new ontology class : name(required), uri(required), prefix(required) , 
 *	 uniqueIdentifier(not required) , listOfProperties(not required), superClassList(not required), 
 *	 typeClassList(not required), RestrictionList (not required) 
 * 
 * 2- to insert a new property:
 * 
 * 	 name(required), prefix(required), multipleValues(required), unique(required), 
 * 	 subjectClass(not required), propertyType(required), range(not required) 
 * 
 */

public class DynamicOntologyRequestValidation {

	private static Hashtable<String, Prefix> htblPrefixes;
	private static Hashtable<String, XSDDatatype> htblXSDDatatypes;

	public DynamicOntologyRequestValidation() {
		loadPrefixes();
		loadXSDdataTypes();
	}

	/*
	 * validateDynamicOntologyClassRequest validate requestBody of dynamic
	 * ontology class insertion
	 */
	public static LinkedHashMap<String, LinkedHashMap<String, Object>> validateDynamicOntologyClassRequest(
			LinkedHashMap<String, Object> htblRequestBody) {

		/*
		 * validationResult contains :
		 * 
		 * key is the classUri and its value is LinkedHashMap<String, Object>
		 * contains:
		 * 
		 * 1- key = prefixedClassName and value = prefixedClassName(eg.
		 * iot-platform:Developer)
		 * 
		 * 2- key = subClass and value = a list of subClassesURIs
		 * 
		 * 3- key = restriction and value = a linkedHashMap contains restriction
		 * (eg. {owl:onProperty = foaf:name ; owl:allValuesFrom = xsd:string .})
		 * 
		 * 4- key = classURI (a nested subclass) and value = linkedHashMap
		 * contains the same as the above 2 points because it is a new class to
		 * be inserted and but it is a subClass of the first one
		 */
		LinkedHashMap<String, LinkedHashMap<String, Object>> validationResult = new LinkedHashMap<>();

		validateNewClassMap(validationResult, htblRequestBody, false, null, false, null);

		return validationResult;
	}

	private static void validateNewClassMap(LinkedHashMap<String, LinkedHashMap<String, Object>> validationResult,
			LinkedHashMap<String, Object> newClassMap, boolean isSuperClass, String subClassPrefixedName,
			boolean isSubClass, String superClassPrefixedName) {

		/*
		 * check that the required fields name and prefixAlias are exist
		 */
		if (!newClassMap.containsKey("name")) {
			throw new InvalidDynamicOntologyException("Invalid Dynamic Ontology class insertion request. "
					+ "To insert a new ontology class, you must provide required field name which has "
					+ "value the new class name.");
		}

		if (!newClassMap.containsKey("prefixAlias")) {
			throw new InvalidDynamicOntologyException("Invalid Dynamic Ontology class insertion request. "
					+ "To insert a new ontology class, you must provide required field prefixAlias "
					+ "which has value the new class prefixAlias eg: foaf or iot-platform .");
		}

		/*
		 * get values name and prefixAlias keys
		 */
		String newClassName = newClassMap.get("name").toString();
		Prefix newClassPrefix = htblPrefixes.get(newClassMap.get("prefixAlias").toString());

		/*
		 * Capitalize the first character of the className in order to match
		 * universal conventions
		 */
		newClassName = newClassName.substring(0, 1).toUpperCase() + newClassName.substring(1);

		/*
		 * check that the newClassPrefix is a valid prefix (not equal null)
		 */
		if (newClassPrefix == null) {
			throw new InvalidDynamicOntologyException("Invalid Dynamic Ontology class insertion request. "
					+ "InValid prefixAlias with value = " + newClassMap.get("prefixAlias").toString() + "."
					+ " The prefixAlias must have a value from " + htblPrefixes.keySet().toString());
		}

		/*
		 * add newClass prefixedClassName to validationResult
		 */
		String newClassPrefixedName = newClassPrefix.getPrefix() + newClassName;
		validationResult.put(newClassPrefixedName, new LinkedHashMap<String, Object>());
		validationResult.get(newClassPrefixedName).put("prefixedClassName", newClassPrefix.getPrefix() + newClassName);

		/*
		 * if isSuperClass = true it means that the passed class is a superClass
		 * of a class with subClassPrefixedName
		 */
		if (isSuperClass) {
			/*
			 * create a new keyField with name subClassList and its value is a
			 * list of subClassesPrefixedNames
			 */
			validationResult.get(newClassPrefixedName).put("subClassList", new ArrayList<String>());

			/*
			 * add subClass with subClassPrefixedName in the subClassList
			 */
			((ArrayList<String>) validationResult.get(newClassPrefixedName).get("subClassList"))
					.add(subClassPrefixedName);

		}

		/*
		 * if isSubClass = true it means that the passed class is a subClass of
		 * a class with superClassPrefixedName
		 */
		if (isSubClass) {
			/*
			 * check if superClassPrefixedName exist before in validationResult
			 */
			if (validationResult.containsKey(superClassPrefixedName)) {

				/*
				 * check if hashMap of superClassPrefixedName has subClassList
				 * keyField
				 */
				if (validationResult.get(superClassPrefixedName).containsKey("subClassList")) {

					/*
					 * add newClassURI to the subClassList of the superClass
					 */
					((ArrayList<String>) validationResult.get(superClassPrefixedName).get("subClassList"))
							.add(newClassPrefixedName);
				} else {
					/*
					 * add subClassList to hashMap of superClassPrefixedName in
					 * validationResult to hold subClasses prefixedNames of
					 * superClassPrefixedName
					 */
					validationResult.get(superClassPrefixedName).put("subClassList", new ArrayList<String>());

					/*
					 * add newClassURI to the subClassList of the superClass
					 */
					((ArrayList<String>) validationResult.get(superClassPrefixedName).get("subClassList"))
							.add(newClassPrefixedName);
				}

			} else {
				/*
				 * superClassPrefixedName does not exist in validationResult so
				 * create a new fieldKey= validationResult
				 */
				validationResult.put(superClassPrefixedName, new LinkedHashMap<>());

				/*
				 * add subClassList to hashMap of superClassPrefixedName in
				 * validationResult to hold subClasses prefixedNames of
				 * superClassPrefixedName
				 */
				validationResult.get(superClassPrefixedName).put("subClassList", new ArrayList<String>());

				/*
				 * add newClassURI to the subClassList of the superClass
				 */
				((ArrayList<String>) validationResult.get(superClassPrefixedName).get("subClassList"))
						.add(newClassPrefixedName);

			}
		}

		/*
		 * Iterate over the htblRequestBody to validate and parse it
		 */
		Iterator<String> htblRequestBodyIter = newClassMap.keySet().iterator();

		/*
		 * flag is used to check that key is valid to throw an exception if it
		 * is not valid
		 */
		boolean flag;
		while (htblRequestBodyIter.hasNext()) {
			String key = htblRequestBodyIter.next();
			flag = false;
			/*
			 * skip name and prefixAlias keys because I used them above
			 */
			if (key.equals("name") || key.equals("prefixAlias"))
				continue;

			/*
			 * check if the requestBody has uniqueIdentifierPropertyName key
			 * field which tells that the new class has uniqueIdentifier so the
			 * system will treat the value passed of this
			 * uniqueIdentifierProperty as the subjectIdnetifier of any instance
			 * of this class
			 */
			if (key.equals("uniqueIdentifierPropertyName")) {

				/*
				 * check if value of uniqueIdentifierPropertyName key field has
				 * a valid dataType (String)
				 */
				if (newClassMap.get(key) instanceof String) {

					/*
					 * get uniqueIdentifierPropertyName
					 */
					String uniqueIdentifierPropertyName = newClassMap.get(key).toString();

					/*
					 * add uniqueIdentiferProperty prefixedName to
					 * htblUniqueIdentifier
					 */
					validationResult.get(newClassPrefixedName).put("uniqueIdentiferProperty",
							uniqueIdentifierPropertyName);

					flag = true;
				} else {

					throw new InvalidDynamicOntologyException("Invalid Dynamic Ontology class insertion request. "
							+ "Invalid uniqueIdentiferProperty key field. Its value must be a String "
							+ "which identifies the prefixedPropertyName of the uniqueIdentifierproperty");
				}
			}

			if (key.equals("superClassList")) {

				/*
				 * check if value of superClassList key field has a valid
				 * dataType (list)
				 */
				if (newClassMap.get(key) instanceof java.util.ArrayList) {

					/*
					 * call validateSuperClassListKeyField to parse and validate
					 * superClass field
					 */
					validateSuperClassListKeyField(newClassPrefixedName, validationResult,
							(ArrayList<Object>) newClassMap.get(key));
					flag = true;

				} else {
					throw new InvalidDynamicOntologyException("Invalid Dynamic Ontology class insertion request. "
							+ "Invalid superClassList key field. Its value must be a list");
				}
			}

			if (key.equals("subClassList")) {

				/*
				 * check if value of subClassList key field has a valid dataType
				 * (list)
				 */
				if (newClassMap.get(key) instanceof java.util.ArrayList) {

					/*
					 * create a new keyField (subClassList) in the hashMap of
					 * newClassPrefixedName in validationResult with value
					 * StringList
					 */
					validationResult.get(newClassPrefixedName).put("subClassList", new ArrayList<String>());

					/*
					 * call validationResult to parse and validate superClass
					 * field
					 */
					validateSubClassListKeyField(newClassPrefixedName, validationResult,
							(ArrayList<Object>) newClassMap.get(key));

					flag = true;

				} else {

					throw new InvalidDynamicOntologyException("Invalid Dynamic Ontology class insertion request. "
							+ "Invalid subClassList key field. Its value must be a list");
				}
			}

			if (key.equals("restrictionList")) {

				/*
				 * check if value of RestrictionList key field has a valid
				 * dataType (list)
				 */
				if (newClassMap.get(key) instanceof java.util.ArrayList) {

				} else {

					throw new InvalidDynamicOntologyException("Invalid Dynamic Ontology class insertion request. "
							+ "Invalid restrictionList key field. Its value must be a list");
				}
			}

			if (!flag) {
				throw new InvalidDynamicOntologyException("Invalid Dynamic Ontology class insertion request. "
						+ "Invalid field: " + key + " Check the documentation to learn the available "
						+ "fields to insert a new ontology class");
			}

		}
	}

	/*
	 * validateSubClassListKeyField is used to parse and validate
	 * subClassKeyField value
	 */
	private static void validateSubClassListKeyField(String currentClassPrefixedName,
			LinkedHashMap<String, LinkedHashMap<String, Object>> validationResult, ArrayList<Object> subClassesList) {

		for (Object subClass : subClassesList) {
			/*
			 * check that the subClass is a String. Therefore it will be the
			 * prefixedName of an existing class eg. foaf:Person
			 */
			if (subClass instanceof String) {

				String subClassPrefixedName = subClass.toString();
				/*
				 * add superClassName to superClassList of currentClassURI in
				 * validationResult
				 */
				((ArrayList<String>) validationResult.get(currentClassPrefixedName).get("subClassList"))
						.add(subClassPrefixedName);

			} else {

				/*
				 * check if the superClass is a new nestedClass
				 */
				if (subClass instanceof java.util.LinkedHashMap<?, ?>) {

					validateNewClassMap(validationResult, (LinkedHashMap<String, Object>) subClass, false, null, true,
							currentClassPrefixedName);
				} else {

					/*
					 * invalid format
					 */
					throw new InvalidDynamicOntologyException("Invalid Dynamic Ontology class insertion request. "
							+ "Invalid field: subClassList, the value of subClassList field must be a list of"
							+ "either a 1- String (prefixedClassName of an existing class). "
							+ "2- An object (A nested new subClass).");
				}
			}
		}
	}

	/*
	 * validateSuperClassListKeyField is used to parse and validate
	 * superClassKeyField value
	 */
	private static void validateSuperClassListKeyField(String currentClassPrefixedName,
			LinkedHashMap<String, LinkedHashMap<String, Object>> validationResult, ArrayList<Object> superClassesList) {

		for (Object superClass : superClassesList) {

			/*
			 * check that the superClass is a String. Therefore it will be the
			 * prefixedName of an existing class eg. foaf:Person
			 */
			if (superClass instanceof String) {

				String superClassPrefixedName = superClass.toString();

				/*
				 * add superClass's prefixedName to validationResult and add a
				 * subClassList and add the currentClassPrefixedName in
				 * subClassList
				 * 
				 * 
				 * check if superClassPrefixedName exist before in
				 * validationResult
				 */
				if (validationResult.containsKey(superClassPrefixedName)) {

					/*
					 * check if hashMap of superClassPrefixedName has
					 * subClassList keyField
					 */
					if (validationResult.get(superClassPrefixedName).containsKey("subClassList")) {

						/*
						 * add newClassURI to the subClassList of the superClass
						 */
						((ArrayList<String>) validationResult.get(superClassPrefixedName).get("subClassList"))
								.add(currentClassPrefixedName);
					} else {
						/*
						 * add subClassList to hashMap of superClassPrefixedName
						 * in validationResult to hold subClasses prefixedNames
						 * of superClassPrefixedName
						 */
						validationResult.get(superClassPrefixedName).put("subClassList", new ArrayList<String>());

						/*
						 * add newClassURI to the subClassList of the superClass
						 */
						((ArrayList<String>) validationResult.get(superClassPrefixedName).get("subClassList"))
								.add(currentClassPrefixedName);
					}

				} else {
					/*
					 * superClassPrefixedName does not exist in validationResult
					 * so create a new fieldKey= validationResult
					 */
					validationResult.put(superClassPrefixedName, new LinkedHashMap<>());

					/*
					 * add exist keyField which specifies that this
					 * superClassPrefixedName is of an existing ontology class
					 */
					validationResult.get(superClassPrefixedName).put("exist", true);

					/*
					 * add subClassList to hashMap of superClassPrefixedName in
					 * validationResult to hold subClasses prefixedNames of
					 * superClassPrefixedName
					 */
					validationResult.get(superClassPrefixedName).put("subClassList", new ArrayList<String>());

					/*
					 * add newClassURI to the subClassList of the superClass
					 */
					((ArrayList<String>) validationResult.get(superClassPrefixedName).get("subClassList"))
							.add(currentClassPrefixedName);

				}

			} else {

				/*
				 * check if the superClass is a new nestedClass
				 */
				if (superClass instanceof java.util.LinkedHashMap<?, ?>) {
					validateNewClassMap(validationResult, (LinkedHashMap<String, Object>) superClass, true,
							currentClassPrefixedName, false, null);
				} else {

					/*
					 * invalid format
					 */
					throw new InvalidDynamicOntologyException("Invalid Dynamic Ontology class insertion request. "
							+ "Invalid field: superClassList, the value of superClassList field must be a list of"
							+ "either a 1- String (prefixedClassName of an existing class). "
							+ "2- An object (A nested new superClass).");
				}
			}
		}
	}

	/*
	 * validateRestrictionKeyField is used to parse and validate
	 * restricitonField value
	 */
	private static void validateRestrictionKeyField(String currentClassPrefixedName,
			LinkedHashMap<String, LinkedHashMap<String, Object>> validationResult, ArrayList<Object> restrictionList) {

	}

	/*
	 * getPrefix is used to get Prefix enum that maps prefixAlias and it will
	 * return null if the prefixAlias is not valid
	 */
	private static void loadPrefixes() {

		htblPrefixes = new Hashtable<>();

		for (Prefix prefix : Prefix.values()) {
			htblPrefixes.put(prefix.getPrefixName(), prefix);
		}

	}

	/*
	 * getPrefix is used to get Prefix enum that maps prefixAlias and it will
	 * return null if the prefixAlias is not valid
	 */
	private static void loadXSDdataTypes() {

		htblXSDDatatypes = new Hashtable<>();

		for (XSDDatatype xsdDatatype : XSDDatatype.values()) {
			htblXSDDatatypes.put(xsdDatatype.getDataType(), xsdDatatype);
		}

	}

	private static boolean isPrefixValid(String prefixedName) {
		int index = prefixedName.indexOf(":");
		String prefix = prefixedName.substring(0, index + 1);

		return (htblPrefixes.containsKey(prefix)) ? true : false;

	}

	/**
	 * getXSDDataTypeEnumOfDataTypeName return XsdDataType enum instance
	 * of @param(dataTypeName)
	 * 
	 * @param dataTypeName
	 *            holds datatypeName eg. string,integer
	 * @return
	 */
	private XSDDatatype getXSDDataTypeEnumOfDataTypeName(String dataTypeName) {

		if (XSDDatatype.boolean_type.getDataType().equals(dataTypeName)) {
			return XSDDatatype.boolean_type;
		}

		if (XSDDatatype.decimal_typed.getDataType().equals(dataTypeName)) {
			return XSDDatatype.decimal_typed;
		}

		if (XSDDatatype.float_typed.getDataType().equals(dataTypeName)) {
			return XSDDatatype.float_typed;
		}

		if (XSDDatatype.integer_typed.getDataType().equals(dataTypeName)) {
			return XSDDatatype.integer_typed;
		}

		if (XSDDatatype.string_typed.getDataType().equals(dataTypeName)) {
			return XSDDatatype.string_typed;
		}

		if (XSDDatatype.dateTime_typed.getDataType().equals(dataTypeName)) {
			return XSDDatatype.dateTime_typed;
		}

		if (XSDDatatype.double_typed.getDataType().equals(dataTypeName)) {
			return XSDDatatype.double_typed;
		}

		return null;
	}

	public static Hashtable<String, Object> validateNewClassOntologyRequest(Hashtable<String, Object> htblRequestBody) {

		if (htblRequestBody.size() == 1 && htblRequestBody.containsKey("name")
				&& htblRequestBody.get("name") instanceof String) {

			return htblRequestBody;
		} else {
			throw new InvalidDynamicOntologyException("Invalid Dynamic Ontology class insertion request. "
					+ "The request body must have only one key with value name and its "
					+ "value is a String. ex: \"name\":\"Person\" ");
		}

	}

	public static Hashtable<String, Object> validateNewObjectPropertyOntologyRequest(
			Hashtable<String, Object> htbRequestBody) {

		if (htbRequestBody.size() != 5) {
			throw new InvalidDynamicOntologyException("Invalid Dynamic Ontology  ObjectProperty insertion request. "
					+ "The request body must have propertyName,domainPrefixName,rangePrefixName,hasMultipleValues,isUnique ");
		}

		if (!(htbRequestBody.containsKey("propertyName") && htbRequestBody.get("propertyName") instanceof String)) {
			throw new InvalidDynamicOntologyException("Invalid Dynamic ObjectProperty insertion request. "
					+ "The request body must have key with value propertyName and its "
					+ "value is a String. ex: \"propertyName\":\"knows\" ");
		}

		if (!(htbRequestBody.containsKey("domainPrefixName")
				&& htbRequestBody.get("domainPrefixName") instanceof String)
				&& isPrefixValid(htbRequestBody.get("domainPrefixName").toString())) {
			throw new InvalidDynamicOntologyException("Invalid Dynamic ObjectProperty insertion request. "
					+ "The request body must have key with value domainPrefixName and its "
					+ "value is a String. ex: \"domainPrefixName\":\"foaf:Person\" ");
		}

		if (!(htbRequestBody.containsKey("rangePrefixName") && htbRequestBody.get("rangePrefixName") instanceof String)
				&& isPrefixValid(htbRequestBody.get("rangePrefixName").toString())) {
			throw new InvalidDynamicOntologyException("Invalid Dynamic ObjectProperty insertion request. "
					+ "The request body must have key with value rangePrefixName and its "
					+ "value is a String. ex: \"rangePrefixName\":\"foaf:Person\" ");
		}

		if (!(htbRequestBody.containsKey("hasMultipleValues")
				&& htbRequestBody.get("hasMultipleValues") instanceof Boolean)) {
			throw new InvalidDynamicOntologyException("Invalid Dynamic ObjectProperty insertion request. "
					+ "The request body must have key with value hasMultipleValues and its "
					+ "value is a boolean. ex: \"hasMultipleValues\": true ");
		}

		if (!(htbRequestBody.containsKey("isUnique") && htbRequestBody.get("isUnique") instanceof Boolean)) {
			throw new InvalidDynamicOntologyException("Invalid Dynamic ObjectProperty insertion request. "
					+ "The request body must have key with value isUnique and its "
					+ "value is a boolean. ex: \"isUnique\": false ");
		}

		return htbRequestBody;

	}

	public static Hashtable<String, Object> validateNewDataTypPropertyOntologyRequest(
			Hashtable<String, Object> htbRequestBody) {

		if (htbRequestBody.size() != 5) {
			throw new InvalidDynamicOntologyException("Invalid Dynamic Ontology  DataTypeProperty insertion request. "
					+ "The request body must have propertyName,domainPrefixName,dataType,hasMultipleValues,isUnique ");
		}

		if (!(htbRequestBody.containsKey("propertyName") && htbRequestBody.get("propertyName") instanceof String)) {
			throw new InvalidDynamicOntologyException("Invalid Dynamic DataTypeProperty insertion request. "
					+ "The request body must have key with value propertyName and its "
					+ "value is a String. ex: \"propertyName\":\"knows\" ");
		}

		if (!(htbRequestBody.containsKey("domainPrefixName")
				&& htbRequestBody.get("domainPrefixName") instanceof String)
				&& isPrefixValid(htbRequestBody.get("domainPrefixName").toString())) {
			throw new InvalidDynamicOntologyException("Invalid Dynamic DataTypeProperty insertion request. "
					+ "The request body must have key with value domainPrefixName and its "
					+ "value is a String. ex: \"domainPrefixName\":\"foaf:Person\" ");
		}

		if (!(htbRequestBody.containsKey("dataType") && htbRequestBody.get("dataType") instanceof String)
				&& htblXSDDatatypes.containsKey(htbRequestBody.get("rangePrefixName").toString().toLowerCase())) {
			throw new InvalidDynamicOntologyException("Invalid Dynamic DataTypeProperty insertion request. "
					+ "The request body must have key with value dataType and its "
					+ "value is a String. ex: \"dataType\":\"string\" ");
		}

		if (!(htbRequestBody.containsKey("hasMultipleValues")
				&& htbRequestBody.get("hasMultipleValues") instanceof Boolean)) {
			throw new InvalidDynamicOntologyException("Invalid Dynamic DataTypeProperty insertion request. "
					+ "The request body must have key with value hasMultipleValues and its "
					+ "value is a boolean. ex: \"hasMultipleValues\": true ");
		}

		if (!(htbRequestBody.containsKey("isUnique") && htbRequestBody.get("isUnique") instanceof Boolean)) {
			throw new InvalidDynamicOntologyException("Invalid Dynamic DataTypeProperty insertion request. "
					+ "The request body must have key with value isUnique and its "
					+ "value is a boolean. ex: \"isUnique\": false ");
		}

		return htbRequestBody;

	}

	public static void main(String[] args) {
		LinkedHashMap<String, Object> htblRequestBody = new LinkedHashMap<>();

		htblRequestBody.put("name", "virtualSensor");
		htblRequestBody.put("prefixAlias", "iot-platform");
		htblRequestBody.put("uniqueIdentifierPropertyName", "iot-lite:id");

		ArrayList<Object> subClassesList = new ArrayList<>();
		htblRequestBody.put("subClassList", subClassesList);

		subClassesList.add("ssn:Sensor");
		LinkedHashMap<String, Object> subClass = new LinkedHashMap<>();
		subClass.put("name", "human");
		subClass.put("prefixAlias", "iot-platform");
		subClassesList.add(subClass);

		ArrayList<Object> superClassList = new ArrayList<>();
		htblRequestBody.put("superClassList", superClassList);

		superClassList.add("ssn:Device");

		LinkedHashMap<String, Object> superClass = new LinkedHashMap<>();
		superClass.put("name", "VirtualSensing");
		superClass.put("prefixAlias", "iot-platform");

		ArrayList<Object> superClassList2 = new ArrayList<>();
		superClass.put("superClassList", superClassList2);
		superClassList2.add("ssn:Device");

		superClassList.add(superClass);

		System.out.println(htblRequestBody);

		try {
			LinkedHashMap<String, LinkedHashMap<String, Object>> validationRes = DynamicOntologyRequestValidation
					.validateDynamicOntologyClassRequest(htblRequestBody);
			System.out.println(validationRes);
		} catch (ErrorObjException e) {
			System.out.println(e.getExceptionMessage());
		}

	}

}
