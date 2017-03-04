package com.iotplatform.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.classes.Person;
import com.iotplatform.utilities.PropertyValue;
import com.iotplatform.utilities.ValueOfTypeClass;

import oracle.spatial.rdf.client.jena.Oracle;

@Component
public class ValidationDao {
	private Oracle oracle;
	private final String suffix = "_MODEL";

	@Autowired
	public ValidationDao(Oracle oracle) {
		this.oracle = oracle;
	}

	/*
	 * checkIfInstanceExsist used to query the passed application model to check
	 * if there are instances of a specified classes passed to it . This
	 * validation is done to make sure that no object property has a value which
	 * is not available to maintain the consistency and integrity of the data.
	 * 
	 * It also check if there is no unique constraint violation
	 */

	public int checkIfInstanceExsist(String applicationName, ArrayList<ValueOfTypeClass> classValueList,
			ArrayList<PropertyValue> uniquePropValueList) {
		String queryString = constructQuery(applicationName, classValueList, uniquePropValueList);
		System.out.println(queryString);
		try {
			ResultSet resultSet = oracle.executeQuery(queryString, 0, 1);
			resultSet.next();
			return resultSet.getInt(1);

		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage(), "Application");

		}
	}

	/*
	 * constructQuery used to construct a sparql query String based on input it
	 * takes the application name to get the application model in order to query
	 * and it takes a hashtable of classes and values to check if there is an
	 * instance of that class with this value exist or not to insure data
	 * integrity and consistency
	 * 
	 * It also add graph patterns to the query to check that no constraint
	 * violations happened
	 * 
	 * The Sparql injected sql query auto constructed query has the same format
	 * like this:
	 * 
	 * select found from table(sem_match('select (count(*) as ?found)
	 * where{iot-platform:testapp a iot-platform:Application
	 * .}',sem_models('TESTAPP_MODEL'),null,SEM_ALIASES(SEM_ALIAS('iot-platform'
	 * ,'http://iot-platform#')),null));
	 */

	private String constructQuery(String applicationName, ArrayList<ValueOfTypeClass> classValueList,
			ArrayList<PropertyValue> uniquePropValueList) {

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("select found from table(sem_match('select (count(*) as ?found) where{");
		StringBuilder prefixStringBuilder = new StringBuilder();

		for (ValueOfTypeClass valueOfTypeClass : classValueList) {
			Class valueClassType = valueOfTypeClass.getTypeClass();
			Object value = valueOfTypeClass.getValue();

			/*
			 * any instance created has the prefix of iot-platform so the
			 * subject will have the iot-platform prefix
			 */

			String subject = Prefixes.IOT_PLATFORM.getPrefix() + value.toString().toLowerCase();
			String object = valueClassType.getPrefix().getPrefix() + valueClassType.getName();

			stringBuilder.append(subject + " a " + object + " . \n");

		}

		int counter = 0;

		for (Prefixes prefix : Prefixes.values()) {
			/*
			 * 8 because there are only 9 prefixes and the counter started from
			 * 0
			 */
			if (counter == 8) {
				prefixStringBuilder.append("SEM_ALIAS('" + prefix.getPrefixName() + "','" + prefix.getUri() + "')");
			} else {
				prefixStringBuilder.append("SEM_ALIAS('" + prefix.getPrefixName() + "','" + prefix.getUri() + "'),");
			}

			counter++;
		}

		String modelName = applicationName.replaceAll(" ", "").toUpperCase() + suffix;
		stringBuilder.append("}',sem_models('" + modelName + "'),null,");
		stringBuilder.append("SEM_ALIASES(" + prefixStringBuilder.toString() + "),null))");

		return stringBuilder.toString();
	}

	/*
	 * constructIntegrityConstraintCheckSubQuery method takes a list of
	 * objectProperties values and construct a subquery that checks that the
	 * object are valid (the passed objectValue exist in the applicationModel)
	 */

	private String constructIntegrityConstraintCheckSubQuery(ArrayList<ValueOfTypeClass> classValueList) {

		StringBuilder stringBuilder = new StringBuilder();
		
		for (ValueOfTypeClass valueOfTypeClass : classValueList) {
			Class valueClassType = valueOfTypeClass.getTypeClass();
			Object value = valueOfTypeClass.getValue();

			/*
			 * any instance created has the prefix of iot-platform so the
			 * subject will have the iot-platform prefix
			 */

			String subject = Prefixes.IOT_PLATFORM.getPrefix() + value.toString().toLowerCase();
			String object = valueClassType.getPrefix().getPrefix() + valueClassType.getName();

			stringBuilder.append(subject + " a " + object + " . \n");

		}

		return stringBuilder.toString();
	}

	/*
	 * constructUniqueContstraintCheckSubQueryStr method takes a list of
	 * properties that must have unique value and the value inserted to check if
	 * there is a similar value that will violate unique constraint
	 */

	private String constructUniqueContstraintCheckSubQueryStr(ArrayList<PropertyValue> uniquePropValueList,
			Class subjectClass) {

		StringBuilder stringBuilder = new StringBuilder();
		StringBuilder filterConditionsStringBuilder = new StringBuilder();

		String variableName = "?var";
		int variableCount = 0;

		/*
		 * start of the subQuery
		 */

		stringBuilder.append("{ SELECT (COUNT(*) as ?isUnique ) WHERE { ");
		stringBuilder.append("?subject a " + subjectClass.getUri() + " ; ");

		/*
		 * start of filter part
		 */

		filterConditionsStringBuilder.append(" FILTER ( ");

		/*
		 * iteration on uniquePropValueList to construct graph patterns and
		 * filter conditions of the subquery
		 */

		int count = 0;
		int size = uniquePropValueList.size();

		for (PropertyValue propertyValue : uniquePropValueList) {
			String prefixedPropertyName = propertyValue.getPropertyName();
			Object value = propertyValue.getValue();

			if (count < size - 1) {
				stringBuilder.append(prefixedPropertyName + "  " + variableName + variableCount + " ; ");

				/*
				 * check is the value is a string value to make add ""
				 */

				if (value instanceof String) {
					filterConditionsStringBuilder
							.append("LCASE(" + variableName + variableCount + ") = \"" + value.toString() + "\" ||  ");
				} else {
					filterConditionsStringBuilder
							.append("LCASE(" + variableName + variableCount + ") = " + value.toString() + " ||  ");
				}

			} else {

				/*
				 * check if it is the last condition to end the graph patterns
				 * with a dot
				 * 
				 * and not adding || for the last filter condition
				 */

				stringBuilder.append(prefixedPropertyName + "  " + variableName + variableCount + " .");

				/*
				 * check is the value is a string value to make add ""
				 */

				if (value instanceof String) {
					filterConditionsStringBuilder
							.append("LCASE(" + variableName + variableCount + ") = \"" + value.toString() + "\"");
				} else {
					filterConditionsStringBuilder
							.append("LCASE(" + variableName + variableCount + ") = " + value.toString());
				}

			}

			count++;
			variableCount++;
		}

		/*
		 * complete end of the filter part
		 */

		filterConditionsStringBuilder.append(" )");

		/*
		 * complete end of the subquery structure
		 */

		stringBuilder.append(" " + filterConditionsStringBuilder.toString() + " }} ");

		return stringBuilder.toString();

	}

	public static void main(String[] args) {
		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		String szUser = "rdfusr";
		String szPasswd = "rdfusr";

		ValidationDao validationDao = new ValidationDao(new Oracle(szJdbcURL, szUser, szPasswd));

		ArrayList<PropertyValue> uniquePropValueList = new ArrayList<>();
		uniquePropValueList.add(new PropertyValue("foaf:userName", "HatemMorgan"));
		uniquePropValueList.add(new PropertyValue("foaf:mbox", "hatemmorgan17@gmail.com"));
		uniquePropValueList.add(new PropertyValue("foaf:mbox", "hatem.el-sayed@student.guc.edu.eg"));

		// System.out.println(Prefixes.SSN.toString().toLowerCase());
		// Hashtable<Class, Object> htblClassValue = new Hashtable<>();
		// htblClassValue.put(new Application(), "testapplication");
		// // this will fail the check
		// htblClassValue.put(new Person(), "Hatem");
		try {
			// System.out.println(validationDao.constructUniqueContstraintCheckSubQueryStr(uniquePropValueList,
			// new Person()));
			// System.out.println(validationDao.checkIfInstanceExsist("testApplication",
			// htblClassValue));
		} catch (DatabaseException e) {
			System.out.println(e.getCode());
			System.out.println(e.getMessage());
			System.out.println(e.getExceptionMessage());
		}

	}
}
