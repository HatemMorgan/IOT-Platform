package com.iotplatform.configs;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.iotplatform")
public class AppConfig {

	public static final HttpHeaders HTTP_HEADERS = new HttpHeaders();
	static{
	    HTTP_HEADERS.add("Access-Control-Allow-Headers", "Content-Type");
	    HTTP_HEADERS.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
	    HTTP_HEADERS.add("Access-Control-Allow-Origin", "*");
	}
	
}