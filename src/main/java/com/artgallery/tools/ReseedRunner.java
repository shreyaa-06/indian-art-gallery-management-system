package com.artgallery.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.artgallery.config.DatabaseConfig;

public class ReseedRunner {
    public static void main(String[] args) {
        Path seedPath = Path.of("database", "seed.sql").toAbsolutePath();
        if (!Files.exists(seedPath)) {
            System.err.println("Seed file not found: " + seedPath);
            System.exit(2);
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            System.out.println("Connected to DB. Applying seed: " + seedPath);
            String sql = Files.readString(seedPath);
            List<String> statements = splitSqlStatements(sql);
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

                for (String s : statements) {
                    String t = s.trim();
                    if (t.isEmpty() || t.startsWith("--") || t.toLowerCase().startsWith("use ")) continue;
                    System.out.println("Executing: " + (t.length() > 80 ? t.substring(0, 80) + "..." : t));
                    stmt.executeUpdate(t);
                }
            }
            conn.commit();
            System.out.println("Seed applied successfully.");
        } catch (SQLException | IOException e) {
            System.err.println("Failed to apply seed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static java.util.List<String> splitSqlStatements(String sql) {
        java.util.List<String> statements = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingle = false;
        for (int i = 0; i < sql.length(); i++) {
            char ch = sql.charAt(i);
            if (ch == '\'' && (i == 0 || sql.charAt(i - 1) != '\\')) inSingle = !inSingle;
            if (ch == ';' && !inSingle) {
                String st = current.toString().trim();
                if (!st.isEmpty()) statements.add(st);
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        String rem = current.toString().trim();
        if (!rem.isEmpty()) statements.add(rem);
        return statements;
    }
}
