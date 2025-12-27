package com.azuredoom.hyleveling.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * A factory class for creating and configuring database connection pools using HikariCP.
 * <p>
 * This class provides a utility method to construct a {@link HikariDataSource} instance which
 * can be used for managing database connections in a high-performance and efficient manner.
 * It is designed for ease of use and supports configuration of core connection parameters
 * such as JDBC URL, username, and password.
 * <p>
 * This class is not meant to be instantiated and is designed to be used statically.
 */
public final class DataSourceFactory {

    private DataSourceFactory() {}

    /**
     * Creates and configures a HikariDataSource instance using the provided JDBC URL, username, and password.
     *
     * @param jdbcUrl  the JDBC URL to connect to the database
     * @param username the username for the database connection
     * @param password the password for the database connection
     * @return a configured HikariDataSource instance ready for database interactions
     */
    public static HikariDataSource create(String jdbcUrl, String username, String password) {
        var cfg = new HikariConfig();
        cfg.setJdbcUrl(jdbcUrl);
        cfg.setUsername(username);
        cfg.setPassword(password);

        cfg.setMaximumPoolSize(10);
        cfg.setMinimumIdle(1);

        return new HikariDataSource(cfg);
    }
}

