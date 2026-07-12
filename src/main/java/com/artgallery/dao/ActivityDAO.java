package com.artgallery.dao;

import com.artgallery.config.DatabaseConfig;
import com.artgallery.model.ActivityLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityDAO {

    public void log(String action, String entityType, Integer entityId, String description, Integer userId) throws SQLException {
        String sql = "INSERT INTO activity_log (action, entity_type, entity_id, description, user_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, action);
            ps.setString(2, entityType);
            if (entityId != null) ps.setInt(3, entityId); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, description);
            if (userId != null) ps.setInt(5, userId); else ps.setNull(5, Types.INTEGER);
            ps.executeUpdate();
        }
    }

    public List<ActivityLog> findRecent(int limit) throws SQLException {
        String sql = """
            SELECT al.*, u.full_name AS user_name
            FROM activity_log al
            LEFT JOIN users u ON al.user_id = u.id
            ORDER BY al.created_at DESC
            LIMIT ?
            """;
        List<ActivityLog> logs = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRow(rs));
                }
            }
        }
        return logs;
    }

    private ActivityLog mapRow(ResultSet rs) throws SQLException {
        ActivityLog log = new ActivityLog();
        log.setId(rs.getInt("id"));
        log.setAction(rs.getString("action"));
        log.setEntityType(rs.getString("entity_type"));
        int entityId = rs.getInt("entity_id");
        log.setEntityId(rs.wasNull() ? null : entityId);
        log.setDescription(rs.getString("description"));
        int userId = rs.getInt("user_id");
        log.setUserId(rs.wasNull() ? null : userId);
        log.setUserName(rs.getString("user_name"));
        Timestamp ts = rs.getTimestamp("created_at");
        log.setCreatedAt(ts != null ? ts.toString() : null);
        return log;
    }
}
