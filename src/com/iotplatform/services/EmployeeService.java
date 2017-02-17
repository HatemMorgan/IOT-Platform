package com.iotplatform.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iotplatform.daos.EmployeeDAO2;
import com.iotplatform.models.Employee;





@Service("employeeService")
public class EmployeeService {
	@Autowired
	private EmployeeDAO2 employeeDao;
	
	public EmployeeService(){
		System.out.println("Employee Service Created");
	}

	public List<Employee> getEmployees() {
		List<Employee> employees = employeeDao.getEmployees();
		return employees;
	}

	public Employee getEmployee(Long employeeId) {
		Employee employee = employeeDao.getEmployee(employeeId);
		return employee;
	}

}
