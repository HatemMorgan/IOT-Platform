package com.iotplatform.daos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.ontology.classes.Admin;
import com.iotplatform.ontology.classes.Developer;
import com.iotplatform.ontology.classes.NormalUser;

//@Component
public class PersonDAO {

	private Admin admin;
	private Developer developer;
	private NormalUser normalUser;

//	@Autowired
	public PersonDAO(Admin admin, Developer developer, NormalUser normalUser) {
		super();
		this.admin = admin;
		this.developer = developer;
		this.normalUser = normalUser;
	}

	
	
}
