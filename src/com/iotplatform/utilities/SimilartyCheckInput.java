package com.iotplatform.utilities;

public class SimilartyCheckInput {

	private Object[] classesArr;
	private Object[] propertiesArr;

	public SimilartyCheckInput(Object[] classesArr, Object[] propertiesArr) {
		this.classesArr = classesArr;
		this.propertiesArr = propertiesArr;
	}

	public Object[] getClassesArr() {
		return classesArr;
	}

	public Object[] getPropertiesArr() {
		return propertiesArr;
	}

}
