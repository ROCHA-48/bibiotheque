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
    @WithMockUser(username = "admin", roles = {"Admin"})
    void shouldReturn200WhenAdminListsAllReservations() throws Exception {
        doReturn(ResponseEntity.ok(Collections.emptyList()))
                .when(reservationService).getAllReservations(isNull());

        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"Admin"})
    void shouldReturn204WhenAdminDeletesReservation() throws Exception {
        doReturn(ResponseEntity.noContent().build())
                .when(reservationService).deleteReservation(1);

        mockMvc.perform(delete("/api/reservations/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"Admin"})
    void shouldReturn200WhenAdminReadsAnyReservation() throws Exception {
        Reservation reservation = new Reservation();
        reservation.setReservationId(9106);
        reservation.setUserId(5);

        doReturn(reservation).when(reservationService).getReservation(9106);

        mockMvc.perform(get("/api/reservations/9106"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn401WhenBibliothecaireWithoutTokenTriesToDeleteReservation() throws Exception {
        mockMvc.perform(delete("/api/reservations/1"))
                .andExpect(status().isUnauthorized());
    }

    // --- RS-01 : anonyme refusé sur TOUS les endpoints ---

    @Test
    void shouldReturn401WhenAnonymousCreatesReservation_RS01() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .contentType("application/json")
                        .content("{\"bookId\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenAnonymousCancelsReservation_RS01() throws Exception {
        mockMvc.perform(patch("/api/reservations/1/annuler"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenAnonymousListsUsersForReservations_RS01() throws Exception {
        mockMvc.perform(get("/api/reservations/users"))
                .andExpect(status().isUnauthorized());
    }

    // --- RS-02 : l'adhérent ne peut pas agir comme un bibliothécaire ---

    @Test
    @WithMockUser(username = "adherent1", roles = {"ADHERENT"})
    void shouldReturn403WhenAdherentDeletesReservation_RS02() throws Exception {
        mockMvc.perform(delete("/api/reservations/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "adherent1", roles = {"ADHERENT"})
    void shouldReturn403WhenAdherentListsUsersForReservations_RS02() throws Exception {
        mockMvc.perform(get("/api/reservations/users"))
                .andExpect(status().isForbidden());
    }

    // --- Les rôles legacy « User » / « Admin » gardent l'accès (compatibilité) ---

    @Test
    @WithMockUser(username = "classic", roles = {"User"})
    void shouldReturn200WhenLegacyUserListsReservations() throws Exception {
        doReturn(ResponseEntity.ok(Collections.emptyList()))
                .when(reservationService).getAllReservations(isNull());

        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "biblio1", roles = {"BIBLIOTHECAIRE"})
    void shouldReturn200WhenBibliothecaireCreatesReservation() throws Exception {
        Reservation reservation = new Reservation();
        reservation.setReservationId(3);
        reservation.setBookId(1);
        reservation.setUserId(2);

        doReturn(ResponseEntity.status(201).body(reservation))
                .when(reservationService).createReservation(any(Reservation.class));

        mockMvc.perform(post("/api/reservations")
                        .contentType("application/json")
                        .content("{\"bookId\":1,\"userId\":2}"))
                .andExpect(status().isCreated());
    }
}