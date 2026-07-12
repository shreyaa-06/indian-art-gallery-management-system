package com.artgallery.dao;

import com.artgallery.config.DatabaseConfig;
import com.artgallery.model.Artwork;
import com.artgallery.model.Exhibition;
import com.artgallery.model.PagedResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExhibitionDAO {

    public PagedResult<Exhibition> findAll(String status, int page, int pageSize) throws SQLException {
        String where = "";
        if (status != null && !status.isBlank()) {
            where = " WHERE status = ?";
        }
        int offset = (page - 1) * pageSize;
        String countSql = "SELECT COUNT(*) FROM exhibitions" + where;
        String sql = "SELECT * FROM exhibitions" + where + " ORDER BY start_date DESC LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            int total;
            if (!where.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                    ps.setString(1, status);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        total = rs.getInt(1);
                    }
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(countSql);
                     ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    total = rs.getInt(1);
                }
            }

            List<Exhibition> list = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int idx = 1;
                if (!where.isEmpty()) ps.setString(idx++, status);
                ps.setInt(idx++, pageSize);
                ps.setInt(idx, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                }
            }
            return new PagedResult<>(list, total, page, pageSize);
        }
    }

    public Exhibition findById(int id) throws SQLException {
        String sql = "SELECT * FROM exhibitions WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Exhibition ex = mapRow(rs);
                    ex.setArtworks(findArtworksForExhibition(conn, id));
                    return ex;
                }
            }
        }
        return null;
    }

    public int create(Exhibition ex) throws SQLException {
        String sql = """
            INSERT INTO exhibitions (title, description, location, start_date, end_date, status)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindExhibition(ps, ex);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Failed to create exhibition");
    }

    public boolean update(Exhibition ex) throws SQLException {
        String sql = """
            UPDATE exhibitions SET title=?, description=?, location=?, start_date=?, end_date=?, status=?
            WHERE id=?
            """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindExhibition(ps, ex);
            ps.setInt(7, ex.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM exhibitions WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public void assignArtworks(int exhibitionId, List<Integer> artworkIds) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String deleteSql = "DELETE FROM exhibition_artworks WHERE exhibition_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setInt(1, exhibitionId);
                    ps.executeUpdate();
                }

                String insertSql = "INSERT INTO exhibition_artworks (exhibition_id, artwork_id, display_order) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    for (int i = 0; i < artworkIds.size(); i++) {
                        ps.setInt(1, exhibitionId);
                        ps.setInt(2, artworkIds.get(i));
                        ps.setInt(3, i + 1);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public int countActive() throws SQLException {
        String sql = "SELECT COUNT(*) FROM exhibitions WHERE status = 'active'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private List<Artwork> findArtworksForExhibition(Connection conn, int exhibitionId) throws SQLException {
        String sql = """
            SELECT a.*, ar.name AS artist_name
            FROM exhibition_artworks ea
            JOIN artworks a ON ea.artwork_id = a.id
            JOIN artists ar ON a.artist_id = ar.id
            WHERE ea.exhibition_id = ?
            ORDER BY ea.display_order
            """;
        List<Artwork> artworks = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, exhibitionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    artworks.add(mapArtworkRow(rs));
                }
            }
        }
        return artworks;
    }

    private void bindExhibition(PreparedStatement ps, Exhibition ex) throws SQLException {
        ps.setString(1, ex.getTitle());
        ps.setString(2, ex.getDescription());
        ps.setString(3, ex.getLocation());
        ps.setString(4, ex.getStartDate());
        ps.setString(5, ex.getEndDate());
        ps.setString(6, ex.getStatus());
    }

    private Exhibition mapRow(ResultSet rs) throws SQLException {
        Exhibition ex = new Exhibition();
        ex.setId(rs.getInt("id"));
        ex.setTitle(rs.getString("title"));
        ex.setDescription(rs.getString("description"));
        ex.setLocation(rs.getString("location"));
        ex.setStartDate(rs.getString("start_date"));
        ex.setEndDate(rs.getString("end_date"));
        ex.setStatus(rs.getString("status"));
        Timestamp created = rs.getTimestamp("created_at");
        ex.setCreatedAt(created != null ? created.toString() : null);
        Timestamp updated = rs.getTimestamp("updated_at");
        ex.setUpdatedAt(updated != null ? updated.toString() : null);
        return ex;
    }

    private Artwork mapArtworkRow(ResultSet rs) throws SQLException {
        Artwork a = new Artwork();
        a.setId(rs.getInt("id"));
        a.setTitle(rs.getString("title"));
        a.setArtistId(rs.getInt("artist_id"));
        a.setArtistName(rs.getString("artist_name"));
        a.setCategory(rs.getString("category"));
        a.setMedium(rs.getString("medium"));
        int year = rs.getInt("year_created");
        a.setYearCreated(rs.wasNull() ? null : year);
        a.setDimensions(rs.getString("dimensions"));
        a.setPrice(rs.getBigDecimal("price"));
        a.setStatus(rs.getString("status"));
        a.setImageUrl(rs.getString("image_url"));
        a.setDescription(rs.getString("description"));
        return a;
    }
}
