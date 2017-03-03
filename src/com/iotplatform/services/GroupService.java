package com.iotplatform.services;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.AdminDao;
import com.iotplatform.daos.ApplicationDao;
import com.iotplatform.daos.DynamicConceptDao;
import com.iotplatform.daos.GroupDao;
import com.iotplatform.daos.ValidationDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.NoApplicationModelException;
import com.iotplatform.models.SuccessfullInsertionModel;
import com.iotplatform.models.SuccessfullSelectAllJsonModel;
import com.iotplatform.ontology.classes.Admin;
import com.iotplatform.ontology.classes.Application;
import com.iotplatform.ontology.classes.Group;
import com.iotplatform.utilities.PropertyValue;
import com.iotplatform.utilities.SelectionUtility;
import com.iotplatform.validations.RequestValidation;

import oracle.spatial.rdf.client.jena.Oracle;

@Service("GroupService")
public class GroupService {

	private RequestValidation requestValidation;
	private ApplicationDao applicationDao;
	private GroupDao groupDao;
	private Group groupClass;

	@Autowired
	public GroupService(RequestValidation requestValidation, ApplicationDao applicationDao, GroupDao groupDao,
			Group groupClass) {

		this.requestValidation = requestValidation;
		this.applicationDao = applicationDao;
		this.groupDao = groupDao;
		this.groupClass = groupClass;
	}

	/*
	 * insertGroup method is a service method that is responsible to take
	 * property values key pairs and call request validation to validate the
	 * request content then if it pass the validations call the group data
	 * access object to insert the new group
	 */

	public Hashtable<String, Object> insertGroup(Hashtable<String, Object> htblPropValue, String applicationNameCode) {

		long startTime = System.currentTimeMillis();

		/*
		 * check if the model exist or not .
		 */

		boolean exist = applicationDao.checkIfApplicationModelExsist(applicationNameCode);
		if (!exist) {
			NoApplicationModelException exception = new NoApplicationModelException(applicationNameCode, "Group");
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return exception.getExceptionHashTable(timeTaken);
		}

		/*
		 * Check if the request is valid or not
		 */

		try {

			ArrayList<PropertyValue> prefixedPropertyValue = requestValidation.isRequestValid(applicationNameCode,
					groupClass, htblPropValue);

			String applicationModelName = applicationDao.getHtblApplicationNameModelName().get(applicationNameCode);

			String groupName = htblPropValue.get("name").toString();

			groupDao.insertGroup(prefixedPropertyValue, applicationModelName, groupName);

			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			SuccessfullInsertionModel successModel = new SuccessfullInsertionModel("Group", timeTaken);
			return successModel.getResponseJson();

		} catch (ErrorObjException ex) {
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return ex.getExceptionHashTable(timeTaken);

		}
	}

	/*
	 * getGroups method check if the application model is correct then it calls
	 * groupDao to get all groups of this application
	 */
	public Hashtable<String, Object> getGroups(String applicationNameCode) {

		long startTime = System.currentTimeMillis();
		boolean exist = applicationDao.checkIfApplicationModelExsist(applicationNameCode);

		/*
		 * check if the model exist or not .
		 */

		if (!exist) {
			NoApplicationModelException exception = new NoApplicationModelException(applicationNameCode, "Grooup");
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return new SuccessfullSelectAllJsonModel(exception.getExceptionHashTable(timeTaken)).getJson();
		}

		try {

			List<Hashtable<String, Object>> htblPropValue = groupDao
					.getGroups(applicationDao.getHtblApplicationNameModelName().get(applicationNameCode));

			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return new SuccessfullSelectAllJsonModel(htblPropValue, timeTaken).getJson();

		} catch (ErrorObjException e) {
			double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
			return new SuccessfullSelectAllJsonModel(e.getExceptionHashTable(timeTaken)).getJson();

		}
	}

	public static void main(String[] args) {
		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		String szUser = "rdfusr";
		String szPasswd = "rdfusr";
		String szJdbcDriver = "oracle.jdbc.driver.OracleDriver";

		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(szJdbcDriver);
		dataSource.setUrl(szJdbcURL);
		dataSource.setUsername(szUser);
		dataSource.setPassword(szPasswd);

		Oracle oracle = new Oracle(szJdbcURL, szUser, szPasswd);

		DynamicConceptDao dynamicConceptDao = new DynamicConceptDao(dataSource);

		ValidationDao validationDao = new ValidationDao(oracle);

		Group groupClass = new Group();

		RequestValidation requestValidation = new RequestValidation(validationDao, dynamicConceptDao);

		GroupDao groupDao = new GroupDao(oracle, new SelectionUtility(requestValidation), groupClass);

		Hashtable<String, Object> htblPropValue = new Hashtable<>();
		htblPropValue.put("name", "Developers Group");
		htblPropValue.put("description", "Developers Group is a group for all developers in the application");

		ArrayList<Object> members = new ArrayList<>();
		members.add("OmarTag");
		members.add("HatemMorgan");

		htblPropValue.put("member", members);

		GroupService groupService = new GroupService(requestValidation, new ApplicationDao(oracle, new Application()),
				groupDao, groupClass);

		Hashtable<String, Object> groups = groupService.getGroups("TESTAPPLICATION");
		System.out.println(groups);

//		Hashtable<String, Object> res = groupService.insertGroup(htblPropValue, "TESTAPPLICATION");

//		Hashtable<String, Object>[] json = (Hashtable<String, Object>[]) res.get("errors");
//		System.out.println(json[0].toString());

//		 System.out.println(res.toString());
	}

}
