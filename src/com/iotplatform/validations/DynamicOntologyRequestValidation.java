package com.iotplatform.validations;

import java.util.Hashtable;
import java.util.LinkedHashMap;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Prefix;

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

@Component
public class DynamicOntologyRequestValidation {

	private static Hashtable<String, Prefix> htblPrefixes;

	private static void validateDynamicOntologyClassRequest(LinkedHashMap<String, Object> htblRequestBody) {

	}

	/*
	 * getPrefix is used to get Prefix enum that maps prefixAlias and it will
	 * return null if the prefixAlias is not valid
	 */
	private static Prefix getPrefix(String prefixAlias) {
		if (htblPrefixes == null) {
			htblPrefixes = new Hashtable<>();

			for (Prefix prefix : Prefix.values()) {
				htblPrefixes.put(prefix.getPrefixName(), prefix);
			}

		}

		if (htblPrefixes.containsKey(prefixAlias)) {
			return htblPrefixes.get(prefixAlias);
		} else {
			return null;
		}
	}

}
