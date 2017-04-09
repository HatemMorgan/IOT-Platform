package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.XSDDatatype;

/*
 * This Class maps the geo:Point class in the ontology
 * 
 * Uniquely identified by lat/long/alt. i.e.
 *
 *spaciallyIntersects(P1, P2) :- lat(P1, LAT), long(P1, LONG), alt(P1, ALT),
 *lat(P2, LAT), long(P2, LONG), alt(P2, ALT).
 *
 *sameThing(P1, P2) :- type(P1, Point), type(P2, Point), spaciallyIntersects(P1, P2).
  
 */

@Component
public class Point extends Class {

	private static Point pointInstance;
	private Class pointSubjectClassInstance;

	public Point() {
		super("Point", "http://www.w3.org/2003/01/geo/wgs84_pos#Point", Prefix.GEO, null, false);
		init();
	}

	private Class getPointSubjectClassInstance() {
		if (pointSubjectClassInstance == null)
			pointSubjectClassInstance = new Class("Point", "http://www.w3.org/2003/01/geo/wgs84_pos#Point",
					Prefix.GEO, null, false);

		return pointSubjectClassInstance;
	}

	public synchronized static Point getPointInstacne() {
		if (pointInstance == null)
			pointInstance = new Point();

		return pointInstance;
	}

	private void init() {

		/*
		 * point id which must be unique
		 */
		super.getProperties().put("id", new DataTypeProperty(getPointSubjectClassInstance(), "id", Prefix.IOT_LITE,
				XSDDatatype.string_typed, false, false));

		/*
		 * point latitude
		 */
		super.getProperties().put("lat", new DataTypeProperty(getPointSubjectClassInstance(), "lat", Prefix.GEO,
				XSDDatatype.double_typed, false, false));

		/*
		 * point longitude
		 */
		super.getProperties().put("long", new DataTypeProperty(getPointSubjectClassInstance(), "long", Prefix.GEO,
				XSDDatatype.double_typed, false, false));

		/*
		 * point google maps url
		 */
		super.getProperties().put("googleMapsUrl", new DataTypeProperty(getPointSubjectClassInstance(), "googleMapsUrl",
				Prefix.IOT_PLATFORM, XSDDatatype.string_typed, false, false));

		super.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "id", "id");
		super.getHtblPropUriName().put(Prefix.GEO.getUri() + "lat", "lat");
		super.getHtblPropUriName().put(Prefix.GEO.getUri() + "long", "long");
		super.getHtblPropUriName().put(Prefix.IOT_PLATFORM.getUri() + "googleMapsUrl", "googleMapsUrl");

	}

}
