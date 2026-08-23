package com.learning.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RoleInputValidatorTest {
    @Test
    void acceptsRoleNamesAllowedByTheForm() {
        assertNull(RoleInputValidator.validationError("Hotel Manager"));
    }

    @Test
    void rejectsNamesThatAreTooShortOrContainUnsupportedCharacters() {
        assertNotNull(RoleInputValidator.validationError("HR"));
        assertNotNull(RoleInputValidator.validationError("Manager!"));
    }
}
