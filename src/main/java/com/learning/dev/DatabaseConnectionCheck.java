package com.learning.dev;

import com.learning.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public final class DatabaseConnectionCheck {
    private DatabaseConnectionCheck() {
    }

    public static void main(String[] args) throws Exception {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT current_user")) {

            resultSet.next();
            System.out.println("PostgreSQL connection succeeded as " + resultSet.getString(1));
            System.out.println("Database: " + connection.getMetaData().getDatabaseProductVersion());
        }
    }
}
