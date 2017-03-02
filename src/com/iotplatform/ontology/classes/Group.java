package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefixes;
import com.iotplatform.ontology.XSDDataTypes;

/*
 *  This class maps the group class in the ontology 
 *  A group can contains another group or persons or organizations (any agent)
 */

@Component
public class Group extends Agent {

	public Group() {
		super("Group","http://xmlns.com/foaf/0.1/Group", Prefixes.FOAF);
		
		super.getProperties().put("name", new DataTypeProperty("name", Prefixes.FOAF, XSDDataTypes.string_typed,false,true));
		super.getProperties().put("description",
				new DataTypeProperty("description", Prefixes.IOT_PLATFORM, XSDDataTypes.string_typed,false,false));
		super.getProperties().put("member", new ObjectProperty("member", Prefixes.FOAF,Agent.getAgentInstance(),true,false));
		
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri() + "name", "name");
		this.getHtblPropUriName().put(Prefixes.IOT_PLATFORM.getUri()+"description", "description");
		this.getHtblPropUriName().put(Prefixes.FOAF.getUri()+"member", "member");
		
		super.getSuperClassesList().add(Agent.getAgentInstance());

		
	}

	public static void main(String[] args) {
		Group group = new Group();
		System.out.println(group.getProperties());
	}
}
