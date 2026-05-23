// package com.escuelaing.techcup;

// import com.escuelaing.techcup.security.JwtUtil;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import static org.junit.jupiter.api.Assertions.*;

// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// class AppTest {

//     @Autowired
//     private JwtUtil jwtUtil;

//     @Test
//     void contextLoads() {
//         assertNotNull(jwtUtil);
//     }

//     @Test
//     void tokenGenerationAndValidation() {
//         String token = jwtUtil.generateToken(1L, "test@test.com", "PLAYER", "Test User");
//         assertNotNull(token);
//         assertTrue(jwtUtil.validateToken(token));
//         assertEquals("test@test.com", jwtUtil.extractEmail(token));
//         assertEquals("PLAYER", jwtUtil.extractRole(token));
//         assertEquals("1", jwtUtil.extractUserId(token));
//         assertEquals("Test User", jwtUtil.extractName(token));
//     }

//     @Test
//     void refreshTokenGeneration() {
//         JwtUtil.TokenPair pair = jwtUtil.generateRefreshToken();
//         assertNotNull(pair.getRaw());
//         assertNotNull(pair.getHashed());
//         assertNotEquals(pair.getRaw(), pair.getHashed());
//     }

//     @Test
//     void sha256Hash() {
//         String hash = JwtUtil.sha256Hex("test-input");
//         assertNotNull(hash);
//         assertEquals(64, hash.length()); // SHA-256 siempre produce 64 caracteres hex
//     }
// }