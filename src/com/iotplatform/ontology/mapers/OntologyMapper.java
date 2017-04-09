package com.iotplatform.ontology.mapers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.jena.ontology.AllValuesFromRestriction;
import org.apache.jena.ontology.CardinalityRestriction;
import org.apache.jena.ontology.HasValueRestriction;
import org.apache.jena.ontology.MaxCardinalityRestriction;
import org.apache.jena.ontology.MinCardinalityRestriction;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.ontology.SomeValuesFromRestriction;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.PropertyType;

/*
 * OntologyMapper is used to map the main ontology into some data structures that holds instances which 
 * represents the ontology concepts (eg. classes, properties).
 */

@Component
public class OntologyMapper {

	private static OntModel model;
	private static Hashtable<String, Class> htblMainOntologyClasses;
	private static Hashtable<String, OntProperty> htblMainOntologyProperties;

	public OntologyMapper() {
		model = ModelFactory.createOntologyModel();
		htblMainOntologyClasses = new Hashtable<>();
		htblMainOntologyProperties = new Hashtable<>();

	}

	/*
	 * readOntology method is used to read main ontology turtle file into jena
	 * model to be able to parse it
	 */
	private static void readOntology() {
		try {
			InputStream is = FileManager.get().open("iot-platform.n3");
			model.read(new InputStreamReader(is), "http://iot-platform", "TURTLE");
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * getMainOntologyProperties is used to load all the properties into
	 * htblMainOntologyProperties hashtable
	 */
	private static void getMainOntologyProperties() {
		ExtendedIterator<OntProperty> ontologyPropertiesIter = model.listAllOntProperties();

		while (ontologyPropertiesIter.hasNext()) {
			OntProperty property = ontologyPropertiesIter.next();

			/*
			 * skip annotation properties to only add objectProperties and
			 * DatatypeProperties
			 */
			if (!property.isDatatypeProperty() && !property.isObjectProperty())
				continue;

			htblMainOntologyProperties.put(property.getURI(), property);
		}

	}

	private static void getMainOntologyClasses() {
		ExtendedIterator<OntClass> ontologyClassesIter = model.listClasses();

		while (ontologyClassesIter.hasNext()) {
			OntClass ontologyClass = ontologyClassesIter.next();
			if (ontologyClass.isClass() && ontologyClass.getURI() != null) {

				/*
				 * Skip ontology class with uri rdfs:class because it is not one
				 * of the main ontology class it is only used by the ontology to
				 * tell that rdfs:Class is a owl:Class
				 */
				if (ontologyClass.getLocalName().equals("Class"))
					continue;

				/*
				 * get className , classURI and classPrefix
				 */
				String className = ontologyClass.getLocalName();
				String classUri = ontologyClass.getURI();
				Prefix classPrefix = getPrefix(ontologyClass.getNameSpace());

				/*
				 * create a new Class instance
				 */
				Class ontologyClassMapper = new Class(className, classUri, classPrefix);

				/*
				 * add new ontologyClassMapper to htblMainOntologyClasses
				 */
				htblMainOntologyClasses.put(className, ontologyClassMapper);
			}
		}

	}

	private static void completeCreationOfOntologyClassesMappers() {
		ExtendedIterator<OntClass> ontologyClassesIter = model.listClasses();

		while (ontologyClassesIter.hasNext()) {
			OntClass ontologyClass = ontologyClassesIter.next();

			/*
			 * check that ontology class is a proper owl:class not a restriction
			 * or blank nodes
			 */
			if (ontologyClass.isClass() && ontologyClass.getURI() != null) {
				/*
				 * Skip ontology class with uri rdfs:class because it is not one
				 * of the main ontology class it is only used by the ontology to
				 * tell that rdfs:Class is a owl:Class
				 */
				if (ontologyClass.getLocalName().equals("Class"))
					continue;

				/*
				 * populate superClassList if the given ontology class has
				 * superClasses
				 */
				if (ontologyClass.hasSuperClass())
					addSuperClasses(ontologyClass);

				/*
				 * populate subClassesList if the given ontology class has
				 * subClasses
				 */
				if (ontologyClass.hasSubClass())
					addSubClasses(ontologyClass);
			}
		}

	}

	private static void addSuperClasses(OntClass ontologyClass) {

		/*
		 * get ontology class mapper
		 */
		Class ontologyClassMapper = htblMainOntologyClasses.get(ontologyClass.getLocalName());

		/*
		 * populate superClass list
		 */
		ExtendedIterator<OntClass> superClassesIter = ontologyClass.listSuperClasses();
		while (superClassesIter.hasNext()) {
			OntClass superClass = superClassesIter.next();

			if (superClass.isClass() && superClass.getURI() != null) {
				String superClassName = superClass.getLocalName();

				/*
				 * resource is the root class of ontology (like Thing class ) so
				 * I will skip it because I do not need to add it to superClass
				 * list of any classMapper
				 */
				if (superClassName.equals("Resource"))
					continue;

				Class superClassMapper = htblMainOntologyClasses.get(superClassName);
				ontologyClassMapper.getSuperClassesList().add(superClassMapper);
			}

		}

	}

	private static void addSubClasses(OntClass ontologyClass) {

		/*
		 * get ontology class mapper
		 */
		Class ontologyClassMapper = htblMainOntologyClasses.get(ontologyClass.getLocalName());

		/*
		 * set hasTypeClasses boolean in the ontologyClassMapper
		 */
		ontologyClassMapper.setHasTypeClasses(true);

		/*
		 * populate subCLass list
		 */
		ExtendedIterator<OntClass> subClassesIter = ontologyClass.listSubClasses();
		while (subClassesIter.hasNext()) {
			OntClass subClass = subClassesIter.next();

			if (subClass.isClass() && subClass.getURI() != null) {
				String subClassName = subClass.getLocalName();

				/*
				 * resource is the root class of ontology (like Thing class ) so
				 * I will skip it because I do not need to add it to superClass
				 * list of any classMapper
				 */
				if (subClassName.equals("Resource"))
					continue;

				Class subClassMapper = htblMainOntologyClasses.get(subClassName);
				ontologyClassMapper.getClassTypesList().put(subClassName, subClassMapper);
			}

		}

	}

	private static void addPropertiesFromRestrictions(String classMapperName, Restriction restriction) {

		/*
		 * get property associated with restriction
		 */
		OntProperty prop = restriction.getOnProperty();
		/*
		 * All the Value restriction ( like owl:someValuesFrom ,
		 * owl:allValuesFrom, owl:hasValue) the property must be an
		 * objectProperty
		 * 
		 * Cardinality constraint restriction (like owl:qualifiedCardinality,
		 * owl:maxQualifiedCardinality ,owl:minQualifiedCardinality ,
		 * owl:minCardinality and owl:maxCardinality) the property associated
		 * with this restriction can be ObjectProperty or DataTypeProperty
		 * 
		 * I will not check for Cardinality constraint restriction because I
		 * need to know the range of the property either the if it is a class or
		 * datatype so I will check for Value constraint restrictions only
		 * 
		 */
		if (prop.isObjectProperty()) {

			String objectClassName = "";

			if (restriction.isSomeValuesFromRestriction()) {
				SomeValuesFromRestriction someValuesFromRestriction = restriction.asSomeValuesFromRestriction();
				objectClassName = someValuesFromRestriction.getSomeValuesFrom().getLocalName();
			}

			if (restriction.isAllValuesFromRestriction()) {
				AllValuesFromRestriction allValuesFromRestriction = restriction.asAllValuesFromRestriction();
				objectClassName = allValuesFromRestriction.getAllValuesFrom().getLocalName();
			}

			if (restriction.isHasValueRestriction()) {
				HasValueRestriction hasValueRestriction = restriction.asHasValueRestriction();

				if (hasValueRestriction.getHasValue().isResource())
					objectClassName = hasValueRestriction.getHasValue().asResource().getLocalName();
			}

			/*
			 * check that their is a objectClass for object Property
			 */
			if (objectClassName != "") {

				Class subjectClass = htblMainOntologyClasses.get(classMapperName);
				boolean isPropertyUnique = isPropertyUnique(prop.getURI(), PropertyType.ObjectProperty.toString());
				String propName = prop.getLocalName();
				Prefix prefix = getPrefix(prop.getNameSpace());
				Class ObjectClass = htblMainOntologyClasses.get(objectClassName);

				/*
				 * create new ObjectProperty. I will make the default for
				 * multipleValues true untill their is a macCardinalty
				 * restriction with value 1
				 */
				ObjectProperty objectProperty = new ObjectProperty(subjectClass, prop.getLocalName(), prefix,
						ObjectClass, true, isPropertyUnique);
				subjectClass.getProperties().put(propName, objectProperty);

			}

		} else {

			/*
			 * if property is a datatype property so I have to get its dataType
			 * from htblMainOntologyProperties if the restriction does not have
			 * the dataType
			 */
			if (prop.isDatatypeProperty()) {

			}
		}
	}

	private static boolean isPropertyUnique(String propertyUri, String propertyType) {

		StringBuilder queryBuilder = new StringBuilder();
		for (Prefix prefix : Prefix.values()) {
			queryBuilder.append("PREFIX	" + prefix.getPrefix() + "	<" + prefix.getUri() + ">\n");
		}

		String queryStr = "SELECT (COUNT(*) as ?isFound ) \n WHERE { <" + propertyUri + ">   a   owl:" + propertyType
				+ " ; \n " + "iot-platform:isUnique  \"true\"^^xsd:boolean . \n" + "}";

		queryBuilder.append(queryStr);
		// System.out.println(queryBuilder.toString());
		Query query = QueryFactory.create(queryBuilder.toString());

		QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
		ResultSet resultSet = queryExecution.execSelect();

		QuerySolution sol = resultSet.next();
		int isFound = sol.get("isFound").asLiteral().getInt();

		if (isFound == 0) {
			return false;
		}

		return true;
	}

	private static String isClassHasUniqueIdentifier(String classUri) {

		StringBuilder queryBuilder = new StringBuilder();
		for (Prefix prefix : Prefix.values()) {
			queryBuilder.append("PREFIX	" + prefix.getPrefix() + "	<" + prefix.getUri() + ">\n");
		}

		String queryStr = "SELECT ?uniqueIdentifierProp \n WHERE { <" + classUri + ">   a   owl:Class \n" + " ; \n "
				+ "iot-platform:hasUniqueIdentifier  ?uniqueIdentifierProp . \n" + "}";

		queryBuilder.append(queryStr);
		// System.out.println(queryBuilder.toString());
		Query query = QueryFactory.create(queryBuilder.toString());

		QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
		ResultSet resultSet = queryExecution.execSelect();
		if (resultSet.hasNext()) {
			QuerySolution sol = resultSet.next();
			String uniqueIdentifierPropName = sol.get("uniqueIdentifierProp").asResource().getLocalName();
			return uniqueIdentifierPropName;
		}
		return null;
	}

	/*
	 * get Prefix enum that maps the String nameSpace from a dynamicProperty
	 */
	private static Prefix getPrefix(String nameSpace) {

		if (Prefix.FOAF.getUri().equals(nameSpace)) {
			return Prefix.FOAF;
		}

		if (Prefix.SSN.getUri().equals(nameSpace)) {
			return Prefix.SSN;
		}

		if (Prefix.IOT_LITE.getUri().equals(nameSpace)) {
			return Prefix.IOT_LITE;
		}

		if (Prefix.IOT_PLATFORM.getUri().equals(nameSpace)) {
			return Prefix.IOT_PLATFORM;
		}

		if (Prefix.GEO.getUri().equals(nameSpace)) {
			return Prefix.GEO;
		}

		if (Prefix.XSD.getUri().equals(nameSpace)) {
			return Prefix.XSD;
		}

		if (Prefix.OWL.getUri().equals(nameSpace)) {
			return Prefix.OWL;
		}

		if (Prefix.RDFS.getUri().equals(nameSpace)) {
			return Prefix.RDFS;
		}

		if (Prefix.RDF.getUri().equals(nameSpace)) {
			return Prefix.RDF;
		}

		if (Prefix.QU.getUri().equals(nameSpace)) {
			return Prefix.QU;
		}

		if (Prefix.DUL.getUri().equals(nameSpace)) {
			return Prefix.DUL;
		}

		return null;
	}

	public static void main(String[] args) {
		OntologyMapper ontologyMapper = new OntologyMapper();
		ontologyMapper.readOntology();
		ontologyMapper.getMainOntologyProperties();
		ontologyMapper.getMainOntologyClasses();
		ontologyMapper.completeCreationOfOntologyClassesMappers();
		System.out.println(ontologyMapper.htblMainOntologyClasses.size());
		System.out.println(ontologyMapper.htblMainOntologyProperties.size());

		System.out.println(ontologyMapper.isPropertyUnique("http://xmlns.com/foaf/0.1/name",
				PropertyType.DatatypeProperty.toString()));

		System.out.println(ontologyMapper.isClassHasUniqueIdentifier("http://xmlns.com/foaf/0.1/Group"));

		// for (Class superClass :
		// htblMainOntologyClasses.get("Resolution").getSuperClassesList()) {
		// System.out.println(superClass.getName());
		// }

		// System.out.println(htblMainOntologyClasses.get("Coverage").getClassTypesList().toString());
	}

}
