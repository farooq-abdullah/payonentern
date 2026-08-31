package com.learning.dao;

import com.learning.model.Permission;
import com.learning.model.Role;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoleDao {
    List<Role> findAll() throws SQLException;

    Optional<Role> findById(long roleId) throws SQLException;

    Optional<Role> findByName(String roleName) throws SQLException;

    Optional<Role> findDefaultRole() throws SQLException;

    List<Permission> findAllFunctions() throws SQLException;

    Set<String> findAllFunctionCodes() throws SQLException;

    boolean create(String name, Set<String> functionCodes) throws SQLException;

    boolean update(long roleId, String name, Set<String> functionCodes) throws SQLException;

    boolean deleteById(long roleId) throws SQLException;
}
