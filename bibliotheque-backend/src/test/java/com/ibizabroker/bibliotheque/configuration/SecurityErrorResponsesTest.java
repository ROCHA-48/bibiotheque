package com.ibizabroker.bibliotheque.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityErrorResponsesTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnJson401WithMessageRequiringTokenWhenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentification requise. Veuillez fournir un token valide."))
                .andExpect(jsonPath("$.reason").value("MISSING"));
    }

    @Test
    void shouldReturnJson401WithExpiredSessionMessageWhenTokenIsGarbage() throws Exception {
        // Un token illisible est traité comme invalide ; un vrai token expiré donnerait EXPIRED.
        MvcResult result = mockMvc.perform(get("/api/reservations")
                        .header("Authorization", "Bearer token.invalide.signature"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.reason").value("INVALID"))
                .andExpect(jsonPath("$.message").value("Token invalide. Authentification refusée."))
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains("Token invalide"));
    }

    @Test
    void shouldReturnJson403WithForbiddenMessageWhenAdherentDeletesReservation() throws Exception {
        mockMvc.perform(delete("/api/reservations/1")
                        .with(user("adherent1").roles("ADHERENT")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Accès interdit : vous n'avez pas les droits nécessaires pour cette action."));
    }
}
