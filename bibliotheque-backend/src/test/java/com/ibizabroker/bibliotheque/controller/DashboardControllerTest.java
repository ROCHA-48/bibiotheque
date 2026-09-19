package com.ibizabroker.bibliotheque.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn401WhenNoTokenIsProvided() throws Exception {
        mockMvc.perform(get("/admin/dashboard/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"Admin"})
    void shouldReturnStatsForAdmin() throws Exception {
        mockMvc.perform(get("/admin/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBooks").isNumber())
                .andExpect(jsonPath("$.totalUsers").isNumber())
                .andExpect(jsonPath("$.activeBorrows").isNumber())
                .andExpect(jsonPath("$.pendingReservations").isNumber());
    }

    @Test
    @WithMockUser(username = "membre", roles = {"User"})
    void shouldReturn403ForSimpleUser() throws Exception {
        mockMvc.perform(get("/admin/dashboard/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"Admin"})
    void shouldReturnChartsForAdmin() throws Exception {
        mockMvc.perform(get("/admin/dashboard/chart/books-by-genre"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels").isArray())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/admin/dashboard/chart/reservation-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels").isArray());

        mockMvc.perform(get("/admin/dashboard/chart/borrows-by-month"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels").isArray());
    }
}