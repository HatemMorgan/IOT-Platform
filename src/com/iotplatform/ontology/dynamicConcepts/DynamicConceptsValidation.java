package com.iotplatform.ontology.dynamicConcepts;

import com.iotplatform.exceptions.InvalidDynamicOntologyException;
import com.iotplatform.models.DynamicConceptModel;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.PropertyType;
import com.iotplatform.ontology.XSDDatatype;

public class DynamicConceptsValidation {

	/*
	 * isValidUri method is used to check if the classURI, propertyURI and
	 * ObjectTypeURI have valid URI (started with one of the prefixes URIs
	 * defined in the ontology)
	 */
	public static boolean isNewConceptValid(DynamicConceptModel newConcept) {

		boolean validClassURI = false;
		boolean validPropertyURI = false;
		boolean validObjectTypeURI = false;

		for (Prefix prefix : Prefix.values()) {

			if (!validClassURI) {
				if (newConcept.getClass_uri().contains(prefix.getUri())
						&& newConcept.getClass_prefix_uri().equals(prefix.getUri())
						&& newConcept.getClass_prefix_alias().equals(prefix.getPrefix()))
					validClassURI = true;
			}

			if (!validPropertyURI) {
				if (newConcept.getProperty_uri().contains(prefix.getUri())
						&& newConcept.getProperty_prefix_uri().equals(prefix.getUri())
						&& newConcept.getProperty_prefix_alias().equals(prefix.getPrefix()))
					validPropertyURI = true;
			}

			if (!validObjectTypeURI) {
				if (newConcept.getProperty_object_type_uri().contains(prefix.getUri()))
					validObjectTypeURI = true;
			}

		}

		if (!(newConcept.getIsUnique() == 0 || newConcept.getIsUnique() == 1)) {
			throw new InvalidDynamicOntologyException(
					"Invalid isUnique field value. isUnique field must be 0(false) or 1(true)");
		}

		if (!(newConcept.getHasMultipleValues() == 0 || newConcept.getHasMultipleValues() == 1)) {
			throw new InvalidDynamicOntologyException(
					"Invalid hasMultipleValues field value. hasMultipleValues field must be 0(false) or 1(true)");
		}

		if (!validClassURI) {
			throw new InvalidDynamicOntologyException(
					"Invalid Class fields values. ClassURI, classPrefixURI and classPrefixAlias values must"
							+ " be a valid URI with one of the ontology prefixes");
		}

		if (!validPropertyURI) {
			throw new InvalidDynamicOntologyException(
					"Invalid Class fields values. ClassURI, classPrefixURI and classPrefixAlias values must"
							+ " be a valid URI with one of the ontology prefixes");
		}

		if (!validObjectTypeURI) {
			throw new InvalidDynamicOntologyException("Invalid Class fields values. propertyObjectTypeURI values must"
					+ " be a valid URI with one of the ontology prefixes");
		}

		if (!(newConcept.getProperty_type().equals(PropertyType.DatatypeProperty.toString())
				|| newConcept.getProperty_type().equals(PropertyType.ObjectProperty.toString()))) {
			throw new InvalidDynamicOntologyException("Invalid propertyObjectTypeURI value. propertyObjectTypeURI field"
					+ " must have a value either \"DatatypeProperty\" or \"ObjectProperty\" ");
		}

		/*
		 * check if DataTypeProperty as a valid datatype
		 */
		if (newConcept.getProperty_type().equals(PropertyType.DatatypeProperty.toString()) && validObjectTypeURI) {
			boolean check = false;
			for (XSDDatatype xsdDataType : XSDDatatype.values()) {
				if (newConcept.getProperty_object_type_uri().equals(xsdDataType.getXsdTypeURI())) {
					check = true;
					break;
				}
			}

			if (!check) {
				throw new InvalidDynamicOntologyException(
						"Invalid DataType for new added DataProperty. Check documentation for more information");
			}
		}

		return true;
	}

}
