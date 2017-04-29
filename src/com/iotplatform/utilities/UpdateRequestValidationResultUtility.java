package com.iotplatform.utilities;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * UpdateRequestValidationResultUtility is used to encapsulate
 * UpdateRequestValidationResult
 * 
 * @author HatemMorgan
 *
 */
public class UpdateRequestValidationResultUtility {

	/*
	 * validationResult is a list of UpdatePropertyValue that is the result of
	 * parsing requestBody and it will be used by UpdateQuery class to generate
	 * update query
	 */
	private ArrayList<UpdatePropertyValueUtility> validationResult = new ArrayList<>();

	/*
	 * classValueList is list of ValueOfTypeClass instances (holds objectValue
	 * and its classType). it will be used to check dataIntegrity constraints
	 */
	private ArrayList<ValueOfTypeClassUtility> classValueList = new ArrayList<>();

	/*
	 * uniquePropValueList is a LikedHashMap of key prefixedClassName and value
	 * LinkedHashMap<String,ArrayList<PropertyValue>> with key
	 * prefixedPropertyName and value list of propertyValue object that holds
	 * the unique propertyName and value I used LinkedHashMap to ensure that the
	 * property will not be duplicated for the prefixedClassName (this will
	 * improve efficiency by reducing graph patterns as there will never be
	 * duplicated properties)
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
	private LinkedHashMap<String, LinkedHashMap<String, ArrayList<Object>>> htblUniquePropValueList = new LinkedHashMap<>();

	/*
	 * isRequestUpdatesUniqueIdentifierValue is a boolean that tells if the
	 * update request updates the unique identifier of the individual.
	 * 
	 * ex. update userName value of a foaf:Person individual
	 */
	private boolean isRequestUpdatesUniqueIdentifierValue;

	/*
	 * newUniqueIdentifierValue holds the value of the new unique identifier
	 */
	private Object newUniqueIdentifierValue;

	public UpdateRequestValidationResultUtility(ArrayList<UpdatePropertyValueUtility> validationResult,
			ArrayList<ValueOfTypeClassUtility> classValueList,
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<Object>>> htblUniquePropValueList) {
		this.validationResult = validationResult;
		this.classValueList = classValueList;
		this.htblUniquePropValueList = htblUniquePropValueList;
	}

	public ArrayList<UpdatePropertyValueUtility> getValidationResult() {
		return validationResult;
	}

	public ArrayList<ValueOfTypeClassUtility> getClassValueList() {
		return classValueList;
	}

	public LinkedHashMap<String, LinkedHashMap<String, ArrayList<Object>>> getHtblUniquePropValueList() {
		return htblUniquePropValueList;
	}

	public boolean isRequestUpdatesUniqueIdentifierValue() {
		return isRequestUpdatesUniqueIdentifierValue;
	}

	public Object getNewUniqueIdentifierValue() {
		return newUniqueIdentifierValue;
	}

	public void setNewUniqueIdentifierValue(Object newUniqueIdentifierValue) {
		this.newUniqueIdentifierValue = newUniqueIdentifierValue;
		this.isRequestUpdatesUniqueIdentifierValue = true;
	}

	@Override
	public String toString() {
		return "UpdateRequestValidationResultUtility [validationResult=" + validationResult + ", classValueList="
				+ classValueList + ", htblUniquePropValueList=" + htblUniquePropValueList
				+ ", isRequestUpdatesUniqueIdentifierValue=" + isRequestUpdatesUniqueIdentifierValue
				+ ", newUniqueIdentifierValue=" + newUniqueIdentifierValue + "]";
	}

}
