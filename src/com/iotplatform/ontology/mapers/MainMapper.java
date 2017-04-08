package com.iotplatform.ontology.mapers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.ontology.AllValuesFromRestriction;
import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.CardinalityRestriction;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.HasValueRestriction;
import org.apache.jena.ontology.MaxCardinalityRestriction;
import org.apache.jena.ontology.MinCardinalityRestriction;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.ontology.SomeValuesFromRestriction;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.springframework.stereotype.Component;

@Component
public class MainMapper {

	public static void readOntology(OntModel model) {
		try {
			InputStream is = FileManager.get().open("iot-platform.n3");
			model.read(new InputStreamReader(is), "http://iot-platform", "TURTLE");
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Traverse the Ontology to find all given concepts
	 */
	public static void traverseStart(OntModel model, OntClass ontClass) {
		// if ontClass is specified we only traverse down that branch
		// if (ontClass != null) {
		// traverse(ontClass, new ArrayList<OntClass>(), 0);
		// return;
		// }

		// create an iterator over the root classes
		// Iterator<OntClass> i = model.listHierarchyRootClasses();
		// System.out.println(model.getOntClass("http://purl.oclc.org/NET/ssnx/ssn#Observation").getLocalName());
		// // traverse through all roots
		// while (i.hasNext()) {
		// OntClass tmp = i.next();
		// System.out.println(tmp.toString());
		//// traverse(tmp, new ArrayList<OntClass>(), 0);
		// }

		ExtendedIterator<AnnotationProperty> annotationsItr = model.listAnnotationProperties();
		while (annotationsItr.hasNext()) {
			AnnotationProperty annotationProperty = annotationsItr.next();
			System.out.println("Annotation Property : " + annotationProperty.getURI());
		}

		ExtendedIterator<OntClass> itr = model.listClasses();

		while (itr.hasNext()) {
			OntClass c = itr.next();
			if (c.getLocalName() == null)
				continue;

			if (c.getLocalName().equals("Group")) {
				ExtendedIterator<OntProperty> propertyItr = c.listDeclaredProperties();
				System.out.println("--->>"+c.getIsDefinedBy().getLocalName());
				while (propertyItr.hasNext()) {
					OntProperty prop = propertyItr.next();
					if (prop.getLocalName().equals("uniqueIdentifierProperty")) {
						System.out.println(prop.isAnnotationProperty());
						AnnotationProperty annotationProperty = prop.asAnnotationProperty();
						
						
					}
				}

				ExtendedIterator<OntClass> subClassItr = c.listSubClasses();

				while (subClassItr.hasNext()) {
					OntClass subClass = subClassItr.next();
					if (subClass != null)
						System.out.println("SubClass : " + subClass.toString());
				}

				ExtendedIterator<OntClass> superClassItr = c.listSuperClasses();
				while (superClassItr.hasNext()) {
					OntClass superClass = superClassItr.next();
					if (superClass != null) {

						if (superClass.isRestriction()) {

							Restriction restriction = superClass.asRestriction();
							OntProperty prop = restriction.getOnProperty();

							System.out.println("Restriction Property : " + prop.getLocalName());

							if (restriction.isSomeValuesFromRestriction()) {
								SomeValuesFromRestriction someValuesFromRestriction = restriction
										.asSomeValuesFromRestriction();
								System.out.println("Some values of Object class : "
										+ someValuesFromRestriction.getSomeValuesFrom());
							}

							if (restriction.isAllValuesFromRestriction()) {
								AllValuesFromRestriction allValuesFromRestriction = restriction
										.asAllValuesFromRestriction();
								System.out.println(
										"All Values of Object class : " + allValuesFromRestriction.getAllValuesFrom());
							}

							if (restriction.isHasValueRestriction()) {
								HasValueRestriction hasValueRestriction = restriction.asHasValueRestriction();
								System.out.println(
										"Value Restriction Object Class : " + hasValueRestriction.getHasValue());
							}

							if (restriction.isMaxCardinalityRestriction()) {
								MaxCardinalityRestriction maxCardinalityRestriction = restriction
										.asMaxCardinalityRestriction();
								System.out.println(
										"Max Cardinality number : " + maxCardinalityRestriction.getMaxCardinality());
							}

							if (restriction.isMinCardinalityRestriction()) {
								MinCardinalityRestriction minCardinalityRestriction = restriction
										.asMinCardinalityRestriction();
								System.out.println(
										"Min Cardinality number : " + minCardinalityRestriction.getMinCardinality());
							}

							if (restriction.isCardinalityRestriction()) {
								CardinalityRestriction cardinalityRestriction = restriction.asCardinalityRestriction();
								System.out.println(" Cardinality number : " + cardinalityRestriction.getCardinality());
							}

						} else {
							System.out.println("---> " + superClass.getLocalName());
						}

					}
					System.out.println();

				}

			}

			// if(c.getLocalName() != null)
			// System.out.println(c.getLocalName());
		}

		// System.out.println("===================================================================================");
		//
		// ExtendedIterator<ObjectProperty> objPropertyItr =
		// model.listObjectProperties();
		// while (objPropertyItr.hasNext()) {
		// ObjectProperty objectProperty = objPropertyItr.next();
		// System.out.println("ObjectProperty : " +
		// objectProperty.getLocalName());
		// }
		//
		// System.out.println("===================================================================================");
		//
		// ExtendedIterator<DatatypeProperty> dataTypePropertyItr =
		// model.listDatatypeProperties();
		// while (dataTypePropertyItr.hasNext()) {
		// DatatypeProperty datatypeProperty = dataTypePropertyItr.next();
		// System.out.println("DatatypeProperty : " +
		// datatypeProperty.getLocalName());
		// }

		// ExtendedIterator<Restriction> restrictionIter =
		// model.listRestrictions();
		// while(restrictionIter.hasNext()){
		// Restriction restriction = restrictionIter.next();
		// System.out.println(restriction.getOnProperty().getLocalName());
		// }
	}

	/**
	 * Start from a class, then recurse down to the sub-classes. Use occurs
	 * check to prevent getting stuck in a loop
	 * 
	 * @param oc
	 *            OntClass to traverse from
	 * @param occurs
	 *            stores visited nodes
	 * @param depth
	 *            indicates the graph "depth"
	 * @return list of concepts / entities which were visited when recursing
	 *         through the hierarchy (avoid loops)
	 */
	private static void traverse(OntClass oc, List<OntClass> occurs, int depth) {

		if (oc == null)
			return;

		// if end reached abort (Thing == root, Nothing == deadlock)
		if (oc.getLocalName() == null || oc.getLocalName().equals("Nothing"))
			return;

		// print depth times "\t" to retrieve a explorer tree like output
		for (int i = 0; i < depth; i++) {
			System.out.print("\t");
		}

		// print out the OntClass
		System.out.println(oc.toString());

		// check if we already visited this OntClass (avoid loops in graphs)
		if (oc.canAs(OntClass.class) && !occurs.contains(oc)) {
			// for every subClass, traverse down
			for (Iterator<OntClass> i = oc.listSubClasses(true); i.hasNext();) {
				OntClass subClass = i.next();

				// push this expression on the occurs list before we recurse to
				// avoid loops
				occurs.add(oc);
				// traverse down and increase depth (used for logging tabs)
				traverse(subClass, occurs, depth + 1);
				// after traversing the path, remove from occurs list
				occurs.remove(oc);
			}
		}

	}

	public static void main(String[] args) {
		// read ontology turtle file for file system
		OntModel model = ModelFactory.createOntologyModel();
		readOntology(model);

		// start traverse
		traverseStart(model, null);
	}

}
