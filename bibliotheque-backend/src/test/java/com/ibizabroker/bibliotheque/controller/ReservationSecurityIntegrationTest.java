package com.ibizabroker.bibliotheque.controller;

import com.ibizabroker.bibliotheque.entity.Reservation;
import com.ibizabroker.bibliotheque.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    @Test
    void shouldReturn401WhenNoTokenIsProvided() throws Exception {
        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenNoTokenIsProvidedOnReservationDetail() throws Exception {
        mockMvc.perform(get("/api/reservations/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "adherent1", roles = {"ADHERENT"})
    void shouldReturn200WhenAdherentListOwnReservations() throws Exception {
        Reservation reservation = new Reservation();
        reservation.setReservationId(1);
        reservation.setUserId(1);

        doReturn(ResponseEntity.ok(Collections.singletonList(reservation)))
                .when(reservationService).getAllReservations(isNull());

        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "adherent1", roles = {"ADHERENT"})
    void shouldReturn403WhenAdherentAccessOtherUserReservation() throws Exception {
        doThrow(new SecurityException("Accès interdit : cette réservation ne vous appartient pas."))
                .when(reservationService).getReservation(99);

        mockMvc.perform(get("/api/reservations/99"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "adherent1", roles = {"ADHERENT"})
    void shouldReturn403WhenAdherentTriesToDeleteReservation() throws Exception {
        mockMvc.perform(delete("/api/reservations/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "biblio1", roles = {"BIBLIOTHECAIRE"})
    void shouldReturn200WhenBibliothecaireAccessAnyReservation() throws Exception {
        Reservation reservation = new Reservation();
        reservation.setReservationId(1);
        reservation.setUserId(2);

        doReturn(reservation).when(reservationService).getReservation(1);

        mockMvc.perform(get("/api/reservations/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "biblio1", roles = {"BIBLIOTHECAIRE"})
    void shouldReturn200WhenBibliothecaireDeletesReservation() throws Exception {
        doReturn(ResponseEntity.noContent().build())
                .when(reservationService).deleteReservation(1);

        mockMvc.perform(delete("/api/reservations/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn401WhenBibliothecaireWithoutTokenTriesToDeleteReservation() throws Exception {
        mockMvc.perform(delete("/api/reservations/1"))
                .andExpect(status().isUnauthorized());
    }
}