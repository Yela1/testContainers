package com.example.testcontainers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerDao {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public List<Customer> findAll() {
        return jdbcTemplate.query(
                "SELECT id, first_name, last_name FROM customers",
                (rs, rowNum) -> new Customer(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name"))
        );
    }

    public Customer findCustomer(String name){
        String sqlQuery = "SELECT * FROM CUSTOMERS WHERE first_name = ?";
        return (Customer) jdbcTemplate.queryForObject(sqlQuery,
                new Object[]{name},
                new BeanPropertyRowMapper<>(Customer.class));
    }
}
