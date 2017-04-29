package com.iotplatform.daos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import oracle.spatial.rdf.client.jena.Oracle;

@Repository("updateDao")
public class UpdateDao {

	private Oracle oracle;

	@Autowired
	public UpdateDao(Oracle oracle) {
		this.oracle = oracle;
	}
	
	
}
