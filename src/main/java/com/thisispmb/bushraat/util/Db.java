package com.thisispmb.bushraat.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class Db {

    private static final HikariDataSource DATA_SOURCE = createDataSource();

    private Db() {
    }

    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }

    private static HikariDataSource createDataSource() {
        String jdbcUrl = Env.get("BUSHRAAT_DB_URL");
        String username = Env.get("BUSHRAAT_DB_USER");
        String password = Env.get("BUSHRAAT_DB_PASSWORD");

        if (jdbcUrl == null || username == null || password == null) {
            throw new IllegalStateException(
                    "Database configuration is missing. " +
                            "Set BUSHRAAT_DB_URL, BUSHRAAT_DB_USER and BUSHRAAT_DB_PASSWORD."
            );
        }

        HikariConfig config = new HikariConfig();

        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        config.setMaximumPoolSize(
                parseInt("BUSHRAAT_DB_MAX_POOL_SIZE", 5)
        );

        config.setMinimumIdle(
                parseInt("BUSHRAAT_DB_MIN_IDLE", 1)
        );

        config.setConnectionTimeout(
                parseLong("BUSHRAAT_DB_CONNECTION_TIMEOUT_MS", 10_000)
        );

        config.setValidationTimeout(
                parseLong("BUSHRAAT_DB_VALIDATION_TIMEOUT_MS", 5_000)
        );

        config.setPoolName("BushraatPool");

        if (!Boolean.parseBoolean(
                Env.get("BUSHRAAT_DB_PREPARED_STATEMENTS", "true"))) {

            config.addDataSourceProperty("prepareThreshold", 0);
            config.addDataSourceProperty(
                    "preparedStatementCacheQueries", 0
            );
        }

        return new HikariDataSource(config);
    }

    private static int parseInt(String key, int fallback) {
        try {
            return Integer.parseInt(
                    Env.get(key, String.valueOf(fallback))
            );
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static long parseLong(String key, long fallback) {
        try {
            return Long.parseLong(
                    Env.get(key, String.valueOf(fallback))
            );
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}