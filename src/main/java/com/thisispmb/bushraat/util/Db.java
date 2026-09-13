package com.thisispmb.bushraat.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class Db {
    private static final HikariDataSource DATA_SOURCE;

    static {
        HikariConfig config = new HikariConfig();

        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl(value("BUSHRAAT_DB_URL",
                "jdbc:postgresql://localhost:5432/bushraat"
        ));
        config.setUsername(value("BUSHRAAT_DB_USER", "postgres"));
        config.setPassword(value("BUSHRAAT_DB_PASSWORD", "260803"));
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setPoolName("BushraatPool");

        DATA_SOURCE = new HikariDataSource(config);
    }

    private Db() {
    }

    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }

    private static String value(String key, String fallback) {
        String value = System.getProperty(key);

        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }

        return value == null || value.isBlank() ? fallback : value;
    }
}
