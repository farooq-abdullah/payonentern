package com.learning.dao;

import com.learning.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UserDao {
    void create(User user) throws SQLException;

    boolean existsByUsernameOrEmail(String username, String email) throws SQLException;

    boolean existsByUsernameOrEmailExceptId(String username, String email, long userId) throws SQLException;

    Optional<User> findByUsername(String username) throws SQLException;

    Optional<User> findById(long userId) throws SQLException;

    List<User> findAll() throws SQLException;

    long countAll() throws SQLException;

    boolean updateProfile(User user) throws SQLException;

    boolean deleteById(long userId) throws SQLException;

    long countByRoleId(long roleId) throws SQLException;

    boolean updatePassword(long userId, String passwordHash, boolean mustChangePassword) throws SQLException;

    List<String> findRecentPasswordHashes(long userId, int limit) throws SQLException;
}
