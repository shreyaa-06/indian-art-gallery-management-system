package com.artgallery.service;

import com.artgallery.dao.ActivityDAO;
import com.artgallery.dao.CustomerDAO;
import com.artgallery.model.Customer;
import com.artgallery.model.PagedResult;
import com.artgallery.model.User;
import com.artgallery.util.ValidationUtil;

import java.sql.SQLException;
import java.util.Map;

public class CustomerService {
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final ActivityDAO activityDAO = new ActivityDAO();

    public PagedResult<Customer> findAll(String search, int page, int pageSize) throws SQLException {
        return customerDAO.findAll(search, page, pageSize);
    }

    public Customer findById(int id) throws SQLException {
        Customer customer = customerDAO.findById(id);
        if (customer == null) throw new IllegalArgumentException("Customer not found");
        return customer;
    }

    public Customer create(Map<String, Object> data, User user) throws SQLException {
        Customer customer = mapFromData(data);
        customer.setName(ValidationUtil.requireNonBlank(customer.getName(), "Name"));
        int id = customerDAO.create(customer);
        customer.setId(id);
        activityDAO.log("create", "customer", id, "Registered customer \"" + customer.getName() + "\"", user.getId());
        return customer;
    }

    public Customer update(int id, Map<String, Object> data, User user) throws SQLException {
        if (customerDAO.findById(id) == null) throw new IllegalArgumentException("Customer not found");
        Customer customer = mapFromData(data);
        customer.setId(id);
        customer.setName(ValidationUtil.requireNonBlank(customer.getName(), "Name"));
        customerDAO.update(customer);
        activityDAO.log("update", "customer", id, "Updated customer \"" + customer.getName() + "\"", user.getId());
        return customerDAO.findById(id);
    }

    public void delete(int id, User user) throws SQLException {
        Customer existing = customerDAO.findById(id);
        if (existing == null) throw new IllegalArgumentException("Customer not found");
        if (!customerDAO.delete(id)) throw new SQLException("Failed to delete customer");
        activityDAO.log("delete", "customer", id, "Deleted customer \"" + existing.getName() + "\"", user.getId());
    }

    private Customer mapFromData(Map<String, Object> data) {
        Customer c = new Customer();
        c.setName(ValidationUtil.getString(data, "name"));
        c.setEmail(ValidationUtil.optionalTrim(ValidationUtil.getString(data, "email")));
        c.setPhone(ValidationUtil.optionalTrim(ValidationUtil.getString(data, "phone")));
        c.setAddress(ValidationUtil.optionalTrim(ValidationUtil.getString(data, "address")));
        c.setVisitDate(ValidationUtil.getString(data, "visitDate"));
        c.setNotes(ValidationUtil.optionalTrim(ValidationUtil.getString(data, "notes")));
        return c;
    }
}
