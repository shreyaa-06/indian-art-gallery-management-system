package com.artgallery.dao;

import com.artgallery.config.DatabaseConfig;

import java.sql.*;
import java.util.*;

public class ReportDAO {

    public Map<String, Object> getSummary() throws SQLException {
        Map<String, Object> summary = new LinkedHashMap<>();

        try (Connection conn = DatabaseConfig.getConnection()) {
            summary.put("artworksByCategory", queryCategoryCounts(conn));
            summary.put("artworksByStatus", queryStatusCounts(conn));
            summary.put("exhibitionsByStatus", queryExhibitionStatusCounts(conn));
            summary.put("totalArtworkValue", queryTotalValue(conn));
            summary.put("recentCustomers", queryRecentCustomerCount(conn));
        }
        return summary;
    }

    private List<Map<String, Object>> queryCategoryCounts(Connection conn) throws SQLException {
        String sql = "SELECT category, COUNT(*) as count FROM artworks GROUP BY category ORDER BY count DESC";
        return queryLabelCount(conn, sql, "category");
    }

    private List<Map<String, Object>> queryStatusCounts(Connection conn) throws SQLException {
        String sql = "SELECT status, COUNT(*) as count FROM artworks GROUP BY status ORDER BY count DESC";
        return queryLabelCount(conn, sql, "status");
    }

    private List<Map<String, Object>> queryExhibitionStatusCounts(Connection conn) throws SQLException {
        String sql = "SELECT status, COUNT(*) as count FROM exhibitions GROUP BY status ORDER BY count DESC";
        return queryLabelCount(conn, sql, "status");
    }

    private List<Map<String, Object>> queryLabelCount(Connection conn, String sql, String labelCol) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("label", rs.getString(labelCol));
                row.put("count", rs.getInt("count"));
                list.add(row);
            }
        }
        return list;
    }

    private double queryTotalValue(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(SUM(price), 0) FROM artworks WHERE status != 'sold'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        }
        return 0;
    }

    private int queryRecentCustomerCount(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers WHERE visit_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
