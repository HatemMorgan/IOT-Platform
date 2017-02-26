package com.iotplatform.ontology.classes;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.utilities.QueryResultUtility;

import oracle.spatial.rdf.client.jena.Oracle;
import oracle.spatial.rdf.client.jena.OracleUtils;

public class test {

	public class prop {
		@JsonView(View.Summary.class)
		String first_name;
		@JsonView(View.Summary.class)
		Object last_name;

		public prop(String name, Object value) {
			this.first_name = name;
			this.last_name = value;
		}

	}

	public class onto {
		@JsonView(View.Summary.class)
		List<prop> list;

		// @JsonCreator
		public onto(List<prop> list) {
			super();
			this.list = list;
		}

		@Override
		public String toString() {
			return "onto [result=" + list + "]";
		}

	}

	public class onto2 {
		@JsonView(View.Summary.class)
		Hashtable<String, Object> list;

		@JsonCreator
		public onto2(Hashtable<String, Object> list) {
			this.list = list;
		}

	}

	private void run() {
		ObjectMapper mapper = new ObjectMapper();

		// onto staff = createDummyObject();
		onto2 staff2 = createDummyObject2();
		try {

			// System.out.println(staff);
			// Convert object to JSON string
			String jsonInString = mapper.writeValueAsString(staff2);
			System.out.println(jsonInString);

			// Convert object to JSON string and pretty print
			jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(staff2);
			System.out.println(jsonInString);

		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// private void run2() {
	// onto staff = createDummyObject();
	// StringBuffer stringBuffer = new StringBuffer();
	// for (int i = 0; i < staff.list.size(); i++) {
	// String
	// }
	//
	// }

	private onto createDummyObject() {
		List<prop> list = new ArrayList<>();
		list.add(new prop("firstName", "Hatem"));
		list.add(new prop("last_name", "Morgan"));
		list.add(new prop("age", 22));
		list.add(new prop("marry", false));
		list.add(new prop("salary", 1600.23));

		onto staff = new onto(list);

		return staff;

	}

	private onto2 createDummyObject2() {
		Hashtable<String, Object> list = new Hashtable<>();
		list.put("firstName", "Hatem");
		list.put("last_name", "Morgan");
		list.put("age", 22);
		list.put("marry", false);
		list.put("salary", 1600.23);
		list.put("knows", new prop[] { new prop("Hatem", "Morgan"), new prop("Mohamed", "Kaml") });

		onto2 staff = new onto2(list);

		return staff;

	}

	public static void main(String[] args) throws SQLException {
		// String szJdbcURL = "jdbc:oracle:thin:@127.0.0.1:1539:cdb1";
		// String szUser = "rdfusr";
		// String szPasswd = "rdfusr";
		// Oracle oracle = new Oracle(szJdbcURL, szUser, szPasswd);
		// OracleUtils.dropSemanticModel(oracle, "TESTAPPLICATION_MODEL");

		String x = "Hatem Morgan";
		System.out.println(x.substring(0,x.length()-2));
	}

}
