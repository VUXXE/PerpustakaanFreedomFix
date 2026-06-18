package com.kelompok1.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilTest {

    @Test
    public void testHashAndVerifyPassword() {
        String plainPassword = "testPassword123";
        
        // Test hashing
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        assertNotNull(hashedPassword);
        assertNotEquals(plainPassword, hashedPassword);
        
        // Test verification with correct password
        assertTrue(PasswordUtil.verifyPassword(plainPassword, hashedPassword), "Password verification should succeed with correct password");
        
        // Test verification with incorrect password
        assertFalse(PasswordUtil.verifyPassword("wrongPassword", hashedPassword), "Password verification should fail with incorrect password");
    }
}
