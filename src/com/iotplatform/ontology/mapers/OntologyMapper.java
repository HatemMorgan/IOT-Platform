package com.iotplatform.ontology.mapers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.jena.ontology.AllValuesFromRestriction;
import org.apache.jena.ontology.HasValueRestriction;
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
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.PropertyType;
import com.iotplatform.ontology.XSDDatatype;

/*
 * OntologyMapper is used to map the main ontology into some data structures that holds instances which 
 * represents the ontology concepts (eg. classes, properties).
 */

@Component
public class OntologyMapper {

	private static OntModel model;
	private static Hashtable<String, Class> htblMainOntologyClasses;
	private static Hashtable<String, Class> htblMainOntologyClassesUri;
	private static Hashtable<String, OntProperty> htblMainOntologyProperties;

	private static OntologyMapper ontologyMapper;

	public static OntologyMapper getOntologyMapper() {
		if (ontologyMapper == null)
			ontologyMapper = new OntologyMapper();

		return ontologyMapper;
	}

	public OntologyMapper() {
		model = ModelFactory.createOntologyModel();
		htblMainOntologyClasses = new Hashtable<>();
		htblMainOntologyProperties = new Hashtable<>();
		htblMainOntologyClassesUri = new Hashtable<>();

		/*
		 * read main ontology from iot-platform.n3 (ontology turtle file)
		 */
		readOntology();

		/*
		 * load mainOtology classes
		 */
		getMainOntologyClasses();

		/*
		 * load mainOtology properties
		 */
		getMainOntologyProperties();

		/*
		 * complete mapping by added properties to classesMappers and add map
		 * class relation hierarchy
		 */
		completeCreationOfOntologyClassesMappers();
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

	/*
	 * load all ontology classes (it will load owl:class instances , blank nodes
	 * and restriction classes)
	 */
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
				htblMainOntologyClasses.put(className.toLowerCase(), ontologyClassMapper);
				htblMainOntologyClassesUri.put(classUri, ontologyClassMapper);
			}
		}

	}

	/*
	 * completeCreationOfOntologyClassesMappers is used to complete classMapper
	 * creation by adding subClasses, superClasses, properties , properties
	 * inheritance from superClasses and add uniqueIdentifier property if exist
	 * and add id property if not exist
	 */
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
				 * populate subClassesList if the given ontology class has
				 * subClasses
				 */
				if (ontologyClass.hasSubClass())
					addSubClasses(ontologyClass);

				/*
				 * populate superClassList if the given ontology class has
				 * superClasses
				 */
				if (ontologyClass.hasSuperClass())
					addSuperClasses(ontologyClass);

			}
		}

		/*
		 * add Properties
		 */
		addPropertiesToClassesMappersUsingDomainAndRange();

		/*
		 * add inherited Properties from superClasses to subClasses
		 */
		addInheritedPropertiesToSubClasses();

		/*
		 * add uniqueIdentifier to ontology class mappers
		 */
		addUniqueIdentifierToOntologyClassesMappers();

	}

	/*
	 * addUniqueIdentifierToOntologyClassesMappers method is used to add unique
	 * identifier properties to classes if they have and also make the
	 * subClasses of inherit unique identifier
	 * 
	 * It adds Id property for classes that does not have any uniqueIdentifier
	 * properties in order to allow the system to create a unique id as a
	 * uniqueIdentifer of individual of that class and attach the value to id
	 * property
	 */
	private static void addUniqueIdentifierToOntologyClassesMappers() {

		Iterator<String> htblMainOntologyClassesIter = htblMainOntologyClasses.keySet().iterator();

		while (htblMainOntologyClassesIter.hasNext()) {
			String className = htblMainOntologyClassesIter.next();
			Class ontologyClassMapper = htblMainOntologyClasses.get(className);

			/*
			 * add uniqueIdentifierProperty if it exists
			 */
			String uniqueIdentifierPropertyName = isClassHasUniqueIdentifier(ontologyClassMapper.getUri());

			/*
			 * check that uniqueIdentifierPropertyName is not null and this
			 * class has no uniqueIdentifier (it might be added to it if one of
			 * its superClasses has a uniqueIdentifier)
			 */
			if (uniqueIdentifierPropertyName != null && !ontologyClassMapper.isHasUniqueIdentifierProperty()) {

				ontologyClassMapper.setHasUniqueIdentifierProperty(true);
				ontologyClassMapper.setUniqueIdentifierPropertyName(uniqueIdentifierPropertyName);

				/*
				 * add uniqueIdentifier to subClasses if exist because
				 * subClasses have to inherit uniqueIdentifer of its superClass
				 */
				if (ontologyClassMapper.isHasTypeClasses()) {
					Iterator<String> htbSubClassesIter = ontologyClassMapper.getClassTypesList().keySet().iterator();

					while (htbSubClassesIter.hasNext()) {
						Class subClassMapper = htblMainOntologyClasses.get(htbSubClassesIter.next().toLowerCase());
						subClassMapper.setHasUniqueIdentifierProperty(true);
						subClassMapper.setUniqueIdentifierPropertyName(uniqueIdentifierPropertyName);

						/*
						 * remove id property if it was added to subClassMapper
						 * before
						 */
						subClassMapper.getProperties().remove("id");
						ontologyClassMapper.getHtblPropUriName().remove(Prefix.IOT_LITE.getUri() + "id");
					}

				}

			} else {

				/*
				 * check that this class has no uniqueIdentifier (it might be
				 * added to it if one of its superClasses has a
				 * uniqueIdentifier)
				 */
				if (ontologyClassMapper.isHasUniqueIdentifierProperty()) {
					continue;
				}

				/*
				 * if foundUniqueIdentifier = false this mean that this class
				 * has no uniqueIdentfifier so the platform has to generate an
				 * id for it
				 * 
				 * I will add an id property for this classes to present the id
				 * of an individual
				 */
				DataTypeProperty dataTypeProperty = new DataTypeProperty(ontologyClassMapper, "id", Prefix.IOT_LITE,
						XSDDatatype.string_typed, false, false);
				ontologyClassMapper.getProperties().put("id", dataTypeProperty);
				ontologyClassMapper.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "id", "id");
			}

		}

	}

	/*
	 * addSuperClasses it iterates on the superClass list of passed
	 * ontologyClass
	 * 
	 * it adds the superClass if it is an instance of owl:class to superClass
	 * list of the mapper of the passed ontologyClass
	 * 
	 * if the superClass is an instance of restriction so it represents a
	 * property so it calls addPropertiesFromRestrictions method to get the
	 * restricted property
	 * 
	 */
	private static void addSuperClasses(OntClass ontologyClass) {

		/*
		 * get ontology class mapper
		 */
		Class ontologyClassMapper = htblMainOntologyClasses.get(ontologyClass.getLocalName().toLowerCase());

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
				if (superClassName.equals("Resource") || superClassName.equals("Thing"))
					continue;

				Class superClassMapper = htblMainOntologyClasses.get(superClassName.toLowerCase());
				ontologyClassMapper.getSuperClassesList().add(superClassMapper);
			}

			/*
			 * if it is a restriction then call addPropertiesFromRestrictions to
			 * get properties from restriction and add it to properties list of
			 * ontologyClassMapper
			 */
			if (superClass.isRestriction()) {
				Restriction restriction = superClass.asRestriction();
				addPropertiesFromRestrictions(ontologyClass.getLocalName(), restriction);
			}

		}

	}

	/*
	 * addSubClasses method gets the subclass list of the passed ontologyClass
	 * and add them to classTypeList of the mapper of the passed ontologyClass
	 */
	private static void addSubClasses(OntClass ontologyClass) {

		/*
		 * get ontology class mapper
		 */
		Class ontologyClassMapper = htblMainOntologyClasses.get(ontologyClass.getLocalName().toLowerCase());

		/*
		 * populate subCLass list
		 */
		ExtendedIterator<OntClass> subClassesIter = ontologyClass.listSubClasses();

		/*
		 * set hasTypeClasses boolean in the ontologyClassMapper if the
		 * subClassesIter hasNext() is not empty
		 */
		if (subClassesIter.hasNext())
			ontologyClassMapper.setHasTypeClasses(true);

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

				Class subClassMapper = htblMainOntologyClasses.get(subClassName.toLowerCase());
				ontologyClassMapper.getClassTypesList().put(subClassName, subClassMapper);
			}

		}

	}

	/*
	 * addPropertiesFromRestrictions method is used to get property from passed
	 * restriction and add it to classMaper
	 * 
	 * Some properties might have no domain and range, but they may be attached
	 * to a class by restrictions where value restrictions ensure that for this
	 * restricted property on the ontologyClass with passed classMaperName has a
	 * range of type ontology class
	 * 
	 * example : _:genid15 a owl:Restriction ; owl:onProperty DUL:includesEvent
	 * ; owl:someValuesFrom ssn:Stimulus .
	 * 
	 * where the restricted property is DUL:includesEvent and property value is
	 * of type ssn:Stimulus
	 * 
	 */
	private static void addPropertiesFromRestrictions(String classMapperName, Restriction restriction) {

		/*
		 * get property associated with restriction
		 */
		OntProperty prop = restriction.getOnProperty();
		/*
		 * All the Value restriction ( like owl:someValuesFrom ,
		 * owl:allValuesFrom, owl:hasValue) It will have the object class if the
		 * property is an objectProperty
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

				Class subjectClass = htblMainOntologyClasses.get(classMapperName.toLowerCase());
				boolean isPropertyUnique = isPropertyUnique(prop.getURI(), PropertyType.ObjectProperty.toString());
				boolean isPropertyMultiValued = isPropertyMultValued(prop.getURI(),
						PropertyType.ObjectProperty.toString());

				String propName = prop.getLocalName();
				Prefix prefix = getPrefix(prop.getNameSpace());

				/*
				 * create new ObjectProperty. I will make the default for
				 * multipleValues true untill their is a macCardinalty
				 * restriction with value 1
				 */
				ObjectProperty objectProperty = new ObjectProperty(subjectClass, prop.getLocalName(), prefix,
						objectClassName, isPropertyMultiValued, isPropertyUnique);
				subjectClass.getProperties().put(propName, objectProperty);
				subjectClass.getHtblPropUriName().put(prop.getURI(), propName);
			}

		} else {

			/*
			 * if property is a datatype property so I have to get its dataType
			 * from htblMainOntologyProperties if the restriction does not have
			 * the dataType
			 * 
			 * All the Value restriction ( like owl:someValuesFrom ,
			 * owl:allValuesFrom, owl:hasValue) It will have the range datatype
			 * if the property is an datatypeProperty
			 */
			if (prop.isDatatypeProperty()) {
				String datatype = "";
				if (restriction.isSomeValuesFromRestriction()) {
					SomeValuesFromRestriction someValuesFromRestriction = restriction.asSomeValuesFromRestriction();
					datatype = someValuesFromRestriction.getSomeValuesFrom().getURI();
				}

				if (restriction.isAllValuesFromRestriction()) {
					AllValuesFromRestriction allValuesFromRestriction = restriction.asAllValuesFromRestriction();
					datatype = allValuesFromRestriction.getAllValuesFrom().getURI();
				}

				if (restriction.isHasValueRestriction()) {
					HasValueRestriction hasValueRestriction = restriction.asHasValueRestriction();

					if (hasValueRestriction.getHasValue().isResource())
						datatype = hasValueRestriction.getHasValue().asResource().getURI();
				}

				/*
				 * check that their is a objectClass for object Property
				 */
				if (datatype != "") {

					Class subjectClass = htblMainOntologyClasses.get(classMapperName.toLowerCase());
					boolean isPropertyUnique = isPropertyUnique(prop.getURI(),
							PropertyType.DatatypeProperty.toString());
					boolean isPropertyMultiValued = isPropertyMultValued(prop.getURI(),
							PropertyType.DatatypeProperty.toString());

					String propName = prop.getLocalName();
					Prefix prefix = getPrefix(prop.getNameSpace());
					XSDDatatype xsdDataType = getXSDDataTypeEnum(datatype);

					/*
					 * create new dataTypeProperty. I will make the default for
					 * multipleValues true untill their is a maxCardinalty
					 * restriction with value 1
					 */
					DataTypeProperty dataTypeProperty = new DataTypeProperty(subjectClass, propName, prefix,
							xsdDataType, isPropertyMultiValued, isPropertyUnique);
					subjectClass.getProperties().put(propName, dataTypeProperty);
					subjectClass.getHtblPropUriName().put(prop.getURI(), propName);

				}

			}
		}
	}

	/*
	 * addPropertiesToClassesMappersUsingDomainAndRange method iterates on all
	 * the properties in the ontology and according to the domain and range of
	 * the property it create a mapper of the property and add it to the mapper
	 * of the domain class
	 */
	private static void addPropertiesToClassesMappersUsingDomainAndRange() {
		Iterator<String> htblMainOntologyPropertiesIter = htblMainOntologyProperties.keySet().iterator();

		while (htblMainOntologyPropertiesIter.hasNext()) {

			String propertyUri = htblMainOntologyPropertiesIter.next();
			OntProperty property = htblMainOntologyProperties.get(propertyUri);

			/*
			 * check if the property has domain or not. If it has no domain so I
			 * will skip it because I will not be able to know its subjectClass
			 */
			Resource domain = property.getDomain();
			if (domain == null)
				continue;

			/*
			 * check if the property has range or not. If it has no range so I
			 * will skip it because I will not be able to know its objectClass
			 * if its an objectProperty or its dataType if it is a
			 * datatypeProperty
			 */
			Resource range = property.getRange();
			if (range == null)
				continue;

			Class subjectClassMapper = htblMainOntologyClasses.get(domain.getLocalName().toLowerCase());
			Prefix prefix = getPrefix(property.getNameSpace());
			String propertyName = property.getLocalName();
			/*
			 * if the property is DatatypeProperty so create a new
			 * datatypeProperty and add it to its appropriate classMapper
			 */
			if (property.isDatatypeProperty()) {
				/*
				 * get datatype range of the dataTypeProperty
				 */
				XSDDatatype xsdDatatype = getXSDDataTypeEnum(property.getRange().getURI());

				/*
				 * check if the property isUnique
				 */
				boolean isPropertyUnique = isPropertyUnique(property.getURI(),
						PropertyType.DatatypeProperty.toString());

				/*
				 * check if the property is multiValued property
				 */
				boolean isPropertyMultiValued = isPropertyMultValued(property.getURI(),
						PropertyType.DatatypeProperty.toString());

				/*
				 * create a new DataTypeProperty and add it to the
				 * subjectClassMapper
				 */
				DataTypeProperty dataTypeProperty = new DataTypeProperty(subjectClassMapper, propertyName, prefix,
						xsdDatatype, isPropertyMultiValued, isPropertyUnique);
				subjectClassMapper.getProperties().put(propertyName, dataTypeProperty);
				subjectClassMapper.getHtblPropUriName().put(property.getURI(), propertyName);

			}

			/*
			 * if the property is ObjectProperty so create a new ObjectProperty
			 * and add it to its appropriate classMapper
			 */
			if (property.isObjectProperty()) {

				/*
				 * check if the property isUnique
				 */
				boolean isPropertyUnique = isPropertyUnique(property.getURI(), PropertyType.ObjectProperty.toString());

				/*
				 * check if the property is multiValued property
				 */
				boolean isPropertyMultiValued = isPropertyMultValued(property.getURI(),
						PropertyType.ObjectProperty.toString());

				/*
				 * create a new ObjectProperty and add it to the
				 * subjectClassMapper
				 */
				ObjectProperty objectProperty = new ObjectProperty(subjectClassMapper, propertyName, prefix,
						property.getRange().getLocalName(), isPropertyMultiValued, isPropertyUnique);
				subjectClassMapper.getProperties().put(propertyName, objectProperty);
				subjectClassMapper.getHtblPropUriName().put(property.getURI(), propertyName);
			}

		}

	}

	/*
	 * addInheritedPropertiesToSubClasses method is used to add inhered
	 * properties to subclasses
	 * 
	 * According to the ontology engineering. A subClass inherit all the
	 * properties of the superClass
	 */
	private static void addInheritedPropertiesToSubClasses() {

		Iterator<String> htblMainOntologyClassesIter = htblMainOntologyClasses.keySet().iterator();

		while (htblMainOntologyClassesIter.hasNext()) {
			String className = htblMainOntologyClassesIter.next();
			Class classMapper = htblMainOntologyClasses.get(className);

			for (Class superClassMapper : classMapper.getSuperClassesList()) {

				classMapper.getProperties().putAll(superClassMapper.getProperties());
				classMapper.getHtblPropUriName().putAll(superClassMapper.getHtblPropUriName());
			}

		}

	}

	/*
	 * isPropertyUnique method queries the ontology for a given property to
	 * check if it has an annotation property (iot-platform:isUnique) which
	 * tells that this propertyValue must be a unique one
	 */
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

	/*
	 * isPropertyMultValued method queries the ontology for a given property to
	 * check if it has an annotation property (iot-platform:hasMultipleValues)
	 * which tells that this propertyValue is a multiValuedProperty (can has
	 * more than one value)
	 */
	private static boolean isPropertyMultValued(String propertyUri, String propertyType) {

		StringBuilder queryBuilder = new StringBuilder();
		for (Prefix prefix : Prefix.values()) {
			queryBuilder.append("PREFIX	" + prefix.getPrefix() + "	<" + prefix.getUri() + ">\n");
		}

		String queryStr = "SELECT (COUNT(*) as ?isFound ) \n WHERE { <" + propertyUri + ">   a   owl:" + propertyType
				+ " ; \n " + "iot-platform:hasMultipleValues  \"true\"^^xsd:boolean . \n" + "}";

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

	/*
	 * isClassHasUniqueIdentifier method queries the ontology to check if the
	 * passed classUri has uniqueIdentifier Property or not by checking that the
	 * owl:class instance with passed classUri has an annotationProperty
	 * (iot-platform:hasUniqueIdentifier) and it returns the value of this
	 * annotationProperty which represent the uniqueIdentifierProperty of that
	 * class
	 * 
	 * The uniqueIdentiferProperty is used when inserting an individual the user
	 * must add a value for this property and this value will the subject of the
	 * Individual
	 * 
	 * If a class has no annotationProperty(iot-platform:hasUniqueIdentifier)
	 * the system will automatically add n id property to be the
	 * uniqueIdentifier of that class
	 * 
	 */
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
	 * get Prefix Enum that maps the String nameSpace
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

	/*
	 * getXSDDataTypeEnum return XsdDataType enum instance that maps the passed
	 * dataType uri (eg. http://www.w3.org/2001/XMLSchema#string)
	 */
	private static XSDDatatype getXSDDataTypeEnum(String dataType) {

		if (XSDDatatype.boolean_type.getXsdTypeURI().equals(dataType)) {
			return XSDDatatype.boolean_type;
		}

		if (XSDDatatype.decimal_typed.getXsdTypeURI().equals(dataType)) {
			return XSDDatatype.decimal_typed;
		}

		if (XSDDatatype.float_typed.getXsdTypeURI().equals(dataType)) {
			return XSDDatatype.float_typed;
		}

		if (XSDDatatype.integer_typed.getXsdTypeURI().equals(dataType)) {
			return XSDDatatype.integer_typed;
		}

		if (XSDDatatype.string_typed.getXsdTypeURI().equals(dataType)) {
			return XSDDatatype.string_typed;
		}

		if (XSDDatatype.dateTime_typed.getXsdTypeURI().equals(dataType)) {
			return XSDDatatype.dateTime_typed;
		}

		if (XSDDatatype.double_typed.getXsdTypeURI().equals(dataType)) {
			return XSDDatatype.double_typed;
		}

		return null;
	}

	public static Hashtable<String, Class> getHtblMainOntologyClassesMappers() {
		return htblMainOntologyClasses;
	}

	public static Hashtable<String, Class> getHtblMainOntologyClassesUriMappers() {
		return htblMainOntologyClassesUri;
	}

	public static void main(String[] args) {

		// System.out.println(ontologyMapper.htblMainOntologyClasses.size());
		// System.out.println(ontologyMapper.htblMainOntologyProperties.size());

		String className = "SurvivalProperty";

		Hashtable<String, Property> htblProperties = OntologyMapper.getHtblMainOntologyClassesMappers().get(className)
				.getProperties();
		Iterator<String> itr = htblProperties.keySet().iterator();

		while (itr.hasNext()) {
			String propertyName = itr.next();
			Property property = htblProperties.get(propertyName);

			System.out.println(propertyName + " = " + property);

		}

		// System.out.println(ontologyMapper.isPropertyUnique("http://xmlns.com/foaf/0.1/name",
		// PropertyType.DatatypeProperty.toString()));
		//
		// System.out.println(ontologyMapper.isClassHasUniqueIdentifier("http://xmlns.com/foaf/0.1/Group"));

		// for (Class superClass :
		// htblMainOntologyClasses.get("Admin").getSuperClassesList()) {
		// System.out.println(superClass.getName() + " " +
		// superClass.isHasUniqueIdentifierProperty());
		// }
		System.out.println(htblMainOntologyClasses.get(className).getUniqueIdentifierPropertyName());

		// System.out.println(htblMainOntologyClasses.get("Coverage").getClassTypesList().toString());

		// String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		// String szUser = "rdfusr";
		// String szPasswd = "rdfusr";
		// String szJdbcDriver = "oracle.jdbc.driver.OracleDriver";
		//
		// BasicDataSource dataSource = new BasicDataSource();
		// dataSource.setDriverClassName(szJdbcDriver);
		// dataSource.setUrl(szJdbcURL);
		// dataSource.setUsername(szUser);
		// dataSource.setPassword(szPasswd);
		//
		// DynamicConceptsUtility dynamicPropertiesUtility = new
		// DynamicConceptsUtility(
		// new DynamicConceptsDao(dataSource));
		// //
		// System.out.println(dynamicPropertiesUtility.getHtblAllStaticClasses().get(Prefix.SSN.getUri()
		// // + className)
		// // .getProperties());
	}

}
