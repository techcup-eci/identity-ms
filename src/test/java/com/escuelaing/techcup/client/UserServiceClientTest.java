package com.escuelaing.techcup.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for UserServiceClient — WebClient-based HTTP client.
 */
class UserServiceClientTest {

    @Test
    @DisplayName("CreateUserRequest must have all required fields")
    void createUserRequestShouldHaveRequiredFields() throws NoSuchMethodException {
        UserServiceClient.CreateUserRequest req = new UserServiceClient.CreateUserRequest();
        req.setName("Test User");
        req.setEmail("test@escuelaing.edu.co");

        assertEquals("Test User", req.getName());
        assertEquals("test@escuelaing.edu.co", req.getEmail());
    }

    @Test
    @DisplayName("UpdateSystemRoleRequest must set role correctly")
    void updateSystemRoleRequestShouldSetRole() {
        UserServiceClient.UpdateSystemRoleRequest req =
                new UserServiceClient.UpdateSystemRoleRequest("CAPTAIN");

        assertEquals("CAPTAIN", req.getRole());
    }

    @Test
    @DisplayName("UserServiceResponse must map id correctly")
    void userServiceResponseShouldMapId() {
        UserServiceClient.UserServiceResponse res = new UserServiceClient.UserServiceResponse();
        res.setId(42L);

        assertEquals(42L, res.getId());
    }
}
