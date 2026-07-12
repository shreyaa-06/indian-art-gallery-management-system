package com.artgallery.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.artgallery.util.PasswordUtil;

public final class DatabaseInitializer {
    private DatabaseInitializer() {}

    public static void initialize() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            ensureAdminUser(conn);
            seedDemoData(conn);
        } catch (SQLException e) {
            System.err.println("Database initialization warning: " + e.getMessage());
            e.printStackTrace(System.err);
            System.err.println("Ensure MySQL is running and schema is created. See README.md");
        }
    }

    private static void ensureAdminUser(Connection conn) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM users";
        try (PreparedStatement ps = conn.prepareStatement(countSql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            if (rs.getInt(1) > 0) return;
        }

        String insertSql = """
            INSERT INTO users (username, password_hash, full_name, role)
            VALUES ('admin', ?, 'Gallery Administrator', 'admin')
            """;
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, PasswordUtil.hash("admin123"));
            ps.executeUpdate();
            System.out.println("Created default admin user (admin / admin123)");
        }
    }

    private static void seedDemoData(Connection conn) throws SQLException {
        if (!AppConfig.getBoolean("db.reseed", false)) {
            return;
        }

        Path seedPath = Path.of("database", "seed.sql").toAbsolutePath();
        if (!Files.exists(seedPath)) {
            throw new SQLException("Seed file not found: " + seedPath);
        }

        try {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
                stmt.execute("TRUNCATE TABLE exhibition_artworks");
                stmt.execute("TRUNCATE TABLE activity_log");
                stmt.execute("TRUNCATE TABLE artworks");
                stmt.execute("TRUNCATE TABLE exhibitions");
                stmt.execute("TRUNCATE TABLE customers");
                stmt.execute("TRUNCATE TABLE artists");
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            }

            String seedSql = Files.readString(seedPath);
            for (String statement : splitSqlStatements(seedSql)) {
                String trimmed = statement.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--") || trimmed.toLowerCase().startsWith("use ")) {
                    continue;
                }
                try (PreparedStatement ps = conn.prepareStatement(trimmed)) {
                    ps.executeUpdate();
                }
            }

            conn.commit();
            System.out.println("Reseeded database from " + seedPath);
        } catch (Exception e) {
            conn.rollback();
            throw new SQLException("Failed to reseed database", e);
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private static List<String> splitSqlStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;

        for (int i = 0; i < sql.length(); i++) {
            char ch = sql.charAt(i);
            if (ch == '\'' && (i == 0 || sql.charAt(i - 1) != '\\')) {
                inSingleQuote = !inSingleQuote;
            }

            if (ch == ';' && !inSingleQuote) {
                String statement = current.toString().trim();
                if (!statement.isEmpty()) {
                    statements.add(statement);
                }
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        String remainder = current.toString().trim();
        if (!remainder.isEmpty()) {
            statements.add(remainder);
        }

        return statements;
    }
}
