package com.iotplatform.daos;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.mapers.DynamicOntologyMapper;
import com.iotplatform.ontology.mapers.DynamicOntologyStateEnum;
import com.iotplatform.ontology.mapers.OntologyMapper;

import oracle.spatial.rdf.client.jena.Oracle;

@Repository("ontologyDao")
public class OntologyDao {

	private DynamicOntologyDao dynamicOntologyDao;

	@Autowired
	public OntologyDao(DynamicOntologyDao dynamicOntologyDao) {
		this.dynamicOntologyDao = dynamicOntologyDao;
	}

	public LinkedHashMap<String, Object> loadApplicationOntology(String applicationModelName) {

		LinkedHashMap<String, Object> res = new LinkedHashMap<>();

		ArrayList<Object> classList = new ArrayList<>();
		res.put("classes", classList);

		ArrayList<Object> propertiesList = new ArrayList<>();
		res.put("properties", propertiesList);

		getApplicationOntology(applicationModelName, classList, propertiesList);

		return res;

	}

	private void getApplicationOntology(String applicationModelName, ArrayList<Object> classList,
			ArrayList<Object> propertiesList) {

		if (!(DynamicOntologyMapper.getHtblApplicationOntologyState().containsKey(applicationModelName)
				&& DynamicOntologyMapper.getHtblApplicationOntologyState().get(applicationModelName)
						.equals(DynamicOntologyStateEnum.NotModified))) {
			System.out.println("Load  dynamicOntology from database");

			DynamicOntologyMapper.getHtblappDynamicOntologyClasses().remove(applicationModelName);
			DynamicOntologyMapper.getHtblappDynamicOntologyClassesUri().remove(applicationModelName);
			dynamicOntologyDao.loadAndCacheApplicationDynamicOntology(applicationModelName);
		}

		Hashtable<String, String> htbAddedProps = new Hashtable<>();

		Iterator<String> htblMainOntologyClassesMappersIter = OntologyMapper.getHtblMainOntologyClassesMappers()
				.keySet().iterator();

		while (htblMainOntologyClassesMappersIter.hasNext()) {
			String className = htblMainOntologyClassesMappersIter.next();
			Class ontologyClassMapper = OntologyMapper.getHtblMainOntologyClassesMappers().get(className);

			LinkedHashMap<String, String> classMap = new LinkedHashMap<>();
			classMap.put("name", ontologyClassMapper.getName());
			classMap.put("prefix", ontologyClassMapper.getPrefix().getPrefix());

			classList.add(classMap);

			if (!ontologyClassMapper.isHasTypeClasses()) {

				Iterator<String> htblPropertiesIter = ontologyClassMapper.getProperties().keySet().iterator();
				while (htblPropertiesIter.hasNext()) {
					String propertyName = htblPropertiesIter.next();

					if (!htbAddedProps.containsKey(propertyName)) {

						Property property = ontologyClassMapper.getProperties().get(propertyName);

						htbAddedProps.put(propertyName, propertyName);

						if (property instanceof ObjectProperty) {
							LinkedHashMap<String, String> objectPropMap = new LinkedHashMap<>();

							objectPropMap.put("name", propertyName);
							objectPropMap.put("domain", property.getSubjectClass().getName());
							objectPropMap.put("range", ((ObjectProperty) property).getObjectClassName());

							propertiesList.add(objectPropMap);
						} else {

							LinkedHashMap<String, String> objectPropMap = new LinkedHashMap<>();

							objectPropMap.put("name", propertyName);
							objectPropMap.put("domain", property.getSubjectClass().getName());
							objectPropMap.put("range", ((DataTypeProperty) property).getDataType().getDataType());

							propertiesList.add(objectPropMap);

						}
					}
				}
			}

		}
		if (DynamicOntologyMapper.getHtblappDynamicOntologyClasses().containsKey(applicationModelName)) {
			Iterator<String> htblappDynamicOntologyClassesIter = DynamicOntologyMapper
					.getHtblappDynamicOntologyClasses().get(applicationModelName).keySet().iterator();

			while (htblappDynamicOntologyClassesIter.hasNext()) {
				String className = htblappDynamicOntologyClassesIter.next();
				Class dynamicOntologyClassMapper = DynamicOntologyMapper.getHtblappDynamicOntologyClasses()
						.get(applicationModelName).get(className);

				LinkedHashMap<String, String> classMap = new LinkedHashMap<>();
				classMap.put("name", className);
				classMap.put("prefix", dynamicOntologyClassMapper.getPrefix().getPrefix());

				classList.add(classMap);

				if (!dynamicOntologyClassMapper.isHasTypeClasses()) {
					Iterator<String> htblPropertiesIter = dynamicOntologyClassMapper.getProperties().keySet()
							.iterator();
					while (htblPropertiesIter.hasNext()) {
						String propertyName = htblPropertiesIter.next();

						if (!htbAddedProps.containsKey(propertyName)) {

							Property property = dynamicOntologyClassMapper.getProperties().get(propertyName);

							htbAddedProps.put(propertyName, propertyName);

							if (property instanceof ObjectProperty) {
								LinkedHashMap<String, String> objectPropMap = new LinkedHashMap<>();

								objectPropMap.put("name", propertyName);
								objectPropMap.put("domain", property.getSubjectClass().getName());
								objectPropMap.put("range", ((ObjectProperty) property).getObjectClassName());

								propertiesList.add(objectPropMap);
							} else {

								LinkedHashMap<String, String> objectPropMap = new LinkedHashMap<>();

								objectPropMap.put("name", propertyName);
								objectPropMap.put("domain", property.getSubjectClass().getName());
								objectPropMap.put("range", ((DataTypeProperty) property).getDataType().getDataType());

								propertiesList.add(objectPropMap);

							}

						}
					}

				}
			}
		}
	}

	public static void main(String[] args) {
		String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		String szUser = "rdfusr";
		String szPasswd = "rdfusr";

		Oracle oracle = new Oracle(szJdbcURL, szUser, szPasswd);

		DynamicOntologyDao dynamicOntologyDao = new DynamicOntologyDao(oracle);

		OntologyDao ontologyDao = new OntologyDao(dynamicOntologyDao);

		System.out.println(ontologyDao.loadApplicationOntology("TESTAPPLICATION_MODEL"));

	}

}
