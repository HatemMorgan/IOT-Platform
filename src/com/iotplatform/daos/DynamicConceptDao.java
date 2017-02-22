package com.iotplatform.daos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.iotplatform.models.DynamicConceptModel;

@Repository("dynamicConceptsDao")
public class DynamicConceptDao {
	@Autowired
	DynamicConceptModel daConceptModel;
}
