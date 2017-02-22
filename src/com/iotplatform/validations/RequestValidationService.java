package com.iotplatform.validations;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.daos.ValidationDao;
import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.XSDDataTypes;

@Component
public class RequestValidationService {

	private ValidationDao validationDao;

	@Autowired
	public RequestValidationService(ValidationDao validationDao) {
		System.out.println("Request Validation Bean Created");
		this.validationDao = validationDao;
	}

	/*
	 * checkIfFieldsValid checks if the fields passed by the http request are
	 * valid or not
	 */
	public boolean isFieldsValid(Class ontologyClass, Hashtable<String, Object> fields) {

		Hashtable<String, Property> htblProperties = ontologyClass.getProperties();
		Iterator<String> iterator = fields.keySet().iterator();

		while (iterator.hasNext()) {
			String field = iterator.next();
			if (!htblProperties.containsKey(field)) {
				return false;
			}
		}

		return true;
	}

	/*
	 * isPropertiesValid method prepare the inputs for isDataValid method that
	 * checks the validity of a data type property and for
	 * isObjectValuePropertyValid that check the validity of a object type
	 * property
	 */
	public boolean isPropertiesValid(String applicationName, Class subjectClass,
			Hashtable<String, Object> htblPropertyValue) {

		Hashtable<Class, Object> htblClassValue = new Hashtable<>();
		Hashtable<String, Property> htblProperties = subjectClass.getProperties();

		Iterator<String> iterator = htblPropertyValue.keySet().iterator();
		while (iterator.hasNext()) {
			String propertyName = iterator.next();
			Property property = htblProperties.get(propertyName);
			Object value = htblPropertyValue.get(propertyName);

			if (property instanceof DataTypeProperty) {
				DataTypeProperty targetDataProperty = (DataTypeProperty) property;

				if (!isDataValueValid(targetDataProperty, value)) {
					return false;
				}

			} else {
				ObjectProperty objectProperty = (ObjectProperty) property;
				Class ValueClassType = objectProperty.getObject();
				htblClassValue.put(ValueClassType, value);
			}
		}

		boolean isValid = true;
		if (htblClassValue.size() > 0) {
			isValid = isObjectValuePropertyValid(applicationName, htblClassValue);
		}

		return isValid;
	}

	/*
	 * getPrefixedProperties method is used to return a prefixed properties to
	 * the service to be used by the data access object (dao)
	 * 
	 */
	public Hashtable<String, Object> getPrefixedProperties(Hashtable<String, Object> htblPropValue, Class targetClass) {
		Iterator<String> iterator = htblPropValue.keySet().iterator();
		Hashtable<String, Property> targetClassProperty = targetClass.getProperties();
		Hashtable<String, Object> returnedPrefixProperties = new Hashtable<>();
		while (iterator.hasNext()) {
			String propertyName = iterator.next();
			Object value = htblPropValue.get(propertyName);
			Property property = targetClassProperty.get(propertyName);
			Prefixes prefix = property.getPrefix();
			String alias;

			/*
			 * iot-lite and iot-platform prefixes does not have the alias that
			 * is correct so I must change _ to - in order to have a correct
			 * auto query construction
			 */
			switch (prefix) {
			case IOT_LITE:
				alias = "iot-lite:";
				break;
			case IOT_PLATFORM:
				alias = "iot-platform:";
				break;
			default:
				alias = prefix.getPrefix().toLowerCase();
			}
			if(property instanceof DataTypeProperty){
				XSDDataTypes xsdDataType = ((DataTypeProperty) property).getDataType();
				value = "\""+value.toString()+"\""+ xsdDataType.getXsdType();
			}
			
			
			returnedPrefixProperties.put(alias+ propertyName, value);
		}

		return returnedPrefixProperties;
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

	// public static void main(String[] args) {
	// String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
	// String szUser = "rdfusr";
	// String szPasswd = "rdfusr";
	//
	// ValidationDao validationDao = new ValidationDao(new Oracle(szJdbcURL,
	// szUser, szPasswd));
	//
	// RequestValidation requestValidation = new
	// RequestValidation(validationDao);
	//
	// // test if fields(properties name) are valid properties
	// // ArrayList<String> fields = new ArrayList<>();
	// // fields.add("firstName");
	// // fields.add("knows");
	// // /*
	// // * this will fail the check make developedApplication to pass the
	// // check
	// // */
	// // fields.add("adminOf");
	// // System.out.println(requestValidation.isFieldsValid(new Developer(),
	// // fields));
	//
	// Hashtable<String, Object> htblPropValues = new Hashtable<>();
	// htblPropValues.put("name", "Test Application");
	//
	// System.out.println(requestValidation.isPropertiesValid("testApp", new
	// Application(), htblPropValues));
	//
	// }
}
