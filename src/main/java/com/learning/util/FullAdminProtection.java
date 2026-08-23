package com.learning.util;

import com.learning.dao.RoleDao;
import com.learning.dao.UserDao;
import com.learning.model.Role;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

public final class FullAdminProtection {
    private FullAdminProtection() {
    }

    public static boolean isFullAdministrator(Role role, Set<String> allFunctionCodes) {
        return role != null
                && !allFunctionCodes.isEmpty()
                && role.getFunctions().stream().map(function -> function.getCode()).collect(java.util.stream.Collectors.toSet())
                .containsAll(allFunctionCodes);
    }

    public static Optional<Role> findFullAdministratorRole(RoleDao roleDao) throws SQLException {
        Set<String> allFunctionCodes = roleDao.findAllFunctionCodes();
        return roleDao.findAll().stream()
                .filter(role -> isFullAdministrator(role, allFunctionCodes))
                .findFirst();
    }

    public static long countFullAdministrators(RoleDao roleDao, UserDao userDao) throws SQLException {
        Set<String> allFunctionCodes = roleDao.findAllFunctionCodes();
        long count = 0;
        for (Role role : roleDao.findAll()) {
            if (isFullAdministrator(role, allFunctionCodes)) {
                count += userDao.countByRoleId(role.getId());
            }
        }
        return count;
    }

    public static boolean selectedFunctionsAreFullAdministrative(Set<String> selectedFunctions, Set<String> allFunctionCodes) {
        return selectedFunctions.containsAll(allFunctionCodes);
    }
}
