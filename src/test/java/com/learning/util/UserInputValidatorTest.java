package com.learning.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserInputValidatorTest {
    @Test
    void acceptsValidUserInput() {
        assertNull(UserInputValidator.validationError("farooq_1", "farooq@example.com"));
    }

    @Test
    void rejectsInvalidUsername() {
        assertNotNull(UserInputValidator.validationError("a", "farooq@example.com"));
        assertNotNull(UserInputValidator.validationError("farooq!", "farooq@example.com"));
    }

    @Test
    void rejectsInvalidEmail() {
        assertNotNull(UserInputValidator.validationError("farooq", "not-an-email"));
    }
}
