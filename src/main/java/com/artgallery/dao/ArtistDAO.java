package com.artgallery.dao;

import com.artgallery.config.DatabaseConfig;
import com.artgallery.model.Artist;
import com.artgallery.model.PagedResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArtistDAO {

    public PagedResult<Artist> findAll(String search, int page, int pageSize) throws SQLException {
        String where = "";
        if (search != null && !search.isBlank()) {
            where = " WHERE name LIKE ? OR nationality LIKE ? OR email LIKE ?";
        }

        int offset = (page - 1) * pageSize;
        String countSql = "SELECT COUNT(*) FROM artists" + where;
        String sql = "SELECT * FROM artists" + where + " ORDER BY name ASC LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            int total = countTotal(conn, countSql, search);
            List<Artist> artists = new ArrayList<>();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int idx = setSearchParams(ps, search, 1);
                ps.setInt(idx, pageSize);
                ps.setInt(idx + 1, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        artists.add(mapRow(rs));
                    }
                }
            }
            return new PagedResult<>(artists, total, page, pageSize);
        }
    }

    public List<Artist> findAllSimple() throws SQLException {
        String sql = "SELECT * FROM artists ORDER BY name ASC";
        List<Artist> artists = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                artists.add(mapRow(rs));
            }
        }
        return artists;
    }

    public Artist findById(int id) throws SQLException {
        String sql = "SELECT * FROM artists WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public int create(Artist artist) throws SQLException {
        String sql = """
            INSERT INTO artists (name, nationality, birth_year, death_year, email, phone, bio)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindArtist(ps, artist);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Failed to create artist");
    }

    public boolean update(Artist artist) throws SQLException {
        String sql = """
            UPDATE artists SET name=?, nationality=?, birth_year=?, death_year=?,
            email=?, phone=?, bio=? WHERE id=?
            """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindArtist(ps, artist);
            ps.setInt(8, artist.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM artists WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM artists";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private void bindArtist(PreparedStatement ps, Artist artist) throws SQLException {
        ps.setString(1, artist.getName());
        ps.setString(2, artist.getNationality());
        if (artist.getBirthYear() != null) ps.setInt(3, artist.getBirthYear()); else ps.setNull(3, Types.INTEGER);
        if (artist.getDeathYear() != null) ps.setInt(4, artist.getDeathYear()); else ps.setNull(4, Types.INTEGER);
        ps.setString(5, artist.getEmail());
        ps.setString(6, artist.getPhone());
        ps.setString(7, artist.getBio());
    }

    private Artist mapRow(ResultSet rs) throws SQLException {
        Artist a = new Artist();
        a.setId(rs.getInt("id"));
        a.setName(rs.getString("name"));
        a.setNationality(rs.getString("nationality"));
        int birth = rs.getInt("birth_year");
        a.setBirthYear(rs.wasNull() ? null : birth);
        int death = rs.getInt("death_year");
        a.setDeathYear(rs.wasNull() ? null : death);
        a.setEmail(rs.getString("email"));
        a.setPhone(rs.getString("phone"));
        a.setBio(rs.getString("bio"));
        Timestamp created = rs.getTimestamp("created_at");
        a.setCreatedAt(created != null ? created.toString() : null);
        Timestamp updated = rs.getTimestamp("updated_at");
        a.setUpdatedAt(updated != null ? updated.toString() : null);
        return a;
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
