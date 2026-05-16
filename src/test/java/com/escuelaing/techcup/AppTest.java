package com.escuelaing.techcup;

import com.escuelaing.techcup.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AppTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void contextLoads() {
        assertNotNull(jwtUtil);
    }

    @Test
    void tokenGenerationAndValidation() {
        String token = jwtUtil.generateToken("test@test.com", "PLAYER");
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals("test@test.com", jwtUtil.extractEmail(token));
        assertEquals("PLAYER", jwtUtil.extractRole(token));
    }
}