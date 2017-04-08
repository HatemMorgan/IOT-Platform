package com.iotplatform.ontology.classes;

import org.springframework.stereotype.Component;

import com.iotplatform.ontology.Class;
import com.iotplatform.ontology.DataTypeProperty;
import com.iotplatform.ontology.ObjectProperty;
import com.iotplatform.ontology.Prefix;
import com.iotplatform.ontology.XSDDataTypes;

/*
 *  This Class maps ssn:Property class in the ontology
 *  
 *  An observable Quality of an Event or Object.  That is, not a quality of an abstract entity as is 
 *  also allowed by DUL's Quality, but rather an aspect of an entity that is intrinsic to and cannot exist 
 *  without the entity and is observable by a sensor.
 */

@Component
public class Property extends Class {

	private static Property propertyInstance;
	private Class propertySubjectClassInstance;

	public Property(String name, String uri, Prefix prefix, String uniqueIdentifierPropertyName,
			boolean hasTypeClasses) {
		super(name, uri, prefix, uniqueIdentifierPropertyName, hasTypeClasses);
		init();
	}

	/*
	 * This constructor is used to perform overloading constructor technique and
	 * the parameter String nothing will be passed always with null
	 * 
	 * I use this constructor to create any subClassStaticInstance of Property.
	 * This constructor does not call init method so by this way I will be able
	 * to create a static instance from any of subClasses of Property and avoid
	 * throwing java.lang.StackOverflowError exception
	 * 
	 * I will use subClassesStaticInstances to add them to typeClassesList of
	 * Property
	 */
	public Property(String name, String uri, Prefix prefix, String uniqueIdentifierPropertyName,
			boolean hasTypeClasses, String nothing) {
		super(name, uri, prefix, uniqueIdentifierPropertyName, hasTypeClasses);
	}

	private Class getPropertySubjectClassInstance() {
		if (propertySubjectClassInstance == null)
			propertySubjectClassInstance = new Class("Property", "http://purl.oclc.org/NET/ssnx/ssn#Property",
					Prefix.SSN, null, true);

		return propertySubjectClassInstance;
	}

	public Property() {
		super("Property", "http://purl.oclc.org/NET/ssnx/ssn#Property", Prefix.SSN, null, true);
		init();
	}

	/*
	 * This constructor is used to perform overloading constructor technique and
	 * the parameter String nothing will be passed always with null
	 * 
	 * I use this constructor to create Static Instance of propertyClass to be
	 * used by other subClasses
	 * 
	 * I created this constructor to remove calling init() method because it
	 * cause throwing java.lang.StackOverflowError exception when any of the
	 * subClasses want to point to PropertyClass as their superClass
	 */
	public Property(String nothing) {
		super("Property", "http://purl.oclc.org/NET/ssnx/ssn#Property", Prefix.SSN, null, true);
	}

	public synchronized static Property getPropertyInstance() {
		if (propertyInstance == null) {
			propertyInstance = new Property(null);
			initPropertyStaticInstance(propertyInstance);
		}
		return propertyInstance;
	}

	private void init() {

		/*
		 * relation between a Property and its value of type Amount
		 */
		this.getProperties().put("hasValue", new ObjectProperty(getPropertySubjectClassInstance(), "hasValue",
				Prefix.SSN, Amount.getAmountInstance(), false, false));

		this.getProperties().put("id", new DataTypeProperty(getPropertySubjectClassInstance(), "id", Prefix.IOT_LITE,
				XSDDataTypes.string_typed, false, false));

		this.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "id", "id");
		this.getHtblPropUriName().put(Prefix.SSN.getUri() + "hasValue", "hasValue");

		if (this.isHasTypeClasses()) {

			this.getClassTypesList().put("Condition", Condition.getConditionInstance());
			this.getClassTypesList().putAll(Condition.getConditionInstance().getClassTypesList());

			this.getClassTypesList().put("MeasurementProperty", MeasurementProperty.getMeasurementPropertyInstance());
			this.getClassTypesList().putAll(MeasurementProperty.getMeasurementPropertyInstance().getClassTypesList());

			this.getClassTypesList().put("MeasurementCapability",
					MeasurementCapability.getMeasurementCapabilityInstance());
			this.getClassTypesList()
					.putAll(MeasurementCapability.getMeasurementCapabilityInstance().getClassTypesList());

			this.getClassTypesList().put("OperatingRange", OperatingRange.getOperatingRangeInstance());
			this.getClassTypesList().putAll(OperatingRange.getOperatingRangeInstance().getClassTypesList());

			this.getClassTypesList().put("OperatingProperty", OperatingProperty.getOperatingPropertyInstance());
			this.getClassTypesList().putAll(OperatingProperty.getOperatingPropertyInstance().getClassTypesList());

			this.getClassTypesList().put("SurvivalProperty", SurvivalProperty.getSurvivalPropertyInstance());
			this.getClassTypesList().putAll(SurvivalProperty.getSurvivalPropertyInstance().getClassTypesList());

			this.getClassTypesList().put("SurvivalRange", SurvivalRange.getSurvivalRangeInstance());
			this.getClassTypesList().putAll(SurvivalRange.getSurvivalRangeInstance().getClassTypesList());
		}

	}

	public static void initPropertyStaticInstance(Property propertyInstance) {

		/*
		 * relation between a Property and its value of type Amount
		 */
		propertyInstance.getProperties().put("hasValue",
				new ObjectProperty(propertyInstance.getPropertySubjectClassInstance(), "hasValue", Prefix.SSN,
						Amount.getAmountInstance(), false, false));

		propertyInstance.getProperties().put("id",
				new DataTypeProperty(propertyInstance.getPropertySubjectClassInstance(), "id", Prefix.IOT_LITE,
						XSDDataTypes.string_typed, false, false));

		propertyInstance.getHtblPropUriName().put(Prefix.IOT_LITE.getUri() + "id", "id");
		propertyInstance.getHtblPropUriName().put(Prefix.SSN.getUri() + "hasValue", "hasValue");

		if (propertyInstance.isHasTypeClasses()) {

			propertyInstance.getClassTypesList().put("Condition", Condition.getConditionInstance());
			propertyInstance.getClassTypesList().putAll(Condition.getConditionInstance().getClassTypesList());

			propertyInstance.getClassTypesList().put("MeasurementProperty",
					MeasurementProperty.getMeasurementPropertyInstance());
			propertyInstance.getClassTypesList()
					.putAll(MeasurementProperty.getMeasurementPropertyInstance().getClassTypesList());

			propertyInstance.getClassTypesList().put("MeasurementCapability",
					MeasurementCapability.getMeasurementCapabilityInstance());
			propertyInstance.getClassTypesList()
					.putAll(MeasurementCapability.getMeasurementCapabilityInstance().getClassTypesList());

			propertyInstance.getClassTypesList().put("OperatingRange", OperatingRange.getOperatingRangeInstance());
			propertyInstance.getClassTypesList().putAll(OperatingRange.getOperatingRangeInstance().getClassTypesList());

			propertyInstance.getClassTypesList().put("OperatingProperty",
					OperatingProperty.getOperatingPropertyInstance());
			propertyInstance.getClassTypesList()
					.putAll(OperatingProperty.getOperatingPropertyInstance().getClassTypesList());

			propertyInstance.getClassTypesList().put("SurvivalProperty",
					SurvivalProperty.getSurvivalPropertyInstance());
			propertyInstance.getClassTypesList()
					.putAll(SurvivalProperty.getSurvivalPropertyInstance().getClassTypesList());

			propertyInstance.getClassTypesList().put("SurvivalRange", SurvivalRange.getSurvivalRangeInstance());
			propertyInstance.getClassTypesList().putAll(SurvivalRange.getSurvivalRangeInstance().getClassTypesList());
		}

	}

	public static void main(String[] args) {
		Property property = new Property();
		System.out.println(property.getClassTypesList().size());
		System.out.println(Property.getPropertyInstance().getClassTypesList().size());
	}
}
