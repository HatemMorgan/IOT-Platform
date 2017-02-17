package com.iotplatform.daos;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import com.iotplatform.models.Employee;

import oracle.spatial.rdf.client.jena.Oracle;




// using spring jdbc 
@Repository("employeeDAO2")
public class EmployeeDAO2 extends JdbcDaoSupport {

	private JdbcTemplate jdbcTemplate;
	private Oracle oracle ;
	@Autowired
	public EmployeeDAO2(DataSource dataSource,Oracle oracle) {
		System.out.println("Employee DAO created");
		this.oracle = oracle;
		try {
			System.out.println(oracle.getConnection().toString());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.setDataSource(dataSource);
		this.jdbcTemplate = this.getJdbcTemplate();
	}

	public List<Employee> getEmployees() {
		List<Employee> employees = null;

		try {
		
			employees = jdbcTemplate.query("SELECT employee_id, first_name,last_name,email,phone_number FROM employees",
					new BeanPropertyRowMapper<Employee>(Employee.class));
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		return employees;
	}

	// public void getEmployee(int employee_id) {
	//
	// String sql = "SELECT FIRST_NAME FROM EMPLOYEES WHERE EMPLOYEE_ID = ?";
	// List<String> results = this.getJdbcTemplate().queryForList(sql,
	// String.class, employee_id);
	//
	// for (String string : results) {
	// System.out.println(string);
	// }
	// }

	public Employee getEmployee(Long employeeId) {
		Employee employee = null;
		try {
			employee = jdbcTemplate.queryForObject("SELECT * FROM employees WHERE employee_id = ?",
					new Object[] { employeeId }, new BeanPropertyRowMapper<Employee>(Employee.class));
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		
		
		return employee;

	}

	public void deleteEmployee(int emplyee_id) {

		String sql = "DELETE FROM EMPLOYEES WHERE EMPLOYEE_ID = ?";

		// Object[] params = { emplyee_id };
		// int[] types = {Types.NUMERIC};

		int check = this.getJdbcTemplate().update(sql, emplyee_id);
		this.getJdbcTemplate().execute("COMMIT");
		System.out.println(this.getJdbcTemplate().getQueryTimeout());
		System.out.println(check);
	}
}
