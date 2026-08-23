package com.learning.util;

import com.learning.model.Permission;
import com.learning.model.Role;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FullAdminProtectionTest {
    @Test
    void identifiesARoleContainingEverySystemFunction() {
        Role role = new Role();
        role.setFunctions(Set.of(
                new Permission(Permissions.VIEW_USERS),
                new Permission(Permissions.MANAGE_ROLES)));

        assertTrue(FullAdminProtection.isFullAdministrator(
                role, Set.of(Permissions.VIEW_USERS, Permissions.MANAGE_ROLES)));
        assertFalse(FullAdminProtection.isFullAdministrator(
                role, Set.of(Permissions.VIEW_USERS, Permissions.DELETE_USER)));
    }
}
