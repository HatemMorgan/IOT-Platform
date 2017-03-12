package com.iotplatform.validations;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.daos.DynamicConceptDao;
import com.iotplatform.daos.ValidationDao;
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
import com.iotplatform.utilities.InsertionUtility;
import com.iotplatform.utilities.PropertyValue;
import com.iotplatform.utilities.SqlCondition;
import com.iotplatform.utilities.ValueOfTypeClass;

import oracle.spatial.rdf.client.jena.Oracle;

/*
 * SingleClassRequestValidation class is responsible to validate a single entity(class) request 
 * eg. Admin,Developer,Application,Organization
 */

@Component
public class SingleClassRequestValidation {

	private ValidationDao validationDao;
	private DynamicConceptDao dynamicConceptDao;

	@Autowired
	public SingleClassRequestValidation(ValidationDao validationDao, DynamicConceptDao dynamicConceptDao) {

		this.validationDao = validationDao;
		this.dynamicConceptDao = dynamicConceptDao;
	}

	/*
	 * it loads dynamic properties of the given class in the passed application
	 * domain and also caches the dynamic properties to improve performance
	 */
	public Hashtable<String, DynamicConceptModel> getDynamicProperties(String applicationName, Class subjectClass) {

		/*
		 * to get the dynamic properties only one time
		 */

		ArrayList<SqlCondition> orCondtionsFilterList = new ArrayList<>();
		orCondtionsFilterList.add(new SqlCondition(DynamicConceptColumns.CLASS_URI.toString(), subjectClass.getUri()));

		for (Class superClass : subjectClass.getSuperClassesList()) {
			orCondtionsFilterList
					.add(new SqlCondition(DynamicConceptColumns.CLASS_URI.toString(), superClass.getUri()));

		}

		List<DynamicConceptModel> res;
		try {
			res = dynamicConceptDao.getConceptsOfApplicationByFilters(applicationName, null, orCondtionsFilterList);
		} catch (ErrorObjException ex) {
			throw ex;
		}

		/*
		 * populate dynamicProperties hashtable to enhance performance when
		 * having alot of dynamic properties not cached so to avoid going to the
		 * database more than one time. I load all the dynamic properties of the
		 * specified subject and then cache results
		 * 
		 * I used hashtable to hold dynamic properties to enhance performance
		 * when checking for valid property in the loading dynamic properties. I
		 * did not check in the loop to allow caching all new properties that
		 * were not cached before without terminating the loop when finding the
		 * property needed in the dynamicProperties
		 * 
		 * Also add dynamic property to property list of the subject class in
		 * order to improve performance by caching the dynamic properties
		 */
		Hashtable<String, DynamicConceptModel> dynamicProperties = new Hashtable<>();

		applicationName = applicationName.replaceAll(" ", "").toUpperCase();

		for (DynamicConceptModel dynamicProperty : res) {

			// skip if the property was cached before
			if (subjectClass.getProperties().contains(dynamicProperty.getProperty_name())) {
				continue;
			}
			// System.out.println(dynamicProperty.getProperty_uri());

			subjectClass.getHtblPropUriName().put(dynamicProperty.getProperty_uri(),
					dynamicProperty.getProperty_name());

			if (dynamicProperty.getProperty_type().equals(PropertyType.DatatypeProperty.toString())) {
				subjectClass.getProperties().put(dynamicProperty.getProperty_name(),
						new DataTypeProperty(dynamicProperty.getProperty_name(),
								getPrefix(dynamicProperty.getProperty_prefix_alias()),
								getXSDDataTypeEnum(dynamicProperty.getProperty_object_type_uri()), applicationName,
								dynamicProperty.getHasMultipleValues(), dynamicProperty.getIsUnique()));
			} else {
				if (dynamicProperty.getProperty_type().equals(PropertyType.ObjectProperty.toString())) {
					subjectClass.getProperties().put(dynamicProperty.getProperty_name(),
							new ObjectProperty(dynamicProperty.getProperty_name(),
									getPrefix(dynamicProperty.getProperty_prefix_alias()),
									getClassByName(dynamicProperty.getProperty_object_type_uri()), applicationName,
									dynamicProperty.getHasMultipleValues(), dynamicProperty.getIsUnique()));
				}
			}
			dynamicProperties.put(dynamicProperty.getProperty_name(), dynamicProperty);
		}

		return dynamicProperties;
	}

	/*
	 * isFieldsValid checks if the fields passed by the http request are valid
	 * or not and it returns a hashtable of propertyValue keyValue
	 */
	private Hashtable<Object, Object> isFieldsValid(String applicationName, Class subjectClass,
			Hashtable<String, Object> htblPropertyValue) {

		Hashtable<Object, Object> htblFieldPropValue = new Hashtable<>();

		Hashtable<String, DynamicConceptModel> dynamicProperties;

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
				 * get Dynamic properties of given subject class in the passed
				 * application domain
				 */

				dynamicProperties = getDynamicProperties(applicationName, subjectClass);

				/*
				 * check if the field passed is a dynamic property
				 */

				if (!dynamicProperties.containsKey(field)) {

					throw new InvalidRequestFieldsException(subjectClass.getName(), field);

				} else {

					/*
					 * passed field is a dynamic property and it is cached so it
					 * will be availabe in the subject class properties list so
					 * add it to the returned hashtable of property and value
					 */
					htblFieldPropValue.put(htblProperties.get(field), value);
				}

			} else {

				/*
				 * passed field is a static property so add it to
				 * htblStaticProperty so check that the property is valid for
				 * this application domain
				 * 
				 * if the applicationName is null so this field maps a property
				 * in the main ontology .
				 * 
				 * if the applicationName is equal to passed applicationName so
				 * it is a dynamic added property to this application domain
				 * 
				 * else it will be a dynamic property in another application
				 * domain which will happen rarely
				 */

				if (htblProperties.get(field).getApplicationName() == null || htblProperties.get(field)
						.getApplicationName().equals(applicationName.replace(" ", "").toUpperCase())) {
					htblFieldPropValue.put(htblProperties.get(field), value);

				} else {

					/*
					 * this means that this class has a property with the same
					 * name but it is not for the specified application domain
					 */

					throw new InvalidRequestFieldsException(subjectClass.getName(), field);

				}

			}
		}

		return htblFieldPropValue;
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
	 * isDataValueValid checks that the datatype of the values passed with the
	 * property are valid to maintain data integrity and consistency.
	 * 
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
	 * is an instance with the specified name and type and also it checks if the
	 * values do not violate any unique constraints
	 */
	// private boolean isObjectValuePropertyValid(String applicationName,
	// ArrayList<ValueOfTypeClass> classValueList,)
	// throws DatabaseException {
	//
	// int result = validationDao.checkIfInstanceExsist(applicationName,
	// classValueList);
	// boolean found = (result == 1) ? true : false;
	// return found;
	//
	// }

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
			return Prefixes.IOT_PLATFORM.getPrefix() + value;
		}
	}

	private ArrayList<PropertyValue> isProrpertyValueValid(ArrayList<PropertyValue> propertyValueList,
			Class subjectClass, String applicationName) throws ErrorObjException {

		ArrayList<ValueOfTypeClass> classValueList = new ArrayList<>();
		ArrayList<PropertyValue> uniquePropValueList = new ArrayList<>();

		ArrayList<PropertyValue> prefixedPropertyValueList = new ArrayList<>();

		Hashtable<String, Property> htbProperties = subjectClass.getProperties();

		for (PropertyValue propertyValue : propertyValueList) {

			Property property = htbProperties.get(propertyValue.getPropertyName());
			Object value = propertyValue.getValue();

			/*
			 * Object property so add it to htblClassValue to send it to
			 * requestValidationDao
			 */
			if (property instanceof ObjectProperty) {
				classValueList.add(new ValueOfTypeClass(((ObjectProperty) property).getObject(), value));

			} else {

				/*
				 * check if the datatype is correct or not
				 */

				if (!isDataValueValid((DataTypeProperty) property, value)) {

					throw new InvalidPropertyValuesException(subjectClass.getName(), property.getName());
				}

			}

			/*
			 * check if the property has unique constraint to add the value and
			 * the property to uniquePropValueList to be passed to validationDao
			 * to check the no unique constraint violation occured
			 */

			if (property.isUnique()) {
				uniquePropValueList
						.add(new PropertyValue(property.getPrefix().getPrefix() + property.getName(), value));
			}

			/*
			 * add prefix to the property inOrder to pass it for quering
			 */

			propertyValue.setPropertyName(getPropertyPrefixAlias(property) + property.getName());
			propertyValue.setValue(getValue(property, value.toString().replaceAll(" ", "").toLowerCase()));

			prefixedPropertyValueList.add(propertyValue);
		}

		/*
		 * check for property value type of objectProperties
		 */

		if (classValueList.size() > 0 || uniquePropValueList.size() > 0) {

			/*
			 * check if there are any constraints violations if there are any
			 * violations hasConstraintViolations method will throw the
			 * appropriate error that describes the type of the violation
			 * 
			 * if there is no constraints violations a boolean true will be
			 * returned
			 */

			if (validationDao.hasConstraintViolations(applicationName, classValueList, uniquePropValueList,
					subjectClass)) {
				return prefixedPropertyValueList;
			}

		}

		return prefixedPropertyValueList;

	}

	public ArrayList<PropertyValue> isRequestValid(String applicationName, Class subjectClass,
			Hashtable<String, Object> htblPropertyValue) {

		Hashtable<Object, Object> htblPropValue = isFieldsValid(applicationName, subjectClass, htblPropertyValue);
		ArrayList<PropertyValue> propertyValueList = InsertionUtility.constructPropValueList(htblPropValue);
		return isProrpertyValueValid(propertyValueList, subjectClass, applicationName);

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

		System.out.println("Connected to Database");

		SingleClassRequestValidation requestValidation = new SingleClassRequestValidation(validationDao,
				dynamicConceptDao);

		Hashtable<String, Object> htblPropValues = new Hashtable<>();
		htblPropValues.put("firstName", "Hatem");
		htblPropValues.put("knows", "HatemMorgan");
		htblPropValues.put("hates", "HatemMorgan");

		long startTime = System.currentTimeMillis();

		// testing isValidFields method
		Hashtable<Object, Object> res = requestValidation.isFieldsValid("test Application", new Developer(),
				htblPropValues);

		System.out.println(res.toString());

		// // testing isValidRequest
		//
		// Hashtable<String, Object> htblPrefixedPropValue =
		// requestValidation.isRequestValid("test Application",
		// new Developer(), htblPropValues);
		// System.out.println(htblPrefixedPropValue.toString());
		//
		double timeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
		System.out.println("Time taken : " + timeTaken + " sec ");
	}
}
