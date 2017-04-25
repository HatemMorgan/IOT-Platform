package com.iotplatform.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.daos.DynamicOntologyDao;
import com.iotplatform.daos.ValidationDao;

@Component
public class UpdatingService {

	private DynamicOntologyDao dynamicOntologyDao;
	private ValidationDao validationDao;
	
	@Autowired
	public UpdatingService(DynamicOntologyDao dynamicOntologyDao, ValidationDao validationDao) {
		this.dynamicOntologyDao = dynamicOntologyDao;
		this.validationDao = validationDao;
	}
	
	
		
}
