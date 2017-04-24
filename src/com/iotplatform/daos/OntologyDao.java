package com.iotplatform.daos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Property;
import com.iotplatform.ontology.mapers.DynamicOntologyMapper;
import com.iotplatform.ontology.mapers.OntologyMapper;

@Repository("ontologyDao")
public class OntologyDao {

	private DynamicOntologyDao DynamicOntologyDao;

	@Autowired
	public OntologyDao(DynamicOntologyDao DynamicOntologyDao) {
		this.DynamicOntologyDao = DynamicOntologyDao;
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

		DynamicOntologyDao.loadAndCacheApplicationDynamicOntologyClasses(applicationModelName);

		Iterator<String> htblMainOntologyClassesMappersIter = OntologyMapper.getOntologyMapper()
				.getHtblMainOntologyClassesMappers().keySet().iterator();

		while (htblMainOntologyClassesMappersIter.hasNext()) {
			String className = htblMainOntologyClassesMappersIter.next();
			Class ontologyClassMapper = OntologyMapper.getHtblMainOntologyClassesMappers().get(className);

			LinkedHashMap<String, String> classMap = new LinkedHashMap<>();
			classMap.put("name", className);
			classMap.put("prefix", ontologyClassMapper.getPrefix().getPrefix());

			classList.add(classMap);

			Iterator<String> htblPropertiesIter = ontologyClassMapper.getProperties().keySet().iterator();
			while (htblPropertiesIter.hasNext()) {
				String propertyName = htblPropertiesIter.next();
				Property property = ontologyClassMapper.getProperties().get(propertyName);

				if (property instanceof ObjectProperty) {
					LinkedHashMap<String, String> objectPropMap = new LinkedHashMap<>();

					objectPropMap.put("name", propertyName);
					objectPropMap.put("domain", property.getSubjectClass().getName());
					objectPropMap.put("range", ((ObjectProperty) property).getObject().getName());

					propertiesList.add(objectPropMap);
				}

			}

		}

		Iterator<String> htblappDynamicOntologyClassesIter = DynamicOntologyMapper.getHtblappDynamicOntologyClasses()
				.get(applicationModelName).keySet().iterator();

		while (htblappDynamicOntologyClassesIter.hasNext()) {
			String className = htblappDynamicOntologyClassesIter.next();
			Class dynamicOntologyClassMapper = DynamicOntologyMapper.getHtblappDynamicOntologyClasses()
					.get(applicationModelName).get(className);

			LinkedHashMap<String, String> classMap = new LinkedHashMap<>();
			classMap.put("name", className);
			classMap.put("prefix", dynamicOntologyClassMapper.getPrefix().getPrefix());

			classList.add(classMap);

		}

	}

}
