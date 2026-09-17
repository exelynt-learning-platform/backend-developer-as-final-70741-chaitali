package com.booking.resource_booking_system.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {

        jwtService = new JwtService();

        ReflectionTestUtils.setField(
                jwtService,
                "secret",
                "test-secret-key-that-is-at-least-32-bytes-long-12345"
        );

        ReflectionTestUtils.setField(
                jwtService,
                "expirationMs",
                86400000L
        );

        ReflectionTestUtils.setField(
                jwtService,
                "issuer",
                "resource-booking-system"
        );

        ReflectionTestUtils.setField(
                jwtService,
                "audience",
                "resource-booking-api"
        );
    }

    @Test
    void shouldGenerateAndValidateToken() {

        String username = "testuser";

        String token = jwtService.generateToken(username);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, username));
        assertEquals(
                username,
                jwtService.extractUsername(token)
        );
    }

    @Test
    void shouldRejectTokenForDifferentUsername() {

        String token =
                jwtService.generateToken("testuser");

        assertFalse(
                jwtService.isTokenValid(
                        token,
                        "anotheruser"
                )
        );
    }

    @Test
    void shouldRejectInvalidToken() {

        assertFalse(
                jwtService.isTokenValid(
                        "invalid.token.value",
                        "testuser"
                )
        );
    }
}