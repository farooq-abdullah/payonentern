package com.learning.api;

import com.learning.model.Permission;
import com.learning.model.Role;
import com.learning.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiViewMapperTest {
    @Test
    void userResponseIncludesOnlySafePublicFields() {
        Role role = new Role();
        role.setId(4);
        role.setName("Administrator");
        role.setFunctions(Set.of(new Permission("VIEW_USERS")));

        User user = new User();
        user.setId(7);
        user.setUsername("farooq");
        user.setEmail("farooq@example.com");
        user.setPasswordHash("must-never-leave-the-server");
        user.setMustChangePassword(true);
        user.setFailedLoginCount(3);
        user.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        user.setRole(role);

        var response = ApiViewMapper.user(user);

        assertEquals("farooq", response.get("username"));
        assertTrue(response.containsKey("role"));
        assertFalse(response.containsKey("passwordHash"));
        assertFalse(response.containsKey("mustChangePassword"));
        assertFalse(response.containsKey("failedLoginCount"));
        assertFalse(response.containsKey("lockedUntil"));
    }
}
