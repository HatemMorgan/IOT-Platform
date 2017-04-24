package com.iotplatform.daos;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.mapers.OntologyMapper;

import oracle.spatial.rdf.client.jena.Oracle;

@Repository("ontologyDao")
public class OntologyDao {

	private static String prefixesString = null;
	private static String prefixes = null;
	private Oracle oracle;

	@Autowired
	public OntologyDao(Oracle oracle) {
		this.oracle = oracle;
	}

	public void addNewClassToOntology(String newClassName) {

		StringBuilder insertQueryBuilder = new StringBuilder();

		if (prefixesString == null) {
			StringBuilder prefixStringBuilder = new StringBuilder();
			for (Prefix prefix : Prefix.values()) {
				prefixStringBuilder.append("PREFIX	" + prefix.getPrefix() + "	<" + prefix.getUri() + ">\n");
			}

			prefixesString = prefixStringBuilder.toString();
		}

		insertQueryBuilder.append(prefixesString);
		insertQueryBuilder.append("INSERT DATA { \n");
		insertQueryBuilder.append("{ GRAPH iot-platform:ontology { \n");

		insertQueryBuilder.append(Prefix.IOT_PLATFORM.getPrefix() + newClassName + " a owl:Class. \n");

		insertQueryBuilder.append("} \n");
		insertQueryBuilder.append("} \n");

	}

	public void addNewObjectPropertyToOntology(String newPropertyName, String domainClassPrefixedName,
			String rangeClassPrefixedName) {

		StringBuilder insertQueryBuilder = new StringBuilder();

		if (prefixesString == null) {
			StringBuilder prefixStringBuilder = new StringBuilder();
			for (Prefix prefix : Prefix.values()) {
				prefixStringBuilder.append("PREFIX	" + prefix.getPrefix() + "	<" + prefix.getUri() + ">\n");
			}

			prefixesString = prefixStringBuilder.toString();
		}

		insertQueryBuilder.append(prefixesString);
		insertQueryBuilder.append("INSERT DATA { \n");
		insertQueryBuilder.append("{ GRAPH iot-platform:ontology { \n");

		insertQueryBuilder.append(Prefix.IOT_PLATFORM.getPrefix() + newPropertyName + " a owl:ObjectProperty; \n");
		insertQueryBuilder.append("rdfs:domain " + domainClassPrefixedName + "; \n");
		insertQueryBuilder.append("rdfs:range " + rangeClassPrefixedName + ". \n");

		insertQueryBuilder.append("} \n");
		insertQueryBuilder.append("} \n");

	}

	public void loadAndCacheApplicationDynamicOntologyClasses(String applicationModelName) {

		StringBuilder queryBuilder = new StringBuilder();

		queryBuilder.append("SELECT class FROM TABLE(SEM_MATCH(' \n ");
		queryBuilder.append("SELECT ?class WHERE { \n");
		queryBuilder.append("GRAPH { ?class a owl:Class } \n");
		queryBuilder.append("} ' \n");
		queryBuilder.append("sem_models('" + applicationModelName + "'),null, \n SEM_ALIASES(" + prefixes + "),null))");

		/*
		 * check if prefixes String was initialized before or not (not null)
		 */
		if (prefixes == null) {
			prefixes = getPrefixesQueryAliases();
		}

		try {
			ResultSet results = oracle.executeQuery(queryBuilder.toString(), 0, 1);

			ResultSetMetaData rsmd = results.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			while (results.next()) {
				
				String dynamicClassName = results.getString("class");
				
//				if(OntologyMapper.)
				
			}

		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage(), "Ontology");

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
}
