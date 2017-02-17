package com.iotplatform.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Employee {
	int employee_id;
	String first_name;
	String last_name;
	String email;
	String phone_number;
	

	@JsonCreator
	public Employee(@JsonProperty("employeeId") int employee_id, @JsonProperty("firstName") String first_name,
			@JsonProperty("lastName") String last_name, @JsonProperty("email") String email,
			@JsonProperty("phoneNumber") String phone_number) {
		this.employee_id = employee_id;
		this.first_name = first_name;
		this.last_name = last_name;
		this.email = email;
		this.phone_number = phone_number;
		
	}

	public Employee(){
		System.out.println("Employee Model Created");
	}
	
//	public Employee(int employee_id, String first_name, String last_name, String email, String phone_number) {
//		System.out.println("employeee");
//		this.employee_id = employee_id;
//		this.first_name = first_name;
//		this.last_name = last_name;
//		this.email = email;
//		this.phone_number = phone_number;
//		
//	}

	public int getEmployee_id() {
		return employee_id;
	}

	public void setEmployee_id(int employee_id) {
		this.employee_id = employee_id;
	}

	public String getFirst_name() {
		return first_name;
	}

	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}

	public String getLast_name() {
		return last_name;
	}

	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone_number() {
		return phone_number;
	}

	public void setPhone_number(String phone_number) {
		this.phone_number = phone_number;
	}



	
}
