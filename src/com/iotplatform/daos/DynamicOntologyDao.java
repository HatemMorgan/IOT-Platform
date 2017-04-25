package com.iotplatform.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import org.apache.jena.update.UpdateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.mapers.DynamicOntologyMapper;
import com.iotplatform.ontology.mapers.OntologyMapper;

import oracle.spatial.rdf.client.jena.DatasetGraphOracleSem;
import oracle.spatial.rdf.client.jena.GraphOracleSem;
import oracle.spatial.rdf.client.jena.Oracle;

@Repository("dynamicOntologyDao")
public class DynamicOntologyDao {

	private static String prefixesString = null;
	private static String prefixes = null;
	private static Hashtable<String, Prefix> htblPrefixes;
	private Oracle oracle;

	@Autowired
	public DynamicOntologyDao(Oracle oracle) {
		this.oracle = oracle;
	}

	public void addNewClassToOntology(String newClassName, String applicationModelName) {

		StringBuilder insertQueryBuilder = new StringBuilder();

		insertQueryBuilder.append("PREFIX	owl:	<http://www.w3.org/2002/07/owl#> \n");
		insertQueryBuilder.append("PREFIX iot-platform: <http://iot-platform#> \n");
		insertQueryBuilder.append("INSERT DATA { \n");
		insertQueryBuilder.append("GRAPH <http://iot-platform#ontologyGraph> { \n");

		insertQueryBuilder.append(Prefix.IOT_PLATFORM.getPrefix() + newClassName + " a owl:Class. \n");

		insertQueryBuilder.append("} \n");
		insertQueryBuilder.append("} \n");
		System.out.println(insertQueryBuilder.toString());
		try {

			GraphOracleSem graphOracleSem = new GraphOracleSem(oracle, applicationModelName);
			DatasetGraphOracleSem dsgos = DatasetGraphOracleSem.createFrom(graphOracleSem);

			// ModelOracleSem model =
			// ModelOracleSem.createOracleSemModel(oracle,
			// applicationModelName);
			UpdateAction.parseExecute(insertQueryBuilder.toString(), dsgos);
			// model.close();
			dsgos.close();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), "Ontology");
		}

	}

	public void addNewObjectPropertyToOntology(String newPropertyName, String domainClassPrefixedName,
			String rangeClassPrefixedName, String applicationModelName) {

		StringBuilder insertQueryBuilder = new StringBuilder();

		if (prefixesString == null) {
			StringBuilder prefixStringBuilder = new StringBuilder();
			for (Prefix prefix : Prefix.values()) {
				prefixStringBuilder.append("PREFIX " + prefix.getPrefix() + " <" + prefix.getUri() + ">\n");
			}

			prefixesString = prefixStringBuilder.toString();
		}

		insertQueryBuilder.append(prefixesString);
		insertQueryBuilder.append("CREATE GRAPH iot-platform:ontologyGraph ; \n");
		insertQueryBuilder.append("INSERT DATA { \n");
		insertQueryBuilder.append("GRAPH iot-platform:ontologyGraph { \n");

		insertQueryBuilder.append(Prefix.IOT_PLATFORM.getPrefix() + newPropertyName + " a owl:ObjectProperty; \n");
		insertQueryBuilder.append("rdfs:domain " + domainClassPrefixedName + "; \n");
		insertQueryBuilder.append("rdfs:range " + rangeClassPrefixedName + ". \n");

		insertQueryBuilder.append("} \n");
		insertQueryBuilder.append("} \n");

		System.out.println(insertQueryBuilder.toString());

		try {
			GraphOracleSem graphOracleSem = new GraphOracleSem(oracle, applicationModelName);
			DatasetGraphOracleSem dsgos = DatasetGraphOracleSem.createFrom(graphOracleSem);

			// ModelOracleSem model =
			// ModelOracleSem.createOracleSemModel(oracle,
			// applicationModelName);
			UpdateAction.parseExecute(insertQueryBuilder.toString(), dsgos);
			// model.close();
			dsgos.close();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), "Ontology");
		}

	}

	public void loadAndCacheApplicationDynamicOntologyClasses(String applicationModelName) {

		StringBuilder queryBuilder = new StringBuilder();

		/*
		 * check if prefixes String was initialized before or not (not null)
		 */
		if (prefixes == null) {
			prefixes = getPrefixesQueryAliases();
		}

		queryBuilder.append("SELECT class FROM TABLE(SEM_MATCH(' \n ");
		queryBuilder.append("SELECT ?class WHERE { \n");
		queryBuilder.append("GRAPH iot-platform:ontologyGraph { ?class a owl:Class } \n");
		queryBuilder.append("} ', \n");
		queryBuilder.append(
				"sem_models('" + applicationModelName + "'),null, \n " + "SEM_ALIASES(" + prefixes + "),null))");

		System.out.println(queryBuilder.toString());

		try {
			ResultSet results = oracle.executeQuery(queryBuilder.toString(), 0, 1);

			while (results.next()) {

				String dynamicClassURI = results.getString("class");

				String[] res = getValueFromURI(dynamicClassURI);
				String dynamicClassName = res[0];
				String dynamicClassPrefixURI = res[1];

				Prefix prefix = getPrefix(dynamicClassPrefixURI);

				Class newClass = new Class(dynamicClassName, dynamicClassURI, prefix);

				if (DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri().contains(applicationModelName)) {

					DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri().get(applicationModelName)
							.put(dynamicClassURI, newClass);
					DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
							.put(dynamicClassName, newClass);
				} else {
					DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri().put(applicationModelName,
							new Hashtable<>());
					DynamicOntologyMapper.getHtblappDynamicOntologyClasses().put(applicationModelName,
							new Hashtable<>());

					DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri().get(applicationModelName)
							.put(dynamicClassURI, newClass);
					DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
							.put(dynamicClassName, newClass);
				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), "Ontology");

		}

	}

	public void loadAndCacheApplicationDynamicOntologyObjectProperties(String applicationModelName) {

		StringBuilder queryBuilder = new StringBuilder();

		/*
		 * check if prefixes String was initialized before or not (not null)
		 */
		if (prefixes == null) {
			prefixes = getPrefixesQueryAliases();
		}

		queryBuilder.append("SELECT property,domain,range FROM TABLE(SEM_MATCH(' \n ");
		queryBuilder.append("SELECT ?property ?domain ?range WHERE { \n");
		queryBuilder.append("GRAPH iot-platform:ontologyGraph { ?property a owl:ObjectProperty; rdfs:domain ?domain; "
				+ "rdfs:range ?range } \n");
		queryBuilder.append("} ' , \n");
		queryBuilder.append("sem_models('" + applicationModelName + "'),null, \n SEM_ALIASES(" + prefixes + "),null))");

		System.out.println(queryBuilder.toString());

		try {
			ResultSet results = oracle.executeQuery(queryBuilder.toString(), 0, 1);

			while (results.next()) {

				String dynamicObjPropertyURI = results.getString("property");
				String objPropDomainClassURI = results.getString("domain");
				String objPropRangeClassURI = results.getString("range");

				String[] propertyRes = getValueFromURI(dynamicObjPropertyURI);
				String propertyName = propertyRes[0];
				String propertyURI = propertyRes[1];
				Prefix prefix = getPrefix(propertyURI);

				Class domainClass = null;
				Class rangeClass = null;

				if (DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri().get(applicationModelName)
						.containsKey(objPropDomainClassURI)) {

					domainClass = DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri().get(applicationModelName)
							.get(objPropDomainClassURI);
				} else {
					loadAndCacheApplicationDynamicOntologyClasses(applicationModelName);

					if (DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri().get(applicationModelName)
							.containsKey(objPropRangeClassURI)) {
						rangeClass = DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri()
								.get(applicationModelName).get(objPropRangeClassURI);
					}

				}

				if (OntologyMapper.getHtblMainOntologyClassesUriMappers().containsKey(objPropRangeClassURI)) {

					rangeClass = OntologyMapper.getHtblMainOntologyClassesUriMappers().get(objPropRangeClassURI);
				} else {

					if (DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri().get(applicationModelName)
							.containsKey(objPropRangeClassURI)) {

						rangeClass = DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri()
								.get(applicationModelName).get(objPropRangeClassURI);
					} else {
						loadAndCacheApplicationDynamicOntologyClasses(applicationModelName);

						if (DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri().get(applicationModelName)
								.containsKey(objPropRangeClassURI)) {
							rangeClass = DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri()
									.get(applicationModelName).get(objPropRangeClassURI);
						}

					}
				}

				ObjectProperty newObjectProperty = new ObjectProperty(domainClass, propertyName, prefix, rangeClass,
						false, false);

				domainClass.getProperties().put(propertyName, newObjectProperty);

			}

		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage(), "Ontology");

		}

	}

	/**
	 * loadAndCacheDynamicPropertiesOfClass is used to load dynamic properties
	 * added by user to the application with @param applicationModelName
	 * domainOntology for @param subjectClass
	 * 
	 * @param subjectClass
	 *            class that will be used to load its properties and to cache
	 *            the loaded properties
	 * 
	 * @param applicationModelName
	 *            applicationModelName if the modelName of the application that
	 *            stores the data in the default graph and dynamicOntology in
	 *            the ontologyGraph
	 */
	public void loadAndCacheDynamicPropertiesOfClass(Class subjectClass, String applicationModelName) {

	}

	/*
	 * getPrefix is used to get Prefix enum that maps prefixAlias and it will
	 * return null if the prefixAlias is not valid
	 */
	private static Prefix getPrefix(String prefixURI) {
		if (htblPrefixes == null) {
			htblPrefixes = new Hashtable<>();

			for (Prefix prefix : Prefix.values()) {
				htblPrefixes.put(prefix.getUri(), prefix);
			}

		}

		if (htblPrefixes.containsKey(prefixURI)) {
			return htblPrefixes.get(prefixURI);
		} else {
			return null;
		}
	}

	private String getPrefixesQueryAliases() {
		/*
		 * construct prefixes
		 */
		StringBuilder prefixStringBuilder = new StringBuilder();
		int counter = 0;
		int stop = Prefix.values().length - 1;
		for (Prefix prefix : Prefix.values()) {
			if (counter == stop) {
				prefixStringBuilder.append("SEM_ALIAS('" + prefix.getPrefixName() + "','" + prefix.getUri() + "')");
			} else {
				prefixStringBuilder.append("SEM_ALIAS('" + prefix.getPrefixName() + "','" + prefix.getUri() + "'),");
			}

			counter++;
		}

		return prefixStringBuilder.toString();

	}

	/*
	 * getValueFromURI is used to get value from a URI.
	 * 
	 * URI consists of a prefixURI and value so I loop on all prefixes and
	 * return the value by removing the matched prefexURI
	 * 
	 * eg: uri = http://iot-platform#enterprisecompanies the returned value =
	 * enterprisecompanies
	 */
	private static String[] getValueFromURI(String uri) {

		String[] res = new String[2];

		for (Prefix prefix : Prefix.values()) {
			if (uri.contains(prefix.getUri())) {
				res[0] = uri.substring(prefix.getUri().length());
				res[1] = prefix.getUri();
			}
		}
		return res;
	}

	public static void main(String[] args) {
		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		String szUser = "rdfusr";
		String szPasswd = "rdfusr";

		Oracle oracle = new Oracle(szJdbcURL, szUser, szPasswd);

		DynamicOntologyDao dynamicOntologyDao = new DynamicOntologyDao(oracle);

		// dynamicOntologyDao.addNewClassToOntology("VirtualSensor",
		// "TESTAPPLICATION_MODEL");
		// dynamicOntologyDao.addNewObjectPropertyToOntology("virtual",
		// "ssn:Device",
		// "iot-platform:VirtualSensor",
		// "TESTAPPLICATION_MODEL");

		dynamicOntologyDao.loadAndCacheApplicationDynamicOntologyClasses("TESTAPPLICATION_MODEL");
		dynamicOntologyDao.loadAndCacheApplicationDynamicOntologyObjectProperties("TESTAPPLICATION_MODEL");

		System.out.println(OntologyMapper.getHtblMainOntologyClassesMappers().get("device").getProperties());
		System.out.println(DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri().get("TESTAPPLICATION_MODEL"));

	}

}
