package com.iotplatform.ontology.mapers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.commons.dbcp.BasicDataSource;
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

import com.iotplatform.daos.DynamicConceptDao;
import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.PropertyType;
import com.iotplatform.ontology.XSDDatatype;
import com.iotplatform.utilities.DynamicPropertiesUtility;

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
			}

		}

	}

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

	private static void addSubClasses(OntClass ontologyClass) {

		/*
		 * get ontology class mapper
		 */
		Class ontologyClassMapper = htblMainOntologyClasses.get(ontologyClass.getLocalName().toLowerCase());

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

				Class subClassMapper = htblMainOntologyClasses.get(subClassName.toLowerCase());
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
				String propName = prop.getLocalName();
				Prefix prefix = getPrefix(prop.getNameSpace());
				Class ObjectClass = htblMainOntologyClasses.get(objectClassName.toLowerCase());

				/*
				 * create new ObjectProperty. I will make the default for
				 * multipleValues true untill their is a macCardinalty
				 * restriction with value 1
				 */
				ObjectProperty objectProperty = new ObjectProperty(subjectClass, prop.getLocalName(), prefix,
						ObjectClass, true, isPropertyUnique);
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
					boolean isPropertyUnique = isPropertyUnique(prop.getURI(), PropertyType.ObjectProperty.toString());
					String propName = prop.getLocalName();
					Prefix prefix = getPrefix(prop.getNameSpace());
					XSDDatatype xsdDataType = getXSDDataTypeEnum(datatype);

					/*
					 * create new dataTypeProperty. I will make the default for
					 * multipleValues true untill their is a maxCardinalty
					 * restriction with value 1
					 */
					DataTypeProperty dataTypeProperty = new DataTypeProperty(subjectClass, propName, prefix,
							xsdDataType, true, isPropertyUnique);
					subjectClass.getProperties().put(propName, dataTypeProperty);
					subjectClass.getHtblPropUriName().put(prop.getURI(), propName);

				}

			}
		}
	}

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
				 * create a new DataTypeProperty and add it to the
				 * subjectClassMapper
				 */
				DataTypeProperty dataTypeProperty = new DataTypeProperty(subjectClassMapper, propertyName, prefix,
						xsdDatatype, true, isPropertyUnique);
				subjectClassMapper.getProperties().put(propertyName, dataTypeProperty);
				subjectClassMapper.getHtblPropUriName().put(property.getURI(), propertyName);

			}

			/*
			 * if the property is ObjectProperty so create a new ObjectProperty
			 * and add it to its appropriate classMapper
			 */
			if (property.isObjectProperty()) {

				/*
				 * get objectClass
				 */
				Class objectClassMapper = htblMainOntologyClasses.get(property.getRange().getLocalName().toLowerCase());

				/*
				 * check if the property isUnique
				 */
				boolean isPropertyUnique = isPropertyUnique(property.getURI(), PropertyType.ObjectProperty.toString());

				/*
				 * create a new ObjectProperty and add it to the
				 * subjectClassMapper
				 */
				ObjectProperty objectProperty = new ObjectProperty(subjectClassMapper, propertyName, prefix,
						objectClassMapper, true, isPropertyUnique);
				subjectClassMapper.getProperties().put(propertyName, objectProperty);
				subjectClassMapper.getHtblPropUriName().put(property.getURI(), propertyName);
			}

		}

	}

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

	/*
	 * getXSDDataTypeEnum return XsdDataType enum instance
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
		OntologyMapper ontologyMapper = new OntologyMapper();

		// System.out.println(ontologyMapper.htblMainOntologyClasses.size());
		// System.out.println(ontologyMapper.htblMainOntologyProperties.size());

		String className = "SurvivalProperty";

		Hashtable<String, Property> htblProperties = ontologyMapper.htblMainOntologyClasses.get(className)
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

		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		String szUser = "rdfusr";
		String szPasswd = "rdfusr";
		String szJdbcDriver = "oracle.jdbc.driver.OracleDriver";

		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(szJdbcDriver);
		dataSource.setUrl(szJdbcURL);
		dataSource.setUsername(szUser);
		dataSource.setPassword(szPasswd);

		DynamicPropertiesUtility dynamicPropertiesUtility = new DynamicPropertiesUtility(
				new DynamicConceptDao(dataSource));
		// System.out.println(dynamicPropertiesUtility.getHtblAllStaticClasses().get(Prefix.SSN.getUri()
		// + className)
		// .getProperties());
	}

}
