package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.XSDDatatype;

/*
 * This Class maps iot-lite:Coverage Class in the ontology
 * 
 * The coverage of an IoT device (i.e. a temperature sensor inside a room has a coverage of that room).
 */

@Component
public class Coverage extends Class {

	private static Coverage coverageInstance;
	private Class coverageSubjectClassinstance;

	public Coverage() {
		super("Coverage", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Coverage", Prefix.IOT_LITE, null, true);
		init();
	}

	/*
	 * This constructor is used to perform overloading constructor technique and
	 * the parameter String nothing will be passed always with null
	 * 
	 * I have done this overloaded constructor to instantiate the static
	 * systemInstance to avoid java.lang.StackOverflowError exception that Occur
	 * when calling init() to add properties to systemInstance
	 * 
	 */
	public Coverage(String nothing) {
		super("Coverage", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Coverage", Prefix.IOT_LITE, null, true);
	}

	private Class getCoverageSubjectClassinstance() {
		if (coverageSubjectClassinstance == null)
			coverageSubjectClassinstance = new Class("Coverage",
					"http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Coverage", Prefix.IOT_LITE, null, true);

		return coverageSubjectClassinstance;
	}

	public synchronized static Coverage getCoverageInstance() {
		if (coverageInstance == null) {
			coverageInstance = new Coverage(null);
			initCoverageStaticInstance(coverageInstance);
		}

		return coverageInstance;
	}

	private void init() {

		/*
		 * Relation between coverage and its physical location described by
		 * point class
		 */
		super.getProperties().put("location", new ObjectProperty(getCoverageSubjectClassinstance(), "location",
				Prefix.GEO, Point.getPointInstacne(), false, false));

		super.getProperties().put("id", new DataTypeProperty(getCoverageSubjectClassinstance(), "id", Prefix.IOT_LITE,
				XSDDatatype.string_typed, false, false));

		super.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "id", "id");
		super.getHtblPropUriName().put(Prefix.GEO.getUri() + "location", "location");

		// -------------------------------------------------------------------------------------------

		/*
		 * Add iot-lite:Circle Class to coverageTypesList
		 * 
		 * set type classes properties list and htblPropUriName so coverage ones
		 * inOrder to make them have access on the properties list and
		 * htblPropUriName of Coverage class
		 * 
		 * 
		 * I put uniqueIdentefier to null because I defiened in coverage class
		 * which is the superClass
		 */
		Class circle = new Class("Circle", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Circle", Prefix.IOT_LITE,
				null, false);

		circle.setProperties(super.getProperties());
		circle.setHtblPropUriName(super.getHtblPropUriName());

		circle.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "radius", "radius");
		circle.getProperties().put("radius",
				new DataTypeProperty(circle, "radius", Prefix.IOT_LITE, XSDDatatype.double_typed, false, false));

		/*
		 * adding coverage class to superClassesList to tell the dao to add
		 * triple that expresses that an instance of class Circle is also an
		 * instance of class Coverage
		 */
		circle.getSuperClassesList().add(Coverage.getCoverageInstance());
		this.getClassTypesList().put("Circle", circle);

		/*
		 * Add iot-lite:Rectange Class to coverageTypesList
		 */
		Class rectangle = new Class("Rectangle", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Rectangle",
				Prefix.IOT_LITE, null, false);

		/*
		 * adding coverage class to superClassesList to tell the dao to add
		 * triple that expresses that an instance of class Rectangle is also an
		 * instance of class Coverage
		 */
		rectangle.getSuperClassesList().add(Coverage.getCoverageInstance());
		rectangle.setProperties(super.getProperties());
		rectangle.setHtblPropUriName(super.getHtblPropUriName());
		this.getClassTypesList().put("Rectangle", rectangle);

		/*
		 * Add iot-lite:Polygon Class to coverageTypesList
		 */
		Class polygon = new Class("Polygon", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Polygon", Prefix.IOT_LITE,
				null, false);

		/*
		 * adding coverage class to superClassesList to tell the dao to add
		 * triple that expresses that an instance of class Polygon is also an
		 * instance of class Coverage
		 */
		polygon.getSuperClassesList().add(Coverage.getCoverageInstance());
		polygon.setProperties(super.getProperties());
		polygon.setHtblPropUriName(super.getHtblPropUriName());
		this.getClassTypesList().put("Polygon", polygon);

	}

	private static void initCoverageStaticInstance(Coverage coverageInstance) {

		/*
		 * Relation between coverage and its physical location described by
		 * point class
		 */
		coverageInstance.getProperties().put("location",
				new ObjectProperty(coverageInstance.getCoverageSubjectClassinstance(), "location", Prefix.GEO,
						Point.getPointInstacne(), false, false));

		coverageInstance.getProperties().put("id",
				new DataTypeProperty(coverageInstance.getCoverageSubjectClassinstance(), "id", Prefix.IOT_LITE,
						XSDDatatype.string_typed, false, false));

		coverageInstance.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "id", "id");
		coverageInstance.getHtblPropUriName().put(Prefix.GEO.getUri() + "location", "location");

		// -------------------------------------------------------------------------------------------------------

		/*
		 * Add iot-lite:Circle Class to coverageTypesList
		 * 
		 * set type classes properties list and htblPropUriName so coverage ones
		 * inOrder to make them have access on the properties list and
		 * htblPropUriName of Coverage class
		 * 
		 * I put uniqueIdentefier to null because I defiened in coverage class
		 * which is the superClass
		 */
		Class circle = new Class("Circle", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Circle", Prefix.IOT_LITE,
				null, false);

		/*
		 * adding coverage class to superClassesList to tell the dao to add
		 * triple that expresses that an instance of class Circle is also an
		 * instance of class Coverage
		 */
		circle.getSuperClassesList().add(Coverage.getCoverageInstance());
		circle.setProperties(coverageInstance.getProperties());
		circle.setHtblPropUriName(coverageInstance.getHtblPropUriName());

		circle.getProperties().put("radius",
				new DataTypeProperty(circle, "radius", Prefix.IOT_LITE, XSDDatatype.double_typed, false, false));
		circle.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "radius", "radius");

		coverageInstance.getClassTypesList().put("Circle", circle);

		/*
		 * Add iot-lite:Rectange Class to coverageTypesList
		 */
		Class rectangle = new Class("Rectangle", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Rectangle",
				Prefix.IOT_LITE, null, false);

		/*
		 * adding coverage class to superClassesList to tell the dao to add
		 * triple that expresses that an instance of class Rectangle is also an
		 * instance of class Coverage
		 */
		rectangle.getSuperClassesList().add(Coverage.getCoverageInstance());
		rectangle.setProperties(coverageInstance.getProperties());
		rectangle.setHtblPropUriName(coverageInstance.getHtblPropUriName());
		coverageInstance.getClassTypesList().put("Rectangle", rectangle);

		/*
		 * Add iot-lite:Polygon Class to coverageTypesList
		 */
		Class polygon = new Class("Polygon", "http://purl.oclc.org/NET/UNIS/fiware/iot-lite#Polygon", Prefix.IOT_LITE,
				null, false);

		/*
		 * adding coverage class to superClassesList to tell the dao to add
		 * triple that expresses that an instance of class Polygon is also an
		 * instance of class Coverage
		 */
		polygon.getSuperClassesList().add(Coverage.getCoverageInstance());
		polygon.setProperties(coverageInstance.getProperties());
		polygon.setHtblPropUriName(coverageInstance.getHtblPropUriName());
		coverageInstance.getClassTypesList().put("Polygon", polygon);

	}

	public static void main(String[] args) {
		Coverage coverage = new Coverage();
		System.out.println(coverage.getClassTypesList().get("Circle").getProperties());
		System.out.println(coverage.getClassTypesList().get("Rectangle").getProperties());
		System.out.println(coverage.getClassTypesList().get("Polygon").getProperties());

		System.out.println("==================================================================");

		System.out.println(Coverage.getCoverageInstance().getClassTypesList().get("Circle").getProperties());
		System.out.println(Coverage.getCoverageInstance().getClassTypesList().get("Rectangle").getProperties());
		System.out.println(Coverage.getCoverageInstance().getClassTypesList().get("Polygon").getProperties());
	}
}
