package com.escuelaing.techcup.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD: RED phase — Tests for UserServiceClient Feign interface.
 * This test will fail to compile until the UserServiceClient class is created.
 */
class UserServiceClientTest {

    @Test
    @DisplayName("UserServiceClient interface must be annotated with @FeignClient")
    void shouldHaveFeignClientAnnotation() {
        FeignClient annotation = UserServiceClient.class.getAnnotation(FeignClient.class);

        assertNotNull(annotation, "@FeignClient annotation must be present on UserServiceClient");
    }

    @Test
    @DisplayName("@FeignClient name must be 'users-and-players-ms'")
    void shouldHaveCorrectFeignClientName() {
        FeignClient annotation = UserServiceClient.class.getAnnotation(FeignClient.class);

        assertNotNull(annotation);
        assertEquals("users-and-players-ms", annotation.name(),
                "FeignClient name must match the target service name");
    }

    @Test
    @DisplayName("createUser method must have @PostMapping(\"/User\")")
    void createUserShouldHavePostMappingAnnotation() throws NoSuchMethodException {
        PostMapping annotation = UserServiceClient.class
                .getMethod("createUser", UserServiceClient.CreateUserRequest.class)
                .getAnnotation(PostMapping.class);

        assertNotNull(annotation, "createUser must have @PostMapping annotation");
        assertArrayEquals(new String[]{"/User"}, annotation.value(),
                "createUser must map to POST /User");
        assertEquals(1, annotation.value().length,
                "createUser must have exactly one path value");
    }

    @Test
    @DisplayName("createUser method must return UserServiceClient.UserServiceResponse")
    void createUserShouldReturnUserServiceResponse() throws NoSuchMethodException {
        Class<?> returnType = UserServiceClient.class
                .getMethod("createUser", UserServiceClient.CreateUserRequest.class)
                .getReturnType();

        assertEquals(UserServiceClient.UserServiceResponse.class, returnType,
                "createUser must return UserServiceResponse");
    }
}
