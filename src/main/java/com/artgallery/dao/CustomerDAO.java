package com.artgallery.dao;

import com.artgallery.config.DatabaseConfig;
import com.artgallery.model.Customer;
import com.artgallery.model.PagedResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public PagedResult<Customer> findAll(String search, int page, int pageSize) throws SQLException {
        String where = "";
        if (search != null && !search.isBlank()) {
            where = " WHERE name LIKE ? OR email LIKE ? OR phone LIKE ?";
        }
        int offset = (page - 1) * pageSize;
        String countSql = "SELECT COUNT(*) FROM customers" + where;
        String sql = "SELECT * FROM customers" + where + " ORDER BY created_at DESC LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            int total = countTotal(conn, countSql, search);
            List<Customer> customers = new ArrayList<>();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int idx = setSearchParams(ps, search, 1);
                ps.setInt(idx, pageSize);
                ps.setInt(idx + 1, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        customers.add(mapRow(rs));
                    }
                }
            }
            return new PagedResult<>(customers, total, page, pageSize);
        }
    }

    public Customer findById(int id) throws SQLException {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public int create(Customer customer) throws SQLException {
        String sql = """
            INSERT INTO customers (name, email, phone, address, visit_date, notes)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindCustomer(ps, customer);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Failed to create customer");
    }

    public boolean update(Customer customer) throws SQLException {
        String sql = """
            UPDATE customers SET name=?, email=?, phone=?, address=?, visit_date=?, notes=?
            WHERE id=?
            """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindCustomer(ps, customer);
            ps.setInt(7, customer.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private void bindCustomer(PreparedStatement ps, Customer c) throws SQLException {
        ps.setString(1, c.getName());
        ps.setString(2, c.getEmail());
        ps.setString(3, c.getPhone());
        ps.setString(4, c.getAddress());
        ps.setString(5, c.getVisitDate());
        ps.setString(6, c.getNotes());
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        c.setVisitDate(rs.getString("visit_date"));
        c.setNotes(rs.getString("notes"));
        Timestamp created = rs.getTimestamp("created_at");
        c.setCreatedAt(created != null ? created.toString() : null);
        Timestamp updated = rs.getTimestamp("updated_at");
        c.setUpdatedAt(updated != null ? updated.toString() : null);
        return c;
    }

    private int countTotal(Connection conn, String sql, String search) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setSearchParams(ps, search, 1);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private int setSearchParams(PreparedStatement ps, String search, int startIdx) throws SQLException {
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim() + "%";
            ps.setString(startIdx, pattern);
            ps.setString(startIdx + 1, pattern);
            ps.setString(startIdx + 2, pattern);
            return startIdx + 3;
        }
        return startIdx;
    }
}
