package com.learning.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {
    public static final String URL_VARIABLE = "DB_URL";
    public static final String USER_VARIABLE = "DB_USER";
    public static final String PASSWORD_VARIABLE = "DB_PASSWORD";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        loadDriver();
        String url = requiredEnvironmentVariable(URL_VARIABLE);
        String user = requiredEnvironmentVariable(USER_VARIABLE);
        String password = requiredEnvironmentVariable(PASSWORD_VARIABLE);

        return DriverManager.getConnection(url, user, password);
    }

    private static void loadDriver() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("PostgreSQL JDBC driver is not available", exception);
        }
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);

        if (value == null || value.isBlank() || "CHANGE_ME".equals(value)) {
            throw new IllegalStateException(
                    "Missing environment variable " + name
                            + ". Update .env.local and run through the provided scripts.");
        }

        return value;
    }
}
