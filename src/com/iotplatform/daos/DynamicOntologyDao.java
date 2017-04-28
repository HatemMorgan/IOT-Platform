package com.iotplatform.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.jena.update.UpdateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.exceptions.InvalidDynamicOntologyException;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.PropertyType;
import com.iotplatform.ontology.XSDDatatype;
import com.iotplatform.ontology.mapers.DynamicOntologyMapper;
import com.iotplatform.ontology.mapers.DynamicOntologyStateEnum;
import com.iotplatform.ontology.mapers.OntologyMapper;

import oracle.spatial.rdf.client.jena.DatasetGraphOracleSem;
import oracle.spatial.rdf.client.jena.GraphOracleSem;
import oracle.spatial.rdf.client.jena.Oracle;

@Repository("dynamicOntologyDao")
public class DynamicOntologyDao {

	private static String prefixesString = null;
	private static String prefixes = null;
	private static Hashtable<String, Prefix> htblPrefixes;
	private static Hashtable<String, XSDDatatype> htblXSDDatatypes;
	private Oracle oracle;

	@Autowired
	public DynamicOntologyDao(Oracle oracle) {
		this.oracle = oracle;

		htblXSDDatatypes = new Hashtable<>();

		for (XSDDatatype xsdDatatype : XSDDatatype.values()) {
			htblXSDDatatypes.put(xsdDatatype.getDataType(), xsdDatatype);
		}
	}

	public void addNewClassToOntology(String newClassName, String applicationModelName) {

		StringBuilder insertQueryBuilder = new StringBuilder();

		insertQueryBuilder.append("PREFIX	owl:	<http://www.w3.org/2002/07/owl#> \n");
		insertQueryBuilder.append("PREFIX	rdfs:	<http://www.w3.org/2000/01/rdf-schema#> \n");
		insertQueryBuilder.append("PREFIX iot-platform: <http://iot-platform#> \n");
		insertQueryBuilder.append("INSERT DATA { \n");
		insertQueryBuilder.append("GRAPH <http://iot-platform#ontologyGraph> { \n");

		insertQueryBuilder.append(Prefix.IOT_PLATFORM.getPrefix() + newClassName + " a owl:Class; \n");
		insertQueryBuilder.append(" rdfs:label \"" + newClassName + "\" . \n");

		insertQueryBuilder.append("} \n");
		insertQueryBuilder.append("} \n");
		System.out.println(insertQueryBuilder.toString());
		try {

			GraphOracleSem graphOracleSem = new GraphOracleSem(oracle, applicationModelName);
			DatasetGraphOracleSem dsgos = DatasetGraphOracleSem.createFrom(graphOracleSem);

			UpdateAction.parseExecute(insertQueryBuilder.toString(), dsgos);
			dsgos.close();

			if (DynamicOntologyMapper.getHtblApplicationOntologyState().containsKey(applicationModelName)) {

				DynamicOntologyMapper.getHtblApplicationOntologyState().remove(applicationModelName);
				DynamicOntologyMapper.getHtblApplicationOntologyState().put(applicationModelName,
						DynamicOntologyStateEnum.Modified);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), "Ontology");
		}

	}

	public void addNewObjectPropertyToOntology(String newPropertyName, String domainClassPrefixedName,
			String rangeClassPrefixedName, boolean hasMultipleValued, boolean isUnique, String applicationModelName) {

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
		insertQueryBuilder.append("rdfs:range " + rangeClassPrefixedName + "; \n");
		insertQueryBuilder.append("iot-platform:hasMultipleValues \"" + hasMultipleValued + "\""
				+ XSDDatatype.boolean_type.getXsdType() + " ; \n");
		insertQueryBuilder
				.append("iot-platform:isUnique \"" + isUnique + "\"" + XSDDatatype.boolean_type.getXsdType() + " . \n");
		insertQueryBuilder.append(domainClassPrefixedName + " rdfs:label \""
				+ getNameFromPrefixedName(domainClassPrefixedName) + "\" . \n");
		insertQueryBuilder.append("} \n");
		insertQueryBuilder.append("} \n");

		System.out.println(insertQueryBuilder.toString());

		try {
			GraphOracleSem graphOracleSem = new GraphOracleSem(oracle, applicationModelName);
			DatasetGraphOracleSem dsgos = DatasetGraphOracleSem.createFrom(graphOracleSem);

			UpdateAction.parseExecute(insertQueryBuilder.toString(), dsgos);
			dsgos.close();

			if (DynamicOntologyMapper.getHtblApplicationOntologyState().containsKey(applicationModelName)) {
				DynamicOntologyMapper.getHtblApplicationOntologyState().remove(applicationModelName);
				DynamicOntologyMapper.getHtblApplicationOntologyState().put(applicationModelName,
						DynamicOntologyStateEnum.Modified);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), "Ontology");
		}

	}

	public void addNewDatatypePropertyToOntology(String newPropertyName, String domainClassPrefixedName,
			String rangeDatatypeName, boolean hasMultipleValued, boolean isUnique, String applicationModelName) {

		XSDDatatype xsdDatatype = getXSDDataTypeEnumOfDataTypeName(rangeDatatypeName.toLowerCase());
		if (xsdDatatype == null) {
			throw new InvalidDynamicOntologyException("Invalid Dynamic Ontology request body. "
					+ "Invalid datatype. The datatype must be one of " + htblXSDDatatypes.keySet().toString());
		}

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

		insertQueryBuilder.append(Prefix.IOT_PLATFORM.getPrefix() + newPropertyName + " a owl:DatatypeProperty; \n");
		insertQueryBuilder.append("rdfs:domain " + domainClassPrefixedName + "; \n");
		insertQueryBuilder.append("rdfs:range <" + xsdDatatype.getXsdTypeURI() + ">; \n");
		insertQueryBuilder.append("iot-platform:hasMultipleValues \"" + hasMultipleValued + "\""
				+ XSDDatatype.boolean_type.getXsdType() + " ; \n");
		insertQueryBuilder
				.append("iot-platform:isUnique \"" + isUnique + "\"" + XSDDatatype.boolean_type.getXsdType() + " . \n");

		insertQueryBuilder.append(domainClassPrefixedName + " rdfs:label \""
				+ getNameFromPrefixedName(domainClassPrefixedName) + "\" . \n");

		insertQueryBuilder.append("} \n");
		insertQueryBuilder.append("} \n");

		System.out.println(insertQueryBuilder.toString());

		try {
			GraphOracleSem graphOracleSem = new GraphOracleSem(oracle, applicationModelName);
			DatasetGraphOracleSem dsgos = DatasetGraphOracleSem.createFrom(graphOracleSem);

			UpdateAction.parseExecute(insertQueryBuilder.toString(), dsgos);
			dsgos.close();

			if (DynamicOntologyMapper.getHtblApplicationOntologyState().containsKey(applicationModelName)) {

				DynamicOntologyMapper.getHtblApplicationOntologyState().remove(applicationModelName);
				DynamicOntologyMapper.getHtblApplicationOntologyState().put(applicationModelName,
						DynamicOntologyStateEnum.Modified);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), "Ontology");
		}

	}

	public void loadAndCacheApplicationDynamicOntology(String applicationModelName) {
		loadAndCacheAllApplicationDynamicOntologyClasses(applicationModelName);
		loadAndCacheAllApplicationDynamicOntologyDataTypeProperties(applicationModelName);
		loadAndCacheAllApplicationDynamicOntologyObjectProperties(applicationModelName);

		DynamicOntologyMapper.getHtblApplicationOntologyState().put(applicationModelName,
				DynamicOntologyStateEnum.NotModified);

	}

	private void loadAndCacheAllApplicationDynamicOntologyClasses(String applicationModelName) {

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

//		System.out.println(queryBuilder.toString());

		try {
			ResultSet results = oracle.executeQuery(queryBuilder.toString(), 0, 1);

			while (results.next()) {

				String dynamicClassURI = results.getString("class");

				cacheDynamicClass(dynamicClassURI, applicationModelName);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), "Ontology");

		}

	}

	private void loadAndCacheAllApplicationDynamicOntologyObjectProperties(String applicationModelName) {

		StringBuilder queryBuilder = new StringBuilder();

		/*
		 * check if prefixes String was initialized before or not (not null)
		 */
		if (prefixes == null) {
			prefixes = getPrefixesQueryAliases();
		}

		queryBuilder.append("SELECT property,domain,range,hasMultipleValues,isUnique FROM TABLE(SEM_MATCH(' \n ");
		queryBuilder.append("SELECT ?property ?domain ?range ?hasMultipleValues ?isUnique  WHERE { \n");
		queryBuilder.append(
				"GRAPH iot-platform:ontologyGraph { ?property a owl:ObjectProperty; \n" + "rdfs:domain ?domain; \n "
						+ "rdfs:range ?range; \n" + "iot-platform:hasMultipleValues ?hasMultipleValues ; \n"
						+ "iot-platform:isUnique ?isUnique . \n" + "} \n");
		queryBuilder.append("} ' , \n");
		queryBuilder.append("sem_models('" + applicationModelName + "'),null, \n SEM_ALIASES(" + prefixes + "),null))");

//		System.out.println(queryBuilder.toString());

		try {
			ResultSet results = oracle.executeQuery(queryBuilder.toString(), 0, 1);

			while (results.next()) {

				String dynamicObjPropertyURI = results.getString("property");
				String objPropDomainClassURI = results.getString("domain");
				String objPropRangeClassURI = results.getString("range");

				boolean hasMultipleValues = Boolean.parseBoolean(results.getString("hasMultipleValues"));
				boolean isUnique = Boolean.parseBoolean(results.getString("isUnique"));

				cacheDynamicProperties(objPropDomainClassURI, PropertyType.ObjectProperty, dynamicObjPropertyURI,
						objPropRangeClassURI, applicationModelName, hasMultipleValues, isUnique);

			}

		} catch (SQLException e) {
			throw new DatabaseException(e.getMessage(), "Ontology");

		}

	}

	private void loadAndCacheAllApplicationDynamicOntologyDataTypeProperties(String applicationModelName) {

		StringBuilder queryBuilder = new StringBuilder();

		/*
		 * check if prefixes String was initialized before or not (not null)
		 */
		if (prefixes == null) {
			prefixes = getPrefixesQueryAliases();
		}

		queryBuilder.append("SELECT property,domain,range,hasMultipleValues,isUnique FROM TABLE(SEM_MATCH(' \n ");
		queryBuilder.append("SELECT ?property ?domain ?range ?hasMultipleValues ?isUnique  WHERE { \n");
		queryBuilder
				.append("GRAPH iot-platform:ontologyGraph { ?property a owl:DatatypeProperty; \n rdfs:domain ?domain; \n "
						+ "rdfs:range ?range; \n  iot-platform:hasMultipleValues ?hasMultipleValues ; \n"
						+ "iot-platform:isUnique ?isUnique . \n } \n");
		queryBuilder.append("} ' , \n");
		queryBuilder.append("sem_models('" + applicationModelName + "'),null, \n SEM_ALIASES(" + prefixes + "),null))");

//		System.out.println(queryBuilder.toString());

		try {
			ResultSet results = oracle.executeQuery(queryBuilder.toString(), 0, 1);

			while (results.next()) {

				String dynamicObjPropertyURI = results.getString("property");
				String objPropDomainClassURI = results.getString("domain");
				String objPropRangeDataTypeURI = results.getString("range");

				boolean hasMultipleValues = Boolean.parseBoolean(results.getString("hasMultipleValues"));
				boolean isUnique = Boolean.parseBoolean(results.getString("isUnique"));

				cacheDynamicProperties(objPropDomainClassURI, PropertyType.DatatypeProperty, dynamicObjPropertyURI,
						objPropRangeDataTypeURI, applicationModelName, hasMultipleValues, isUnique);

			}

		} catch (SQLException e) {
			e.printStackTrace();
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
	// public void loadAndCacheDynamicPropertiesOfClass(Class subjectClass,
	// String applicationModelName) {
	//
	// }

	/**
	 * loadAndCacheDynamicClassesofApplicationDomain is used to load and cache
	 * dynamic added classes specified in @param(dyanmicClassesNameList) from
	 * applicationDomainDyanmicAddedOntology(which is stored in ontologyGraph in
	 * the applicationModel with @param(applicationModelName)
	 * 
	 * @param applicationModelName
	 *            hold the applicationModelName of the applicationModel where
	 *            the data and dynamic ontology stored there.
	 * 
	 * @param dyanmicClassesNameList
	 *            Holds classesNames that need to be loaded if they exist
	 */
	public void loadAndCacheDynamicClassesofApplicationDomain(String applicationModelName,
			ArrayList<String> dyanmicClassesNameList) {
		StringBuilder queryBuilder = new StringBuilder();

		/*
		 * check if prefixes String was initialized before or not (not null)
		 */
		if (prefixes == null) {
			prefixes = getPrefixesQueryAliases();
		}

		queryBuilder.append(
				"SELECT classURI,objectProperty,objectClassURI,hasMultipleValues1,isUnique1,datatypeProperty,dataTypeURI,hasMultipleValues2,isUnique2  \n ");
		queryBuilder.append("FROM TABLE(SEM_MATCH(' \n ");
		queryBuilder.append(
				"SELECT ?classURI ?objectProperty ?objectClassURI ?hasMultipleValues1 ?isUnique1 ?datatypeProperty ?dataTypeURI ?hasMultipleValues2 ?isUnique2 WHERE { \n");
		queryBuilder.append("GRAPH iot-platform:ontologyGraph { \n ?classURI rdfs:label ?className.  \n ");
		queryBuilder.append("FILTER ( \n");

		boolean start = true;
		for (String className : dyanmicClassesNameList) {
			if (start) {
				queryBuilder.append(" LCASE(?className) = \"" + className.toLowerCase() + "\"");
				start = false;
			} else {
				queryBuilder.append(" || LCASE(?className) = \"" + className.toLowerCase() + "\"");
			}
		}
		queryBuilder.append("\n ) \n");
		queryBuilder.append("OPTIONAL { \n");
		queryBuilder.append("?objectProperty a owl:ObjectProperty; \n");
		queryBuilder.append("rdfs:domain ?classURI; \n");
		queryBuilder.append("rdfs:range ?objectClassURI; \n");
		queryBuilder.append("iot-platform:hasMultipleValues ?hasMultipleValues1 ; \n");
		queryBuilder.append("iot-platform:isUnique ?isUnique1 . \n");
		queryBuilder.append("} \n");
		queryBuilder.append("OPTIONAL { \n");
		queryBuilder.append("?datatypeProperty a owl:DatatypeProperty; \n");
		queryBuilder.append("rdfs:domain ?classURI; \n");
		queryBuilder.append("rdfs:range ?dataTypeURI;\n");
		queryBuilder.append("iot-platform:hasMultipleValues ?hasMultipleValues2 ; \n");
		queryBuilder.append("iot-platform:isUnique ?isUnique2 . \n");
		queryBuilder.append("} \n");

		queryBuilder.append("} \n");
		queryBuilder.append("} ', \n");
		queryBuilder.append(
				"sem_models('" + applicationModelName + "'),null, \n " + "SEM_ALIASES(" + prefixes + "),null))");

		// System.out.println(queryBuilder.toString());
		try {

			ResultSet results = oracle.executeQuery(queryBuilder.toString(), 0, 1);

			// ResultSetMetaData rsmd = results.getMetaData();
			// int columnsNumber = rsmd.getColumnCount();
			// while (results.next()) {
			// for (int i = 1; i <= columnsNumber; i++) {
			// if (i > 1)
			// System.out.print(", ");
			// String columnValue = results.getString(i);
			// System.out.print(columnValue + " " + rsmd.getColumnName(i));
			// }
			// System.out.println("");
			// }
			// System.out.println("---------------------------------");

			/*
			 * htblClassURI is used to hold classesURIs to avoid repeating
			 * caching of classes as the classURI can be repeated because it
			 * might has more than one property
			 */
			Hashtable<String, String> htblClassURI = new Hashtable<>();
			while (results.next()) {

				String dynamicClassURI = results.getString("CLASSURI");
				String dynamicClassObjectProperty = results.getString("OBJECTPROPERTY");
				String objectPropertyRangeObjectURI = results.getString("OBJECTCLASSURI");
				String dynamicClassDatatypeProperty = results.getString("DATATYPEPROPERTY");
				String datatypePropertyRangeDatatypeURI = results.getString("DATATYPEURI");

				String objectPropertyHasMultipleValue = results.getString("hasMultipleValues1");
				String objectPropertyIsUnique = results.getString("isUnique1");

				String dataTypePropertyHasMultipleValue = results.getString("hasMultipleValues2");
				String dataTypePropertyIsUnique = results.getString("isUnique2");

				if (!htblClassURI.containsKey(dynamicClassURI)) {

					/*
					 * get className and URI
					 */
					String[] dynamicClassRes = getValueFromURI(dynamicClassURI);
					String dynamicClassName = dynamicClassRes[0];

					/*
					 * remove dynamic class from caches in order to cache it
					 * fully again. I used this to remove any corrupted or
					 * modified properties or old attributes of the cached
					 * dynamicClass
					 */
					if (DynamicOntologyMapper.getHtblappDynamicOntologyClasses().containsKey(applicationModelName)
							&& DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
									.containsKey(dynamicClassName)) {

						DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
								.remove(dynamicClassName);
						DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri().get(applicationModelName)
								.remove(dynamicClassURI);
					}

					/*
					 * cache dynamic Class then cache properties if exist
					 */
					cacheDynamicClass(dynamicClassURI, applicationModelName);
					htblClassURI.put(dynamicClassURI, dynamicClassURI);

					/*
					 * check that value of objectProperty column is not empty
					 * (not null) to cache it
					 */
					if (dynamicClassObjectProperty != null) {
						boolean hasMultipleValues = Boolean.parseBoolean(objectPropertyHasMultipleValue);
						boolean isUnique = Boolean.parseBoolean(objectPropertyIsUnique);

						cacheDynamicProperties(dynamicClassURI, PropertyType.ObjectProperty, dynamicClassObjectProperty,
								objectPropertyRangeObjectURI, applicationModelName, hasMultipleValues, isUnique);
					}

					/*
					 * check that value of dataTypeProperty column is not empty
					 * (not null) to cache it
					 */
					if (dynamicClassDatatypeProperty != null) {
						boolean hasMultipleValues = Boolean.parseBoolean(dataTypePropertyHasMultipleValue);
						boolean isUnique = Boolean.parseBoolean(dataTypePropertyIsUnique);

						cacheDynamicProperties(dynamicClassURI, PropertyType.DatatypeProperty,
								dynamicClassDatatypeProperty, datatypePropertyRangeDatatypeURI, applicationModelName,
								hasMultipleValues, isUnique);
					}

				} else {

					/*
					 * the class was cached before so I will only cache the
					 * properties
					 */

					/*
					 * check that value of objectProperty column is not empty
					 * (not null) to cache it
					 */
					if (dynamicClassObjectProperty != null) {
						boolean hasMultipleValues = Boolean.parseBoolean(objectPropertyHasMultipleValue);
						boolean isUnique = Boolean.parseBoolean(objectPropertyIsUnique);

						cacheDynamicProperties(dynamicClassURI, PropertyType.ObjectProperty, dynamicClassObjectProperty,
								objectPropertyRangeObjectURI, applicationModelName, hasMultipleValues, isUnique);
					}

					/*
					 * check that value of dataTypeProperty column is not empty
					 * (not null) to cache it
					 */
					if (dynamicClassDatatypeProperty != null) {
						boolean hasMultipleValues = Boolean.parseBoolean(dataTypePropertyHasMultipleValue);
						boolean isUnique = Boolean.parseBoolean(dataTypePropertyIsUnique);

						cacheDynamicProperties(dynamicClassURI, PropertyType.DatatypeProperty,
								dynamicClassDatatypeProperty, datatypePropertyRangeDatatypeURI, applicationModelName,
								hasMultipleValues, isUnique);
					}

				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(e.getMessage(), "Ontology");

		}

	}

	/**
	 * cacheDynamicClass is used to cache loaded dynamic classes in
	 * DynamicOntologyMapper caches
	 * 
	 * @param dynamicClassURI
	 *            holds the URI of the new class
	 * 
	 * @param applicationModelName
	 *            holds the name of the application model that stores the
	 *            application data in the default graph and the dynamic class or
	 *            properties added to the mainOntology in the ontologyGraph
	 */
	private void cacheDynamicClass(String dynamicClassURI, String applicationModelName) {

		String[] res = getValueFromURI(dynamicClassURI);
		String dynamicClassName = res[0].toLowerCase();
		String dynamicClassPrefixURI = res[1];
		Prefix prefix = getPrefix(dynamicClassPrefixURI);

		Class newClass = null;

		if (OntologyMapper.getOntologyMapper().getHtblMainOntologyClassesMappers()
				.containsKey(dynamicClassName.toLowerCase())) {

			Class mainOntologyClass = OntologyMapper.getHtblMainOntologyClassesMappers()
					.get(dynamicClassName.toLowerCase());

			try {
				newClass = (Class) mainOntologyClass.clone();
			} catch (CloneNotSupportedException e) {

				e.printStackTrace();
			}
		} else {
			newClass = new Class(dynamicClassName, dynamicClassURI, prefix);
		}

		if (DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri().containsKey(applicationModelName)) {

			DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri().get(applicationModelName).put(dynamicClassURI,
					newClass);
			DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName).put(dynamicClassName,
					newClass);
		} else {
			DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri().put(applicationModelName, new Hashtable<>());
			DynamicOntologyMapper.getHtblappDynamicOntologyClasses().put(applicationModelName, new Hashtable<>());

			DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri().get(applicationModelName).put(dynamicClassURI,
					newClass);
			DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName).put(dynamicClassName,
					newClass);
		}

	}

	/**
	 * cacheDynamicProperties is used to cache Dynamic properties in
	 * DynamicOntologyMapper caches
	 * 
	 * @param subjectClassURI
	 *            holds the URI of the domain class type
	 * 
	 * @param propertyURI
	 *            holds the URI of the property
	 * 
	 * @param propertyRangeURI
	 *            holds the URI of the domain class type if it is an
	 *            objectProperty or holds the dataTypeURI if it is a
	 *            dataTypeProperty
	 * @param applicationModelName
	 *            holds the name of the application model that stores the
	 *            application data in the default graph and the dynamic class or
	 *            properties added to the mainOntology in the ontologyGraph
	 */
	private void cacheDynamicProperties(String subjectClassURI, PropertyType propertyType, String propertyURI,
			String propertyRangeURI, String applicationModelName, boolean hasMultipleValued, boolean isUnique) {

		/*
		 * get propertyName and prefix by from propertyURI by calling
		 * getValueFromURI method
		 */
		String[] propertyRes = getValueFromURI(propertyURI);
		String propertyName = propertyRes[0];
		String propertyPreifxURI = propertyRes[1];
		Prefix propertyPrefix = getPrefix(propertyPreifxURI);

		/*
		 * get subjectClassName and prefix by from subjectClassURI by calling
		 * getValueFromURI method
		 */
		String[] subjectClassRes = getValueFromURI(subjectClassURI);
		String subjectClassName = subjectClassRes[0];
		String subjectClassPrefixURI = subjectClassRes[1];
		Prefix subjectClassPrefix = getPrefix(subjectClassPrefixURI);

		String objectClassName = null;
		XSDDatatype xsdDatatype = null;

		if (propertyType.equals(PropertyType.ObjectProperty)) {
			/*
			 * get objectClassName and prefix by from objectClassURI by calling
			 * getValueFromURI method
			 */
			String[] objectClassRes = getValueFromURI(propertyRangeURI);
			objectClassName = objectClassRes[0];

		} else {

			/*
			 * DatatypeProperty so get xsdDataType from objectClassPrefixURI
			 */
			xsdDatatype = getXSDDataTypeEnumOfXSDURI(propertyRangeURI);
		}

		/*
		 * check if application is in the cache be checking if
		 * DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri() has the
		 * applicationModelName
		 */
		if (DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri().containsKey(applicationModelName)) {

			Hashtable<String, Class> htblDynamicOntologyClassesURI = DynamicOntologyMapper
					.getHtblappDynamicOntologyClassesUri().get(applicationModelName);
			Hashtable<String, Class> htblDynamicOntologyClasses = DynamicOntologyMapper
					.getHtblappDynamicOntologyClasses().get(applicationModelName);
			/*
			 * check if the subjectClassURI exist or not in
			 * htblDynamicOntologyClassesURI
			 */
			if (htblDynamicOntologyClassesURI.containsKey(subjectClassURI)) {
				Class subjectClass = htblDynamicOntologyClassesURI.get(subjectClassURI);

				/*
				 * create new property
				 */
				Property newProperty = createProperty(subjectClass, propertyName, propertyPrefix, propertyType,
						xsdDatatype, objectClassName, hasMultipleValued, isUnique);
				/*
				 * add new property to subjectClass's proprtiesList
				 */
				subjectClass.getProperties().put(propertyName, newProperty);
				subjectClass.getHtblPropUriName().put(propertyURI, propertyName);

				addInheritedCachedPropertiesToSubClasses(subjectClassName, newProperty, applicationModelName);

			} else {

				/*
				 * the class does not exist so I will add it to
				 * htblDynamicOntologyClassesURI
				 */
				Class subjectClass = new Class(subjectClassName, subjectClassURI, subjectClassPrefix);
				htblDynamicOntologyClassesURI.put(subjectClassURI, subjectClass);

				/*
				 * also add it to htblDynamicOntologyClasses
				 */
				htblDynamicOntologyClasses.put(subjectClassName.toLowerCase(), subjectClass);

				/*
				 * create newProperty
				 */
				Property newProperty = createProperty(subjectClass, propertyName, propertyPrefix, propertyType,
						xsdDatatype, objectClassName, hasMultipleValued, isUnique);
				/*
				 * add new property to subjectClass's proprtiesList
				 */
				subjectClass.getProperties().put(propertyName, newProperty);
				subjectClass.getHtblPropUriName().put(propertyURI, propertyName);

				addInheritedCachedPropertiesToSubClasses(subjectClassName, newProperty, applicationModelName);

			}

		} else {
			/*
			 * first time to to cache dynamic classes or properties of this
			 * application.
			 * 
			 * create caches for this applicationModel with applicationModelName
			 */
			Hashtable<String, Class> htblDynamicOntologyClassesURI = new Hashtable<>();
			DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri().put(applicationModelName,
					htblDynamicOntologyClassesURI);

			Hashtable<String, Class> htblDynamicOntologyClasses = new Hashtable<>();
			DynamicOntologyMapper.getHtblappDynamicOntologyClasses().put(applicationModelName,
					htblDynamicOntologyClasses);

			/*
			 * the class does not exist so I will add it to
			 * htblDynamicOntologyClassesURI
			 */
			Class subjectClass = new Class(subjectClassName, subjectClassURI, subjectClassPrefix);
			htblDynamicOntologyClassesURI.put(subjectClassURI, subjectClass);

			/*
			 * also add it to htblDynamicOntologyClasses
			 */
			htblDynamicOntologyClasses.put(subjectClassName.toLowerCase(), subjectClass);

			Property newProperty = createProperty(subjectClass, propertyName, propertyPrefix, propertyType, xsdDatatype,
					objectClassName, hasMultipleValued, isUnique);
			/*
			 * add new property to subjectClass's proprtiesList
			 */
			subjectClass.getProperties().put(propertyName, newProperty);
			subjectClass.getHtblPropUriName().put(propertyURI, propertyName);

			addInheritedCachedPropertiesToSubClasses(subjectClassName, newProperty, applicationModelName);
		}
	}

	private void addInheritedCachedPropertiesToSubClasses(String subjectClassName, Property property,
			String applicationModelName) {

		if (OntologyMapper.getOntologyMapper().getHtblMainOntologyClassesMappers()
				.containsKey(subjectClassName.toLowerCase())) {

			Class subjectClass = OntologyMapper.getHtblMainOntologyClassesMappers().get(subjectClassName.toLowerCase());

			if (subjectClass.isHasTypeClasses()) {

				Iterator<String> htblTypeClassesIter = subjectClass.getClassTypesList().keySet().iterator();
				while (htblTypeClassesIter.hasNext()) {

					String subClassName = htblTypeClassesIter.next();
					Class subClass = subjectClass.getClassTypesList().get(subClassName);

					if (!DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get(applicationModelName)
							.containsKey(subClassName.toLowerCase())) {
						cacheDynamicClass(subClass.getUri(), applicationModelName);
					}
					DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri().get(applicationModelName)
							.get(subClass.getUri()).getProperties().put(property.getName(), property);
					DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri().get(applicationModelName)
							.get(subClass.getUri()).getHtblPropUriName()
							.put(property.getPrefix().getUri() + property.getName(), property.getName());

				}
			}
		}

	}

	/**
	 * createProperty is used to create a new property based
	 * on @param(propertyType)
	 * 
	 * @param subjectClass
	 *            is the subjectClass of the property
	 * 
	 * @param propertyName
	 *            property name of the new property
	 * 
	 * @param propertyPrefix
	 *            property prefix of the new property
	 * 
	 * @param propertyType
	 *            property type of the new property (ObjectProperty or
	 *            DatatypeProperty)
	 * 
	 * @param xsdDatatype
	 *            XSDdataType of property if it is DatatypeProperty else it will
	 *            be null
	 * 
	 * @param objectClassName
	 *            objectClassName of property if it is ObjectProperty else it
	 *            will be null
	 * 
	 * @param mulitpleValues
	 *            mulitpleValues is a boolean the tells if the new property has
	 *            multipleValues or not
	 * 
	 * @param unique
	 *            unique is a boolean the tells if the new property has unique
	 *            or not
	 * 
	 * @return the new created property
	 */
	private Property createProperty(Class subjectClass, String propertyName, Prefix propertyPrefix,
			PropertyType propertyType, XSDDatatype xsdDatatype, String objectClassName, boolean mulitpleValues,
			boolean unique) {
		Property newProperty;

		if (propertyType.equals(PropertyType.DatatypeProperty)) {
			newProperty = new DataTypeProperty(subjectClass, propertyName, propertyPrefix, xsdDatatype, mulitpleValues,
					unique);
		} else {
			/*
			 * objectProperty
			 */
			newProperty = new ObjectProperty(subjectClass, propertyName, propertyPrefix, objectClassName,
					mulitpleValues, unique);
		}

		return newProperty;
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

	private String getNameFromPrefixedName(String prefixedName) {
		int index = prefixedName.indexOf(":");
		return prefixedName.substring(index + 1);
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
	private String[] getValueFromURI(String uri) {

		String[] res = new String[2];

		for (Prefix prefix : Prefix.values()) {
			if (uri.contains(prefix.getUri())) {
				res[0] = uri.substring(prefix.getUri().length());
				res[1] = prefix.getUri();
			}
		}
		return res;
	}

	/**
	 * getXSDDataTypeEnumOfDataTypeName return XsdDataType enum instance
	 * of @param(dataTypeName)
	 * 
	 * @param dataTypeName
	 *            holds datatypeName eg. string,integer
	 * @return
	 */
	private XSDDatatype getXSDDataTypeEnumOfDataTypeName(String dataTypeName) {

		if (XSDDatatype.boolean_type.getDataType().equals(dataTypeName)) {
			return XSDDatatype.boolean_type;
		}

		if (XSDDatatype.decimal_typed.getDataType().equals(dataTypeName)) {
			return XSDDatatype.decimal_typed;
		}

		if (XSDDatatype.float_typed.getDataType().equals(dataTypeName)) {
			return XSDDatatype.float_typed;
		}

		if (XSDDatatype.integer_typed.getDataType().equals(dataTypeName)) {
			return XSDDatatype.integer_typed;
		}

		if (XSDDatatype.string_typed.getDataType().equals(dataTypeName)) {
			return XSDDatatype.string_typed;
		}

		if (XSDDatatype.dateTime_typed.getDataType().equals(dataTypeName)) {
			return XSDDatatype.dateTime_typed;
		}

		if (XSDDatatype.double_typed.getDataType().equals(dataTypeName)) {
			return XSDDatatype.double_typed;
		}

		return null;
	}

	/**
	 * getXSDDataTypeEnumOfXSDURI return XsdDataType enum instance
	 * of @param(xsdURI)
	 * 
	 * @param xsdURI
	 *            holds xsdURI eg. http://www.w3.org/2001/XMLSchema#string
	 * @return
	 */
	private XSDDatatype getXSDDataTypeEnumOfXSDURI(String xsdURI) {

		if (XSDDatatype.boolean_type.getXsdTypeURI().equals(xsdURI)) {
			return XSDDatatype.boolean_type;
		}

		if (XSDDatatype.decimal_typed.getXsdTypeURI().equals(xsdURI)) {
			return XSDDatatype.decimal_typed;
		}

		if (XSDDatatype.float_typed.getXsdTypeURI().equals(xsdURI)) {
			return XSDDatatype.float_typed;
		}

		if (XSDDatatype.integer_typed.getXsdTypeURI().equals(xsdURI)) {
			return XSDDatatype.integer_typed;
		}

		if (XSDDatatype.string_typed.getXsdTypeURI().equals(xsdURI)) {
			return XSDDatatype.string_typed;
		}

		if (XSDDatatype.dateTime_typed.getXsdTypeURI().equals(xsdURI)) {
			return XSDDatatype.dateTime_typed;
		}

		if (XSDDatatype.double_typed.getXsdTypeURI().equals(xsdURI)) {
			return XSDDatatype.double_typed;
		}

		return null;
	}

	public static void main(String[] args) {
		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		String szUser = "rdfusr";
		String szPasswd = "rdfusr";

		Oracle oracle = new Oracle(szJdbcURL, szUser, szPasswd);

		DynamicOntologyDao dynamicOntologyDao = new DynamicOntologyDao(oracle);
		System.out.println("Database Connected");
		dynamicOntologyDao.addNewClassToOntology("VirtualSensor", "TESTAPPLICATION_MODEL");
		dynamicOntologyDao.addNewObjectPropertyToOntology("virtual", "ssn:Device", "iot-platform:VirtualSensor", true,
				true, "TESTAPPLICATION_MODEL");
		dynamicOntologyDao.addNewDatatypePropertyToOntology("macAddress", "iot-platform:VirtualSensor", "string", false,
				true, "TESTAPPLICATION_MODEL");

		dynamicOntologyDao.addNewDatatypePropertyToOntology("job", "iot-platform:Developer", "string", false, false,
				"TESTAPPLICATION_MODEL");

		dynamicOntologyDao.addNewObjectPropertyToOntology("hates", "foaf:Person", "foaf:Person", true, false,
				"TESTAPPLICATION_MODEL");

		dynamicOntologyDao.addNewObjectPropertyToOntology("loves", "foaf:Person", "foaf:Person", true, false,
				"TESTAPPLICATION_MODEL");

		ArrayList<String> dyanmicClassesNameList = new ArrayList<>();
		dyanmicClassesNameList.add("VirtualSensor");
		dyanmicClassesNameList.add("Device");
		dyanmicClassesNameList.add("Person");
		dyanmicClassesNameList.add("Developer");

		dynamicOntologyDao.loadAndCacheDynamicClassesofApplicationDomain("TESTAPPLICATION_MODEL",
				dyanmicClassesNameList);

		// dynamicOntologyDao.loadAndCacheAllApplicationDynamicOntologyClasses("TESTAPPLICATION_MODEL");
		// dynamicOntologyDao.loadAndCacheAllApplicationDynamicOntologyObjectProperties("TESTAPPLICATION_MODEL");
		// dynamicOntologyDao.loadAndCacheAllApplicationDynamicOntologyDataTypeProperties("TESTAPPLICATION_MODEL");

		System.out.println(DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri().get("TESTAPPLICATION_MODEL"));
		System.out.println(DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri().get("TESTAPPLICATION_MODEL")
				.get("http://iot-platform#VirtualSensor").getProperties());

		System.out.println(DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get("TESTAPPLICATION_MODEL"));
		System.out.println(DynamicOntologyMapper.getHtblappDynamicOntologyClasses().get("TESTAPPLICATION_MODEL")
				.get("device").getProperties());

		System.out.println("--> " + DynamicOntologyMapper.getHtblappDynamicOntologyClasses()
				.get("TESTAPPLICATION_MODEL").get("person").getProperties());

		System.out.println("--> " + DynamicOntologyMapper.getHtblappDynamicOntologyClasses()
				.get("TESTAPPLICATION_MODEL").get("admin").getProperties());

		System.out.println("==> " + DynamicOntologyMapper.getHtblappDynamicOntologyClasses()
				.get("TESTAPPLICATION_MODEL").get("developer").getUniqueIdentifierPropertyName());

	}

}
