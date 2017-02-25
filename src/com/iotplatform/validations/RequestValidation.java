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
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.exceptions.InvalidPropertyValuesException;
import com.iotplatform.exceptions.InvalidRequestFieldsException;
import com.iotplatform.models.DynamicConceptModel;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.DynamicConceptColumns;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.PropertyType;
import com.iotplatform.ontology.XSDDataTypes;
import com.iotplatform.ontology.classes.Admin;
import com.iotplatform.ontology.classes.Application;
import com.iotplatform.ontology.classes.Developer;
import com.iotplatform.ontology.classes.NormalUser;
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
	private Hashtable<Object, Object>[] isFieldsValid(String applicationName, Class subjectClass,
			Hashtable<String, Object> htblPropertyValue) {

		Hashtable<Object, Object>[] returnedArray = (Hashtable<Object, Object>[]) new Hashtable<?, ?>[2];
		Hashtable<Object, Object> htblstaticProperties = new Hashtable<>();
		Hashtable<Object, Object> htbldynamicProperties = new Hashtable<>();

		Hashtable<String, DynamicConceptModel> dynamicProperties = null;

		Hashtable<String, Property> htblProperties = subjectClass.getProperties();
		Iterator<String> iterator = htblPropertyValue.keySet().iterator();

		while (iterator.hasNext()) {
			String field = iterator.next();
			Object value = htblPropertyValue.get(field);

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
					 * 
					 * Also add dynamic property to property list of the subject
					 * class in order to improve performance by caching the
					 * dynamic properties
					 */
					dynamicProperties = new Hashtable<>();
					for (DynamicConceptModel dynamicProperty : res) {
						subjectClass.getHtblPropUriName().put(dynamicProperty.getProperty_uri(),
								dynamicProperty.getProperty_name());

						if (dynamicProperty.getProperty_type().equals(PropertyType.DatatypeProperty)) {

							subjectClass.getProperties().put(dynamicProperty.getProperty_name(),
									new DataTypeProperty(dynamicProperty.getProperty_name(),
											getPrefix(dynamicProperty.getProperty_prefix_alias()),
											getXSDDataTypeEnum(dynamicProperty.getProperty_object_type())));
						} else {
							if (dynamicProperty.getProperty_type().equals(PropertyType.DatatypeProperty)) {
								subjectClass.getProperties().put(dynamicProperty.getProperty_name(),
										new ObjectProperty(dynamicProperty.getProperty_name(),
												getPrefix(dynamicProperty.getProperty_prefix_alias()),
												getClassByName(dynamicProperty.getProperty_object_type())));
							}
						}

						dynamicProperties.put(dynamicProperty.getProperty_name(), dynamicProperty);
					}

				}

				/*
				 * check if the field passed is a dynamic property
				 */

				if (!dynamicProperties.containsKey(field)) {

					throw new InvalidRequestFieldsException(subjectClass.getName(), field);

				} else {

					/*
					 * passed field is a static property so add it to
					 * htblDynamicProperties
					 */
					htbldynamicProperties.put(dynamicProperties.get(field), value);
				}

			} else {
				/*
				 * passed field is a static property so add it to
				 * htblStaticProperty
				 */

				htblstaticProperties.put(htblProperties.get(field), value);
			}
		}

		returnedArray[0] = htblstaticProperties;
		returnedArray[1] = htbldynamicProperties;
		return returnedArray;
	}

	private Prefixes getPrefix(String prefixAlias) {

		if (Prefixes.FOAF.getPrefix().equals(prefixAlias)) {
			return Prefixes.FOAF;
		}

		if (Prefixes.SSN.getPrefix().equals(prefixAlias)) {
			return Prefixes.SSN;
		}

		if (Prefixes.IOT_LITE.getPrefix().equals(prefixAlias)) {
			return Prefixes.IOT_LITE;
		}

		if (Prefixes.IOT_PLATFORM.getPrefix().equals(prefixAlias)) {
			return Prefixes.IOT_PLATFORM;
		}

		if (Prefixes.GEO.getPrefix().equals(prefixAlias)) {
			return Prefixes.GEO;
		}

		if (Prefixes.XSD.getPrefix().equals(prefixAlias)) {
			return Prefixes.XSD;
		}

		if (Prefixes.OWL.getPrefix().equals(prefixAlias)) {
			return Prefixes.OWL;
		}

		if (Prefixes.RDFS.getPrefix().equals(prefixAlias)) {
			return Prefixes.RDFS;
		}

		if (Prefixes.RDF.getPrefix().equals(prefixAlias)) {
			return Prefixes.RDF;
		}

		return null;
	}

	/*
	 * isStaticDataValueValid checks that the datatype of the values passed with
	 * the property are valid to maintain data integrity and consistency.
	 * 
	 * It is used with static dataProperty
	 */
	private boolean isStaticDataValueValid(DataTypeProperty dataProperty, Object value) {

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
	 * isDynamicDataValueValid checks that the datatype of the values passed
	 * with the property are valid to maintain data integrity and consistency.
	 * 
	 * It is used with Dynamic dataProperty
	 */
	private boolean isDynamicDataValueValid(String dataType, Object value) {

		boolean checkFlag;

		if (XSDDataTypes.boolean_type.getDataType().equals(dataType)) {
			return checkFlag = (value instanceof Boolean) ? true : false;
		}

		if (XSDDataTypes.decimal_typed.getDataType().equals(dataType)) {
			return checkFlag = (value instanceof Double) ? true : false;
		}

		if (XSDDataTypes.float_typed.getDataType().equals(dataType)) {
			return checkFlag = (value instanceof Float) ? true : false;
		}

		if (XSDDataTypes.integer_typed.getDataType().equals(dataType)) {
			return checkFlag = (value instanceof Integer) ? true : false;
		}

		if (XSDDataTypes.string_typed.getDataType().equals(dataType)) {
			return checkFlag = (value instanceof String) ? true : false;
		}

		if (XSDDataTypes.dateTime_typed.getDataType().equals(dataType)) {
			return checkFlag = (value instanceof XMLGregorianCalendar) ? true : false;
		}

		if (XSDDataTypes.double_typed.getDataType().equals(dataType)) {
			return checkFlag = (value instanceof Double) ? true : false;
		}

		return false;

	}

	/*
	 * getXSDDataTypeString method returns the xsd datatype string
	 */

	private String getXSDDataTypeString(String dataType) {

		if (XSDDataTypes.boolean_type.getDataType().equals(dataType)) {
			return XSDDataTypes.boolean_type.getXsdType();
		}

		if (XSDDataTypes.decimal_typed.getDataType().equals(dataType)) {
			return XSDDataTypes.decimal_typed.getXsdType();
		}

		if (XSDDataTypes.float_typed.getDataType().equals(dataType)) {
			return XSDDataTypes.float_typed.getXsdType();
		}

		if (XSDDataTypes.integer_typed.getDataType().equals(dataType)) {
			return XSDDataTypes.integer_typed.getXsdType();
		}

		if (XSDDataTypes.string_typed.getDataType().equals(dataType)) {
			return XSDDataTypes.string_typed.getXsdType();
		}

		if (XSDDataTypes.dateTime_typed.getDataType().equals(dataType)) {
			return XSDDataTypes.dateTime_typed.getXsdType();
		}

		if (XSDDataTypes.double_typed.getDataType().equals(dataType)) {
			return XSDDataTypes.double_typed.getXsdType();
		}

		return null;
	}

	/*
	 * getXSDDataTypeEnum return XsdDataType enum instance
	 */
	private XSDDataTypes getXSDDataTypeEnum(String dataType) {

		if (XSDDataTypes.boolean_type.getDataType().equals(dataType)) {
			return XSDDataTypes.boolean_type;
		}

		if (XSDDataTypes.decimal_typed.getDataType().equals(dataType)) {
			return XSDDataTypes.decimal_typed;
		}

		if (XSDDataTypes.float_typed.getDataType().equals(dataType)) {
			return XSDDataTypes.float_typed;
		}

		if (XSDDataTypes.integer_typed.getDataType().equals(dataType)) {
			return XSDDataTypes.integer_typed;
		}

		if (XSDDataTypes.string_typed.getDataType().equals(dataType)) {
			return XSDDataTypes.string_typed;
		}

		if (XSDDataTypes.dateTime_typed.getDataType().equals(dataType)) {
			return XSDDataTypes.dateTime_typed;
		}

		if (XSDDataTypes.double_typed.getDataType().equals(dataType)) {
			return XSDDataTypes.double_typed;
		}

		return null;
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

	private Class getClassByName(String name) {

		switch (name) {
		case "Application":
			return new Application();
		case "Person":
			return new Person();
		case "Admin":
			return new Admin();
		case "Developer":
			return new Developer();
		case "NormalUser":
			return new NormalUser();
		}

		return null;
	}

	private String getPropertyPrefixAlias(Property property) {

		Prefixes prefix = property.getPrefix();
		String alias;

		/*
		 * iot-lite and iot-platform prefixes does not have the alias that is
		 * correct so I must change _ to - in order to have a correct auto query
		 * construction
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

		return alias;
	}

	private Object getValue(Property property, Object value) {

		if (property instanceof DataTypeProperty) {
			XSDDataTypes xsdDataType = ((DataTypeProperty) property).getDataType();
			value = "\"" + value.toString() + "\"" + xsdDataType.getXsdType();
			return value;
		} else {
			Class objectClassType = ((ObjectProperty) property).getObject();
			return objectClassType.getPrefix().getPrefix() + value;
		}
	}

	private Hashtable<String, Object> isProrpertyValueValid(Hashtable<Object, Object> htblDynamicProperties,
			Hashtable<Object, Object> htblStaticProperties, Class subjectClass, String applicationName) {

		Iterator<Object> staticProertyIterator = htblStaticProperties.keySet().iterator();
		Iterator<Object> dynamicPropertyIterator = htblDynamicProperties.keySet().iterator();

		Hashtable<Class, Object> htblClassValue = new Hashtable<>();
		Hashtable<String, Object> htblPrefixedPropertyValues = new Hashtable<>();

		/*
		 * Static properties
		 */

		while (staticProertyIterator.hasNext()) {

			Property staticProperty = (Property) staticProertyIterator.next();
			Object value = htblStaticProperties.get(staticProperty);

			/*
			 * Object property so add it to htblClassValue to send it to
			 * requestValidationDao
			 */
			if (staticProperty instanceof ObjectProperty) {

				htblClassValue.put(((ObjectProperty) staticProperty).getObject(), value);

			} else {

				/*
				 * check if the datatype is correct or not fir static
				 * dataProperty
				 */

				if (!isStaticDataValueValid((DataTypeProperty) staticProperty, value)) {

					throw new InvalidPropertyValuesException(subjectClass.getName(), staticProperty.getName());
				}

			}

			htblPrefixedPropertyValues.put(getPropertyPrefixAlias(staticProperty) + staticProperty.getName(),
					getValue(staticProperty, value));

		}

		/*
		 * Dynamic Properties
		 */
		while (dynamicPropertyIterator.hasNext()) {

			DynamicConceptModel dynamicProperty = (DynamicConceptModel) dynamicPropertyIterator.next();
			Object value = htblDynamicProperties.get(dynamicProperty);

			/*
			 * Object property so add it to htblClassValue to send it to
			 * requestValidationDao
			 */
			if (dynamicProperty.getProperty_type().equals(PropertyType.ObjectProperty.toString())) {

				Class objectClassType = getClassByName(dynamicProperty.getClass_name());
				htblClassValue.put(objectClassType, value);
				value = objectClassType.getPrefix().getPrefix() + value;
			} else {

				/*
				 * check if the datatype is correct or not fir dynamic
				 * dataProperty
				 */

				if (!isDynamicDataValueValid(dynamicProperty.getProperty_object_type(), value)) {
					throw new InvalidPropertyValuesException(subjectClass.getName(),
							dynamicProperty.getProperty_name());
				}

			}
			value = "\"" + value.toString() + "\"" + getXSDDataTypeString(dynamicProperty.getProperty_object_type());
			htblPrefixedPropertyValues
					.put(dynamicProperty.getProperty_prefix_alias() + dynamicProperty.getProperty_name(), value);

		}

		/*
		 * check for property value type of objectProperties
		 */

		if (htblClassValue.size() > 0) {
			boolean isValid = isObjectValuePropertyValid(applicationName, htblClassValue);

			if (!isValid) {
				throw new InvalidPropertyValuesException(subjectClass.getName());
			}

		}

		return htblPrefixedPropertyValues;

	}

	public Hashtable<String, Object> isRequestValid(String applicationName, Class subjectClass,
			Hashtable<String, Object> htblPropertyValue) {

		Hashtable<Object, Object>[] results = isFieldsValid(applicationName, subjectClass, htblPropertyValue);
		return isProrpertyValueValid(results[1], results[0], subjectClass, applicationName);

	}

	// public static void main(String[] args) {
	// String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
	// String szUser = "rdfusr";
	// String szPasswd = "rdfusr";
	// String szJdbcDriver = "oracle.jdbc.driver.OracleDriver";
	//
	// BasicDataSource dataSource = new BasicDataSource();
	// dataSource.setDriverClassName(szJdbcDriver);
	// dataSource.setUrl(szJdbcURL);
	// dataSource.setUsername(szUser);
	// dataSource.setPassword(szPasswd);
	//
	// DynamicConceptDao dynamicConceptDao = new DynamicConceptDao(dataSource);
	// ValidationDao validationDao = new ValidationDao(new Oracle(szJdbcURL,
	// szUser, szPasswd));
	//
	// System.out.println("Connected to Database");
	//
	// RequestValidation requestValidation = new
	// RequestValidation(validationDao, dynamicConceptDao);
	//
	// Hashtable<String, Object> htblPropValues = new Hashtable<>();
	// htblPropValues.put("firstName", "Hatem");
	// htblPropValues.put("knows", "Hatem");
	// htblPropValues.put("hates", "Hatem");
	//
	// long startTime = System.currentTimeMillis();
	//
	// //testing isValidFields method
	// Hashtable<Object, Object>[] res = requestValidation.isFieldsValid("test
	// Application", new Person(),
	// htblPropValues);
	// System.out.println(res[0].size());
	// System.out.println(res[0].toString());
	// System.out.println(res[1].size());
	// System.out.println(res[1].toString());
	//
	// //testing isValidRequest
	// try{
	// Hashtable<String, Object> htblPrefixedPropValue =
	// requestValidation.isRequestValid("test Application", new Person(),
	// htblPropValues);
	// System.out.println(htblPrefixedPropValue.toString());
	// }catch(ErrorObjException ex){
	// System.out.println(((Hashtable<Object,
	// Object>[])ex.getExceptionHashTable().get("errors"))[0].toString());
	// }
	// double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
	// System.out.println("Time taken : " + timeTaken + " sec ");
	// }
}
