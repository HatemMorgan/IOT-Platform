//package com.iotplatform.validations;
//
//import java.util.ArrayList;
//import java.util.Hashtable;
//import java.util.LinkedHashMap;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import com.iotplatform.daos.DynamicConceptDao;
//import com.iotplatform.ontology.Class;
//import com.iotplatform.ontology.classes.Property;
//import com.iotplatform.utilities.QueryField;
//import com.iotplatform.utilities.ValueOfFieldNotMappedToStaticProperty;
//
//@Component
//public class GetQueryRequestValidations {
//
//	public void validateRequest(String applicationName, Hashtable<String, Object> htblFieldValue, Class subjectClass) {
//
//	}
//
//	private LinkedHashMap<String, ArrayList<QueryField>> validateProjectionFields(ArrayList<Object> fieldsList,
//			Class subjectClass) {
//
//		for (Object field : fieldsList) {
//
//			if (field instanceof String) {
//
//			}
//
//		}
//
//		return null;
//	}
//
//	/*
//	 * isFieldMapsToStaticProperty checks if a field maps to a static property (
//	 * has map in the list of properties of passed subject class)
//	 * 
//	 * it returns true if there is a mapping
//	 * 
//	 * return false if there is no mapping and add subject class to passed
//	 * classList in order to get dynamic properties of it and it will add the
//	 * field and value to htblNotFoundFieldValue hashtable to be checked again
//	 * after laading dynamic properties
//	 * 
//	 * uniqueIdentifer is a random generated id that is used in
//	 * uniqueConstraintValidation as a reference to uniquePopertyValues of an
//	 * instance
//	 */
//	private boolean isFieldMapsToStaticProperty(Class subjectClass, String fieldName,
//			Hashtable<String, Class> htblNotMappedFieldsClasses, ArrayList<String> notMappedFieldList) {
//
//		if (subjectClass.getProperties().containsKey(fieldName)) {
//			return true;
//		} else {
//			htblNotMappedFieldsClasses.put(subjectClass.getUri(), subjectClass);
//			notMappedFieldList.add(fieldName);
//			return false;
//		}
//	}
//
//}
