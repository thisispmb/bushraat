package com.thisispmb.bushraat.repository;

import com.thisispmb.bushraat.util.Db;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.HexFormat;
import java.util.UUID;

public class PasswordResetRepository {
    private static volatile boolean initialized;

    private static synchronized void ensureTable() throws SQLException {
        if (initialized) {
            return;
        }

        try (Connection c = Db.getConnection();
             Statement s = c.createStatement()) {
            s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS password_reset_tokens (
                        id BIGSERIAL PRIMARY KEY,
                        user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                        token_hash VARCHAR(64) NOT NULL UNIQUE,
                        expires_at TIMESTAMP NOT NULL,
                        used_at TIMESTAMP
                    )
                    """);

            initialized = true;
        }
    }

    public String create(Long userId) throws SQLException {
        ensureTable();

        String token = UUID.randomUUID() + "-" + UUID.randomUUID();
        String hash = hash(token);

        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO password_reset_tokens(user_id,token_hash,expires_at) VALUES(?,?,CURRENT_TIMESTAMP + INTERVAL '30 minutes')")) {
            ps.setLong(1, userId);
            ps.setString(2, hash);
            ps.executeUpdate();
        }

        return token;
    }

    public Long consume(String token) throws SQLException {
        ensureTable();

        if (token == null || token.isBlank()) {
            return null;
        }

        try (Connection c = Db.getConnection()) {
            c.setAutoCommit(false);

            try (PreparedStatement ps = c.prepareStatement("""
                    SELECT id, user_id
                    FROM password_reset_tokens
                    WHERE token_hash = ?
                      AND expires_at > CURRENT_TIMESTAMP
                      AND used_at IS NULL
                    FOR UPDATE
                    """)
            ) {
                ps.setString(1, hash(token));

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        c.rollback();
                        return null;
                    }

                    long id = rs.getLong("id"), user = rs.getLong("user_id");

                    try (PreparedStatement u = c.prepareStatement("""
                            UPDATE password_reset_tokens
                            SET used_at = CURRENT_TIMESTAMP
                            WHERE id = ?
                            """)
                    ) {
                        u.setLong(1, id);
                        u.executeUpdate();
                    }

                    c.commit();
                    return user;
                }
            }
        }
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
