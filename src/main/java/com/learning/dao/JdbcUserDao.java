package com.learning.dao;

import com.learning.model.User;
import com.learning.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserDao implements UserDao {

    private static final String INSERT_USER_SQL = """
            INSERT INTO app_users (username, email, password_hash)
            VALUES (?, ?, ?)
            """;

    private static final String EXISTS_SQL = """
            SELECT COUNT(*)
            FROM app_users
            WHERE username = ? OR email = ?
            """;

    private static final String FIND_BY_USERNAME_SQL = """
            SELECT user_id, username, email, password_hash, created_at
            FROM app_users
            WHERE username = ?
            """;

    private static final String FIND_ALL_SQL = """
            SELECT user_id, username, email, password_hash, created_at
            FROM app_users
            ORDER BY user_id
            """;

    @Override
    public void create(User user) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_USER_SQL)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());

            int changedRows = statement.executeUpdate();
            if (changedRows != 1) {
                throw new SQLException("Expected to insert one user, but inserted " + changedRows);
            }
        }
    }

    @Override
    public boolean existsByUsernameOrEmail(String username, String email) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(EXISTS_SQL)) {

            statement.setString(1, username);
            statement.setString(2, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    @Override
    public Optional<User> findByUsername(String username) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_USERNAME_SQL)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
        }

        return users;
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");

        return new User(
                resultSet.getLong("user_id"),
                resultSet.getString("username"),
                resultSet.getString("email"),
                resultSet.getString("password_hash"),
                createdAt == null ? null : createdAt.toLocalDateTime()
        );
    }
}
