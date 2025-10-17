package com.ecommerce.analyzer.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database Utility Class
 * Handles PostgreSQL database connections using JDBC
 */
public class DBUtil {
    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;

    // Static block - runs once when class is loaded
    static {
        try {
            // Load database properties from application.properties
            Properties props = new Properties();
            InputStream input = DBUtil.class.getClassLoader()
                    .getResourceAsStream("application.properties");

            if (input == null) {
                throw new RuntimeException("Unable to find application.properties");
            }

            props.load(input);

            URL = props.getProperty("db.url");
            USERNAME = props.getProperty("db.username");
            PASSWORD = props.getProperty("db.password");

            // Load PostgreSQL JDBC Driver
            Class.forName("org.postgresql.Driver");

            System.out.println("Database configuration loaded successfully");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database configuration", e);
        }
    }

    /**
     * Get a new database connection
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * Close database connection safely
     * @param conn Connection to close
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Test database connectivity
     * @return true if connection successful
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}