package com.iotplatform.validations;

import java.util.Hashtable;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.daos.DynamicConceptDao;
import com.iotplatform.ontology.Class;

@Component
public class GetQueryRequestValidations {

	private DynamicConceptDao dynamicConceptDao;
	private Hashtable<String, Class> htblAllStaticClasses;

	@Autowired
	public GetQueryRequestValidations(DynamicConceptDao dynamicConceptDao,
			Hashtable<String, Class> htblAllStaticClasses) {
		this.dynamicConceptDao = dynamicConceptDao;
		this.htblAllStaticClasses = htblAllStaticClasses;
	}

	public void validateRequest(String applicationName, Hashtable<String, Object> htblFieldValue, Class subjectClass) {
		
	}

//	private LinkedHashMap<String,>
	
}
