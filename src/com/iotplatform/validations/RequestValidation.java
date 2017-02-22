package com.iotplatform.validations;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.daos.DynamicConceptDao;
import com.iotplatform.daos.ValidationDao;
import com.iotplatform.models.DynamicConceptModel;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Property;

@Component
public class RequestValidation {

	private ValidationDao validationDao;
	private DynamicConceptDao dynamicConceptDao;

	@Autowired
	public RequestValidation(ValidationDao validationDao, DynamicConceptDao dynamicConceptDao) {

		this.validationDao = validationDao;
		this.dynamicConceptDao = dynamicConceptDao;
	}

	/*
	 * checkIfFieldsValid checks if the fields passed by the http request are
	 * valid or not and it return an array of hashtables if the fields are valid
	 * which contains the hashtable of dynamic properties and the other
	 * hashtable for static properties
	 */
	private Hashtable<String, Object>[] isFieldsValid(String applicationName, Class subjectClass,
			Hashtable<String, Object> htblPropertyValue) {

		Hashtable<String, Object>[] returnedArray = (Hashtable<String, Object>[]) new Hashtable<?, ?>[2];
		Hashtable<String, Object> htblstaticProperties = new Hashtable<>();
		Hashtable<String, Object> htbldynamicProperties = new Hashtable<>();

		List<DynamicConceptModel> dynamicProperties = null;

		Hashtable<String, Property> htblProperties = subjectClass.getProperties();
		Iterator<String> iterator = htblPropertyValue.keySet().iterator();

		while (iterator.hasNext()) {
			String field = iterator.next();

			/*
			 * if not a static property go and get dynamic properties of that
			 * class
			 */
			if (!htblProperties.containsKey(field)) {

				/*
				 * to get the dynamic properties only one time
				 */
				if (dynamicProperties == null) {
					Hashtable<String, String> htblFilter = new Hashtable<>();
					htblFilter.put("class_name",)
				}
			}
		}

		returnedArray[0] = htblstaticProperties;
		returnedArray[1] = htbldynamicProperties;
		return null;
	}

}
