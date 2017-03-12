package com.iotplatform.validations;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.daos.DynamicConceptDao;
import com.iotplatform.daos.ValidationDao;
import com.iotplatform.exceptions.ErrorObjException;
import com.iotplatform.models.DynamicConceptModel;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.DynamicConceptColumns;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.PropertyType;
import com.iotplatform.ontology.classes.ActuatingDevice;
import com.iotplatform.ontology.classes.Admin;
import com.iotplatform.ontology.classes.Amount;
import com.iotplatform.ontology.classes.Application;
import com.iotplatform.ontology.classes.Developer;
import com.iotplatform.ontology.classes.NormalUser;
import com.iotplatform.ontology.classes.OperatingRange;
import com.iotplatform.ontology.classes.Person;
import com.iotplatform.ontology.classes.Point;
import com.iotplatform.ontology.classes.Service;
import com.iotplatform.ontology.classes.SurvivalRange;
import com.iotplatform.utilities.InsertionUtility;
import com.iotplatform.utilities.PropertyValue;
import com.iotplatform.utilities.SqlCondition;

/*
 * MultipleClassRequestValidation class is used to validate multiple Class request
 * (A request that consists of more than one object)
 * 
 * eg. ActuatingDevice,CommunicatingDevice,Sensor
 */

@Component
public class MultipleClassRequestValidation {

	private ValidationDao validationDao;
	private DynamicConceptDao dynamicConceptDao;

	@Autowired
	public MultipleClassRequestValidation(ValidationDao validationDao, DynamicConceptDao dynamicConceptDao) {
		this.validationDao = validationDao;
		this.dynamicConceptDao = dynamicConceptDao;
	}

	/*
	 * it loads dynamic properties of the given classsList in the passed
	 * application domain and also caches the dynamic properties to improve
	 * performance
	 */
	public Hashtable<String, DynamicConceptModel> getDynamicProperties(String applicationName,
			ArrayList<Class> classList) {

		/*
		 * to get the dynamic properties only one time
		 */

		ArrayList<SqlCondition> orCondtionsFilterList = new ArrayList<>();

		for (Class subjectClass : classList) {
			orCondtionsFilterList
					.add(new SqlCondition(DynamicConceptColumns.CLASS_URI.toString(), subjectClass.getUri()));

			for (Class superClass : subjectClass.getSuperClassesList()) {
				orCondtionsFilterList
						.add(new SqlCondition(DynamicConceptColumns.CLASS_URI.toString(), superClass.getUri()));

			}
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

			String className = dynamicProperty.getClass_name();

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
								getXSDDataTypeEnum(dynamicProperty.getProperty_object_type()), applicationName,
								dynamicProperty.getHasMultipleValues(), dynamicProperty.getIsUnique()));
			} else {
				if (dynamicProperty.getProperty_type().equals(PropertyType.ObjectProperty.toString())) {
					subjectClass.getProperties().put(dynamicProperty.getProperty_name(),
							new ObjectProperty(dynamicProperty.getProperty_name(),
									getPrefix(dynamicProperty.getProperty_prefix_alias()),
									getClassByName(dynamicProperty.getProperty_object_type()), applicationName,
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
	 * 
	 * This method takes a hashtable of fields and values . the values may be
	 * single value or an Arraylist or an object so this method will reconstruct
	 * the request body into the appropriate classes and check for the validity
	 * of each field(by checking if maps to a valid property or not)
	 */
	private Hashtable<String, Property> isFieldsValid(String applicationName, ArrayList<Class> classesList,
			Hashtable<String, Object> htblPropertyValue) {

		Hashtable<String, Property> htbClassesAllProperties = new Hashtable<>();

		/*
		 * get all properties from all classes in classList and put them into
		 * one hashtable
		 */

		for (Class subjectClass : classesList) {
			Hashtable<String, Property> subjectClassProperties = subjectClass.getProperties();
			htbClassesAllProperties.putAll(subjectClassProperties);
		}

		Iterator<String> htblPropertyValueIterator = htblPropertyValue.keySet().iterator();

		while (htblPropertyValueIterator.hasNext()) {
			String propertyName = htblPropertyValueIterator.next();
			Object value = htblPropertyValue.get(propertyName);

			/*
			 * check if the property not exist in the htbClassesAllProperties
			 * hashtable . If it is not exist then call getDynamicProperties
			 * method
			 */

			if (!htbClassesAllProperties.containsKey(propertyName)) {

			}

		}

		return null;

	}

	public ArrayList<PropertyValue> isRequestValid(String applicationName, Class subjectClass,
			Hashtable<String, Object> htblPropertyValue) {
		return null;
		// Hashtable<Object, Object> htblPropValue =
		// isFieldsValid(applicationName, subjectClass, htblPropertyValue);
		// ArrayList<PropertyValue> propertyValueList =
		// InsertionUtility.constructPropValueList(htblPropValue);
		// return isProrpertyValueValid(propertyValueList, subjectClass,
		// applicationName);

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
		case "ActuatingDevice": return new ActuatingDevice();	
		case "Agent" : return new Age
		case "Amount" : return new Amount();
		
		}

		return null;
	}

	public static void main(String[] args) {
		ArrayList<Class> classesList = new ArrayList<>();

		classesList.add(new ActuatingDevice());
		classesList.add(new Service());
		classesList.add(new OperatingRange());
		classesList.add(new SurvivalRange());

		// MultipleClassRequestValidation multipleClassRequestValidation = new
		// MultipleClassRequestValidation();
		//
		// System.out.println(multipleClassRequestValidation.isFieldsValid(null,
		// classesList, null));
	}

}
