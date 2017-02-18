package com.iotplatform.exceptions;

public class NoApplicationModelException extends RuntimeException {
	static String prefix = "No Application model with this name: ";
	static String suffix = " . You have to register and create a new application with this name or enter the right name.";
	public NoApplicationModelException(String applicationName){
		super(prefix+applicationName+suffix);
	}
	
}
