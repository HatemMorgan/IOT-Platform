package com.iotplatform.daos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.classes.Admin;
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
	}

	public int checkIfInstanceExsist(String applicationName, Class valueClassType, Object value) {
		String modelName = applicationName.replaceAll(" ", "").toUpperCase() + suffix;
		String queryString = "select found from table(sem_match('select (count(*) as ?found) where{ ? a ? .}',sem_models(?),null,SEM_ALIASES(SEM_ALIAS(?,?)),null))";
		try {
			PreparedStatement stat = oracle.getConnection().prepareStatement(queryString);
			stat.setObject(1, valueClassType.getPrefix().getPrefix() + value.toString());
			stat.setObject(2, valueClassType.getPrefix() + valueClassType.getName());
			stat.setString(3, modelName);
			stat.setString(4, valueClassType.getPrefix().toString().toLowerCase());
			stat.setString(5, valueClassType.getPrefix().getUri());

			ResultSet resultSet = oracle.executeQuery(queryString, 0, 1);
			resultSet.next();
			int found = resultSet.getInt(1);
			System.out.println(found);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}

	/*
	 * constructQuery used to construct a sparql query String based on input it
	 * takes the application name to get the application model in order to query
	 * and it takes a hashtable of classes and values to check if there is an
	 * instance of that class with this value exist or not to insure data
	 * integrity and consistency
	 */

	private String constructQuery(String applicationName, Hashtable<Class, Object> htblClassValue ){
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("select found from table(sem_match('select (count(*) as ?found) where{");		
		 StringBuilder prefixStringBuilder = new StringBuilder();
		Iterator<Class> iterator = htblClassValue.keySet().iterator();
	
		Hashtable<String, String> htblPrefixes = new Hashtable<>();
		
		while(iterator.hasNext()){
			
			Class valueClassType = iterator.next();
			Object value = htblClassValue.get(valueClassType);
			String subject = valueClassType.getPrefix().getPrefix()+value.toString().toLowerCase();
			String object = valueClassType.getPrefix().getPrefix()+valueClassType.getName();
			Prefixes prefix = valueClassType.getPrefix();
			String alias;
			switch(prefix){
			case IOT_LITE : alias = "iot-lite";break;
			case IOT_PLATFORM: alias = "iot-platform"; break;
			default: alias = prefix.toString().toLowerCase();
			}
			
			String uri = valueClassType.getPrefix().getUri();
			
			if(!htblPrefixes.containsKey(prefix)){
				htblPrefixes.put(alias, uri);
				
				if(iterator.hasNext()){
				prefixStringBuilder.append("SEM_ALIAS('"+alias+"','"+uri+"'),");
				}else{
					prefixStringBuilder.append("SEM_ALIAS('"+alias+"','"+uri+"')");
				}
			}
			
			stringBuilder.append(subject+" a "+object+" . \n");
			
		}
		String modelName = applicationName.replaceAll(" ", "").toUpperCase() + suffix;
		stringBuilder.append("}',sem_models('"+modelName+"'),null,");	
		stringBuilder.append("SEM_ALIASES("+prefixStringBuilder.toString()+"),null))");
		
		
		return stringBuilder.toString();
	}

	public static void main(String[] args) {
		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		String szUser = "rdfusr";
		String szPasswd = "rdfusr";

		ValidationDao validationDao = new ValidationDao(new Oracle(szJdbcURL, szUser, szPasswd));
		// System.out.println(Prefixes.SSN.toString().toLowerCase());
		Hashtable<Class, Object> htblClassValue = new Hashtable<>();
		htblClassValue.put(new Application(), "testApp");
		htblClassValue.put(new Person(), "Hatem");
		String autoConstructedQuery = validationDao.constructQuery("testApp", htblClassValue);
		System.out.println(autoConstructedQuery);

	}
}
