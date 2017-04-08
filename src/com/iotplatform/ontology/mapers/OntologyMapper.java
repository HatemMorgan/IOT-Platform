package com.iotplatform.ontology.mapers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.Prefix;

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

			if (ontologyClass.isClass() && ontologyClass.getURI() != null) {

				if (ontologyClass.hasSuperClass())
					addSuperClasses(ontologyClass);
			}
		}

	}

	private static void addSuperClasses(OntClass ontologyClass) {

		/*
		 * get ontology class mapper
		 */
		Class ontologyClassMapper = htblMainOntologyClasses.get(ontologyClass.getLocalName());

		/*
		 * get superClass list
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

		for (Class superClass : htblMainOntologyClasses.get("Resolution").getSuperClassesList()) {
			System.out.println(superClass.getName());
		}
		
	}

}
