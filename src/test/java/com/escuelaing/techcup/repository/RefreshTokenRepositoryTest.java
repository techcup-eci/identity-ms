// package com.escuelaing.techcup.repository;

// import com.escuelaing.techcup.model.RefreshToken;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
// import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
// import org.springframework.test.context.TestPropertySource;

// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.*;

// /**
//  * TDD: Tests for RefreshTokenRepository.
//  * Validates CRUD operations and custom query methods using H2 in-memory database.
//  */
// @DataJpaTest
// @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
// @TestPropertySource(properties = {
//         "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
//         "spring.datasource.driver-class-name=org.h2.Driver",
//         "spring.datasource.username=sa",
//         "spring.datasource.password=",
//         "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
//         "spring.jpa.hibernate.ddl-auto=create-drop",
//         "spring.sql.init.mode=never",
//         "spring.cloud.openfeign.enabled=false"
// })
// class RefreshTokenRepositoryTest {

//     @Autowired
//     private TestEntityManager entityManager;

//     @Autowired
//     private RefreshTokenRepository repository;

//     private RefreshToken activeToken;
//     private RefreshToken revokedToken;

//     @BeforeEach
//     void setUp() {
//         // Create an active refresh token
//         activeToken = new RefreshToken();
//         activeToken.setTokenHash("a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2");
//         activeToken.setUserId(42L);
//         activeToken.setUsersMsUserId(99L);
//         activeToken.setCreatedAt(LocalDateTime.now());
//         activeToken.setExpiresAt(LocalDateTime.now().plusDays(7));
//         activeToken.setRevoked(false);
//         entityManager.persist(activeToken);

//         // Create a revoked refresh token
//         revokedToken = new RefreshToken();
//         revokedToken.setTokenHash("b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3");
//         revokedToken.setUserId(42L);
//         revokedToken.setCreatedAt(LocalDateTime.now());
//         revokedToken.setExpiresAt(LocalDateTime.now().plusDays(7));
//         revokedToken.setRevoked(true);
//         entityManager.persist(revokedToken);

//         entityManager.flush();
//     }

//     @Nested
//     @DisplayName("findByTokenHash")
//     class FindByTokenHash {

//         @Test
//         @DisplayName("Should find token by exact hash match")
//         void shouldFindByExactHash() {
//             Optional<RefreshToken> found = repository.findByTokenHash(
//                     "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2");

//             assertTrue(found.isPresent(), "Should find token with matching hash");
//             assertEquals(42L, found.get().getUserId());
//             assertFalse(found.get().isRevoked());
//         }

//         @Test
//         @DisplayName("Should return empty when hash does not exist")
//         void shouldReturnEmptyForUnknownHash() {
//             Optional<RefreshToken> found = repository.findByTokenHash("unknown_hash");

//             assertFalse(found.isPresent(), "Should return empty for non-existent hash");
//         }
//     }

//     @Nested
//     @DisplayName("findByUserIdAndRevokedFalse")
//     class FindByUserIdAndRevokedFalse {

//         @Test
//         @DisplayName("Should return only non-revoked tokens for a user")
//         void shouldReturnOnlyActiveTokens() {
//             List<RefreshToken> tokens = repository.findByUserIdAndRevokedFalse(42L);

//             assertEquals(1, tokens.size(), "Should return exactly 1 active token");
//             assertFalse(tokens.get(0).isRevoked(), "Returned token must not be revoked");
//         }

//         @Test
//         @DisplayName("Should return empty for user with only revoked tokens")
//         void shouldReturnEmptyWhenAllRevoked() {
//             // User 42 has one revoked and one active — revoke the active one
//             activeToken.setRevoked(true);
//             entityManager.persist(activeToken);
//             entityManager.flush();

//             List<RefreshToken> tokens = repository.findByUserIdAndRevokedFalse(42L);

//             assertTrue(tokens.isEmpty(),
//                     "Should return empty list when all tokens are revoked");
//         }

//         @Test
//         @DisplayName("Should return empty for user with no tokens")
//         void shouldReturnEmptyForUnknownUser() {
//             List<RefreshToken> tokens = repository.findByUserIdAndRevokedFalse(999L);

//             assertTrue(tokens.isEmpty(),
//                     "Should return empty list for user with no tokens");
//         }
//     }

//     @Nested
//     @DisplayName("revokeAllByUserId")
//     class RevokeAllByUserId {

//         @Test
//         @DisplayName("Should revoke all tokens for a given user")
//         void shouldRevokeAllTokens() {
//             int updated = repository.revokeAllByUserId(42L);

//             // Need to clear persistence context to see DB changes
//             entityManager.clear();

//             List<RefreshToken> tokens = repository.findByUserIdAndRevokedFalse(42L);
//             assertTrue(tokens.isEmpty(),
//                     "After revokeAllByUserId, no active tokens should remain");
//         }
//     }

//     @Nested
//     @DisplayName("deleteByUserId")
//     class DeleteByUserId {

//         @Test
//         @DisplayName("Should delete all tokens for a given user")
//         void shouldDeleteAllTokens() {
//             repository.deleteByUserId(42L);
//             entityManager.flush();

//             List<RefreshToken> tokens = repository.findByUserIdAndRevokedFalse(42L);
//             assertTrue(tokens.isEmpty(),
//                     "After deleteByUserId, no tokens should remain");
//         }
//     }

//     @Nested
//     @DisplayName("Entity defaults")
//     class EntityDefaults {

//         @Test
//         @DisplayName("revoked field should default to false when persisted")
//         void revokedShouldDefaultToFalse() {
//             RefreshToken fresh = new RefreshToken();
//             fresh.setTokenHash("unique_hash_default_test");
//             fresh.setUserId(10L);
//             fresh.setCreatedAt(LocalDateTime.now());
//             fresh.setExpiresAt(LocalDateTime.now().plusDays(7));
//             // Do NOT set revoked explicitly

//             entityManager.persist(fresh);
//             entityManager.flush();
//             entityManager.clear();

//             RefreshToken found = entityManager.find(RefreshToken.class, fresh.getId());
//             assertNotNull(found);
//             assertFalse(found.isRevoked(),
//                     "revoked must default to false when not explicitly set");
//         }
//     }
// }
