package com.iotplatform.ontology.mapers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;

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
		}

	}

	/*
	 * createClassInstance method is used to map a ontology class into Class (in
	 * com.iotplatform.ontology package) instance and add it to
	 * htblMainOntologyClasses
	 */
	private static void createClassInstance(OntClass ontologyClass) {

	}

	public static void main(String[] args) {
		OntologyMapper ontologyMapper = new OntologyMapper();
		ontologyMapper.readOntology();
		ontologyMapper.getMainOntologyProperties();
		System.out.println(ontologyMapper.htblMainOntologyProperties.size());
	}

}
