package com.iotplatform.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iotplatform.models.Employee;
import com.iotplatform.services.EmployeeService;



@RestController
public class EmployeeController {
	@Autowired
	private EmployeeService employeeService;

//	@RequestMapping("/")
//	public String showHome() {
//		return "home";
//	}
	
	 public EmployeeController() {
		System.out.println("EmployeeController Created");
	}

	@RequestMapping(value = "/employee", method = RequestMethod.GET)
	public ResponseEntity<List<Employee>> employees() {

		HttpHeaders headers = new HttpHeaders();
		List<Employee> employees = employeeService.getEmployees();

		if (employees == null) {
			return new ResponseEntity<List<Employee>>(HttpStatus.NOT_FOUND);
		}
		headers.add("Number Of Records Found", String.valueOf(employees.size()));
		return new ResponseEntity<List<Employee>>(employees, headers, HttpStatus.OK);
	}

	@RequestMapping(value = "/employee/{id}", method = RequestMethod.GET)
	public ResponseEntity<Employee> getEmployee(@PathVariable("id") Long employeeId) {
		Employee employee = employeeService.getEmployee(employeeId);
		if (employee == null) {
			return new ResponseEntity<Employee>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Employee>(employee, HttpStatus.OK);
	}
}
