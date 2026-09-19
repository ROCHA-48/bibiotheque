package com.ibizabroker.bibliotheque.controller;

import com.ibizabroker.bibliotheque.dao.UsersRepository;
import com.ibizabroker.bibliotheque.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminUserDeletionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    private Integer testUserId;

    @BeforeEach
    void createUser() {
        Users user = new Users();
        user.setUsername("user.a.supprimer");
        user.setName("User À Supprimer");
        user.setPassword("noop");
        testUserId = usersRepository.save(user).getUserId();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"Admin"})
    void shouldReturn204WhenAdminDeletesUser() throws Exception {
        mockMvc.perform(delete("/admin/users/" + testUserId))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn401WhenAnonymousDeletesUser() throws Exception {
        mockMvc.perform(delete("/admin/users/" + testUserId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenAdherentDeletesUser() throws Exception {
        mockMvc.perform(delete("/admin/users/" + testUserId)
                        .with(user("adherent1").roles("ADHERENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn403WhenBibliothecaireDeletesUser() throws Exception {
        mockMvc.perform(delete("/admin/users/" + testUserId)
                        .with(user("biblio1").roles("BIBLIOTHECAIRE")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"Admin"})
    void shouldReturn404WhenAdminDeletesUnknownUser() throws Exception {
        mockMvc.perform(delete("/admin/users/999999"))
                .andExpect(status().isNotFound());
    }
}
