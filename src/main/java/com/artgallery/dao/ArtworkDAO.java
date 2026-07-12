package com.artgallery.dao;

import com.artgallery.config.DatabaseConfig;
import com.artgallery.model.Artwork;
import com.artgallery.model.PagedResult;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArtworkDAO {

    private static final String BASE_SELECT = """
        SELECT a.*, ar.name AS artist_name
        FROM artworks a
        JOIN artists ar ON a.artist_id = ar.id
        """;

    public PagedResult<Artwork> findAll(String search, String category, int page, int pageSize) throws SQLException {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            where.append(" AND (a.title LIKE ? OR ar.name LIKE ? OR a.medium LIKE ?)");
            String pattern = "%" + search.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        if (category != null && !category.isBlank()) {
            where.append(" AND a.category = ?");
            params.add(category);
        }

        int offset = (page - 1) * pageSize;
        String countSql = "SELECT COUNT(*) FROM artworks a JOIN artists ar ON a.artist_id = ar.id" + where;
        String sql = BASE_SELECT + where + " ORDER BY a.created_at DESC LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            int total = countWithParams(conn, countSql, params);
            params.add(pageSize);
            params.add(offset);

            List<Artwork> artworks = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                setParams(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        artworks.add(mapRow(rs));
                    }
                }
            }
            return new PagedResult<>(artworks, total, page, pageSize);
        }
    }

    public Artwork findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE a.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public int create(Artwork artwork) throws SQLException {
        String sql = """
            INSERT INTO artworks (title, artist_id, category, medium, year_created, dimensions,
            price, status, image_url, description)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindArtwork(ps, artwork);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Failed to create artwork");
    }

    public boolean update(Artwork artwork) throws SQLException {
        String sql = """
            UPDATE artworks SET title=?, artist_id=?, category=?, medium=?, year_created=?,
            dimensions=?, price=?, status=?, image_url=?, description=? WHERE id=?
            """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindArtwork(ps, artwork);
            ps.setInt(11, artwork.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM artworks WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM artworks";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public List<Artwork> countByCategory() throws SQLException {
        String sql = """
            SELECT category, COUNT(*) as cnt, NULL as id, category as title, 0 as artist_id,
            NULL as artist_name, NULL as medium, NULL as year_created, NULL as dimensions,
            NULL as price, NULL as status, NULL as image_url, NULL as description,
            NULL as created_at, NULL as updated_at
            FROM artworks GROUP BY category
            """;
        // Simpler approach - return via a report query in ReportDAO
        return new ArrayList<>();
    }

    private void bindArtwork(PreparedStatement ps, Artwork artwork) throws SQLException {
        ps.setString(1, artwork.getTitle());
        ps.setInt(2, artwork.getArtistId());
        ps.setString(3, artwork.getCategory());
        ps.setString(4, artwork.getMedium());
        if (artwork.getYearCreated() != null) ps.setInt(5, artwork.getYearCreated()); else ps.setNull(5, Types.INTEGER);
        ps.setString(6, artwork.getDimensions());
        if (artwork.getPrice() != null) ps.setBigDecimal(7, artwork.getPrice()); else ps.setNull(7, Types.DECIMAL);
        ps.setString(8, artwork.getStatus());
        ps.setString(9, artwork.getImageUrl());
        ps.setString(10, artwork.getDescription());
    }

    private Artwork mapRow(ResultSet rs) throws SQLException {
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
        BigDecimal price = rs.getBigDecimal("price");
        a.setPrice(price);
        a.setStatus(rs.getString("status"));
        a.setImageUrl(rs.getString("image_url"));
        a.setDescription(rs.getString("description"));
        Timestamp created = rs.getTimestamp("created_at");
        a.setCreatedAt(created != null ? created.toString() : null);
        Timestamp updated = rs.getTimestamp("updated_at");
        a.setUpdatedAt(updated != null ? updated.toString() : null);
        return a;
    }

    private int countWithParams(Connection conn, String sql, List<Object> params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object p = params.get(i);
            if (p instanceof String s) ps.setString(i + 1, s);
            else if (p instanceof Integer n) ps.setInt(i + 1, n);
        }
    }
}
