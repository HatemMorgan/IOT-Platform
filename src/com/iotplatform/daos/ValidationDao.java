package com.iotplatform.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.classes.Application;
import com.iotplatform.ontology.classes.Person;

import oracle.spatial.rdf.client.jena.Oracle;

@Component
public class ValidationDao {
	private Oracle oracle;
	private final String suffix = "_MODEL";

	@Autowired
	public ValidationDao(Oracle oracle) {
		this.oracle = oracle;
		System.out.println("Validaiton DAO Bean Created");
	}

	/*
	 * checkIfInstanceExsist used to query the passed application model to check
	 * if there are instances of a specified classes passed to it . This
	 * validation is done to make sure that no object property has a value which
	 * is not available to maintain the consistency and integrity of the data
	 */

	public int checkIfInstanceExsist(String applicationName, Hashtable<Class, Object> htblClassValue) {
		String queryString = constructQuery(applicationName, htblClassValue);
		try {
			ResultSet resultSet = oracle.executeQuery(queryString, 0, 1);
			resultSet.next();
			return resultSet.getInt(1);

		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage(),"Application");

		}
	}

	/*
	 * constructQuery used to construct a sparql query String based on input it
	 * takes the application name to get the application model in order to query
	 * and it takes a hashtable of classes and values to check if there is an
	 * instance of that class with this value exist or not to insure data
	 * integrity and consistency
	 * 
	 * The Sparql injected sql query auto constructed query has the same format
	 * like this:
	 * 
	 * select found from table(sem_match('select (count(*) as ?found)
	 * where{iot-platform:testapp a iot-platform:Application
	 * .}',sem_models('TESTAPP_MODEL'),null,SEM_ALIASES(SEM_ALIAS('iot-platform'
	 * ,'http://iot-platform#')),null));
	 */

	private String constructQuery(String applicationName, Hashtable<Class, Object> htblClassValue) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("select found from table(sem_match('select (count(*) as ?found) where{");
		StringBuilder prefixStringBuilder = new StringBuilder();
		Iterator<Class> iterator = htblClassValue.keySet().iterator();

		Hashtable<String, String> htblPrefixes = new Hashtable<>();

		while (iterator.hasNext()) {

			Class valueClassType = iterator.next();
			Object value = htblClassValue.get(valueClassType);
			String subject = valueClassType.getPrefix().getPrefix() + value.toString();
			String object = valueClassType.getPrefix().getPrefix() + valueClassType.getName();
			Prefixes prefix = valueClassType.getPrefix();
			String alias;

			/*
			 * iot-lite and iot-platform prefixes does not have the alias that
			 * is correct so I must change _ to - in order to have a correct
			 * auto query construction
			 */
			switch (prefix) {
			case IOT_LITE:
				alias = "iot-lite";
				break;
			case IOT_PLATFORM:
				alias = "iot-platform";
				break;
			default:
				alias = prefix.toString().toLowerCase();
			}

			String uri = valueClassType.getPrefix().getUri();

			if (!htblPrefixes.containsKey(prefix)) {
				htblPrefixes.put(alias, uri);

				if (iterator.hasNext()) {
					prefixStringBuilder.append("SEM_ALIAS('" + alias + "','" + uri + "'),");
				} else {
					prefixStringBuilder.append("SEM_ALIAS('" + alias + "','" + uri + "')");
				}
			}

			stringBuilder.append(subject + " a " + object + " . \n");

		}
		String modelName = applicationName.replaceAll(" ", "").toUpperCase() + suffix;
		stringBuilder.append("}',sem_models('" + modelName + "'),null,");
		stringBuilder.append("SEM_ALIASES(" + prefixStringBuilder.toString() + "),null))");

		return stringBuilder.toString();
	}

//	 public static void main(String[] args) {
//	 String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
//	 String szUser = "rdfusr";
//	 String szPasswd = "rdfusr";
//	
//	 ValidationDao validationDao = new ValidationDao(new Oracle(szJdbcURL,
//	 szUser, szPasswd));
//	 // System.out.println(Prefixes.SSN.toString().toLowerCase());
//	 Hashtable<Class, Object> htblClassValue = new Hashtable<>();
//	 htblClassValue.put(new Application(), "testapplication");
//	 // this will fail the check
//	  htblClassValue.put(new Person(), "Hatem");
//	 try {
//	 System.out.println(validationDao.checkIfInstanceExsist("testApplication",
//	 htblClassValue));
//	 } catch (DatabaseException e) {
//	 System.out.println(e.getCode());
//	 System.out.println(e.getMessage());
//	 System.out.println(e.getExceptionMessage());
//	 }
//	
//	 }
}
