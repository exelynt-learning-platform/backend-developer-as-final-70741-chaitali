package com.booking.resource_booking_system.controller;

import com.booking.resource_booking_system.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private String userToken() {
        return jwtService.generateToken("user");
    }

    private String adminToken() {
        return jwtService.generateToken("admin");
    }

    @Test
    void unauthenticatedGetReservationsReturns401() throws Exception {
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedGetReservationByIdReturns401() throws Exception {
        mockMvc.perform(get("/reservations/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCanGetReservations() throws Exception {
        mockMvc.perform(get("/reservations")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanGetReservations() throws Exception {
        mockMvc.perform(get("/reservations")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk());
    }

    @Test
    void userCannotCreateResource() throws Exception {
        String json = """
                {
                    "name": "Security Test Resource",
                    "description": "Test",
                    "price": 100.00,
                    "available": true
                }
                """;

        mockMvc.perform(post("/resources")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCannotUpdateResource() throws Exception {
        String json = """
                {
                    "name": "Updated Resource",
                    "description": "Test",
                    "price": 100.00,
                    "available": true
                }
                """;

        mockMvc.perform(put("/resources/1")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCannotDeleteResource() throws Exception {
        mockMvc.perform(delete("/resources/1")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCannotUpdateReservation() throws Exception {
        String json = """
                {
                    "startTime": "2030-01-01T10:00:00",
                    "endTime": "2030-01-01T11:00:00",
                    "price": 100.00,
                    "status": "PENDING"
                }
                """;

        mockMvc.perform(put("/reservations/1")
                        .header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCannotDeleteReservation() throws Exception {
        mockMvc.perform(delete("/reservations/1")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidJwtReturns401() throws Exception {
        mockMvc.perform(get("/reservations")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }
}