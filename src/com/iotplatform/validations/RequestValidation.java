package com.iotplatform.validations;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.daos.DynamicConceptDao;
import com.iotplatform.daos.ValidationDao;
import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.exceptions.InvalidRequestFieldsException;
import com.iotplatform.models.DynamicConceptModel;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.DynamicConceptColumns;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.XSDDataTypes;
import com.iotplatform.ontology.classes.Person;

import oracle.spatial.rdf.client.jena.Oracle;

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

		Hashtable<String, DynamicConceptModel> dynamicProperties = null;

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
					htblFilter.put(DynamicConceptColumns.CLASS_URI.toString(), subjectClass.getUri());
					List<DynamicConceptModel> res = dynamicConceptDao.getConceptsOfApplicationByFilters(applicationName,
							htblFilter);

					/*
					 * populate dynamicProperties hashtable to enhance
					 * performance when searching many times because using list
					 * will let me loop on the list each time the field passed
					 * is not a static property
					 */
					dynamicProperties = new Hashtable<>();
					for (DynamicConceptModel dynamicProperty : res) {
						dynamicProperties.put(dynamicProperty.getProperty_name(), dynamicProperty);
					}

				}

				/*
				 * check if the field passed is a dynamic property
				 */

				if (!dynamicProperties.containsKey(field)) {

					throw new InvalidRequestFieldsException(subjectClass.getName());

				} else {

					/*
					 * passed field is a static property so add it to
					 * htblDynamicProperties
					 */
					htbldynamicProperties.put(field, dynamicProperties.get(field));
				}

			} else {
				/*
				 * passed field is a static property so add it to
				 * htblStaticProperty
				 */

				htblstaticProperties.put(field, htblProperties.get(field));
			}
		}

		returnedArray[0] = htblstaticProperties;
		returnedArray[1] = htbldynamicProperties;
		return returnedArray;
	}

	/*
	 * checkIfDatatypesValid checks that the datatype of the values passed with
	 * the property are valid to maintain data integrity and consistency
	 */
	private boolean isDataValueValid(DataTypeProperty dataProperty, Object value) {

		XSDDataTypes xsdDataType = dataProperty.getDataType();
		switch (xsdDataType) {
		case boolean_type:
			if (value instanceof Boolean) {
				return true;
			} else {
				return false;
			}
		case decimal_typed:
			if (value instanceof Double) {
				return true;
			} else {
				return false;
			}
		case float_typed:
			if (value instanceof Float) {
				return true;
			} else {
				return false;
			}
		case integer_typed:
			if (value instanceof Integer) {
				return true;
			} else {
				return false;
			}
		case string_typed:
			if (value instanceof String) {
				return true;
			} else {
				return false;
			}
		case dateTime_typed:
			if (value instanceof XMLGregorianCalendar) {
				return true;
			} else {
				return false;
			}
		case double_typed:
			if (value instanceof Double) {
				return true;
			} else {
				return false;
			}
		default:
			return false;
		}

	}

	/*
	 * isObjectValuePropertyValid method calls the validation data access object
	 * that has the responsibility to query application model and check if there
	 * is an instance with the specified name and type
	 */
	private boolean isObjectValuePropertyValid(String applicationName, Hashtable<Class, Object> htblClassValue)
			throws DatabaseException {

		int result = validationDao.checkIfInstanceExsist(applicationName, htblClassValue);
		boolean found = (result == 1) ? true : false;
		return found;

	}

	
	private Class getClassByName(String name){
		
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

		DynamicConceptDao dynamicConceptDao = new DynamicConceptDao(dataSource);
		ValidationDao validationDao = new ValidationDao(new Oracle(szJdbcURL, szUser, szPasswd));

		RequestValidation requestValidation = new RequestValidation(validationDao, dynamicConceptDao);

		Hashtable<String, Object> htblPropValues = new Hashtable<>();
		htblPropValues.put("firstName", "Hatem");
		htblPropValues.put("job", "Engineer");

		long startTime = System.currentTimeMillis();
		Hashtable<String, Object>[] res = requestValidation.isFieldsValid("test Application", new Person(),
				htblPropValues);
		System.out.println(res[0].size());
		System.out.println(res[0].toString());
		System.out.println(res[1].size());
		System.out.println(res[1].toString());

		double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
		System.out.println("Time taken : " + timeTaken + " sec ");
	}
}
