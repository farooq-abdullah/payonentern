package com.learning.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ResetTokenHasherTest {
    @Test
    void generatedTokenIsStoredAsADeterministicOneWayHash() {
        String token = ResetTokenHasher.generateToken();
        String hash = ResetTokenHasher.hash(token);

        assertNotEquals(token, hash);
        assertEquals(64, hash.length());
        assertEquals(hash, ResetTokenHasher.hash(token));
    }
}
