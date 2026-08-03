package com.learning.dao;

import com.learning.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UserDao {
    void create(User user) throws SQLException;

    boolean existsByUsernameOrEmail(String username, String email) throws SQLException;

    Optional<User> findByUsername(String username) throws SQLException;

    List<User> findAll() throws SQLException;
}
