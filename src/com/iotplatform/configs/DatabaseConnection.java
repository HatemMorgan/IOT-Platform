package com.iotplatform.configs;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import oracle.spatial.rdf.client.jena.Oracle;



//Load to Environment.
@PropertySources({ @PropertySource("classpath:com/iotplatform/props/jdbc.properties") })
@Component
public class DatabaseConnection {
	private  String userName ;
	private  String password;
	private  String url ;
	private  Oracle oracle ;
	
	@Autowired
	private Environment env;
	
	
	
	
	@Autowired
	public void setUserName(@Value("${jdbc.userName}")String userName) {
		this.userName = userName;
	}
	@Autowired
	public void setPassword(@Value("${jdbc.password}")String password) {
		this.password = password;
	}
	@Autowired
	public void setUrl(@Value("${jdbc.url}")String url) {
		this.url = url;
	}
	@Bean(name="oracle")
	public  Oracle getOracle() {
		
		if(oracle == null){
			System.out.println("here");
			oracle = new Oracle(url,userName,password);
		}
		System.out.println("Oracle created");
		return oracle;
	}
	@Bean(name="dataSource")
	public DataSource getDataSource(){
	  BasicDataSource dataSource = new BasicDataSource();
	  dataSource.setDriverClassName(env.getProperty("jdbc.database-driver"));
      dataSource.setUrl(env.getProperty("jdbc.url"));
      dataSource.setUsername(env.getProperty("jdbc.userName"));
      dataSource.setPassword(env.getProperty("jdbc.password"));

      System.out.println("## getDataSource: " + dataSource);

      return dataSource;
	}
	
	
	
	
	
}
