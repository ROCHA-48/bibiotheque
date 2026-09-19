package com.ibizabroker.bibliotheque.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Le catalogue des livres est public : le frontend appelle
 * http://localhost:8080/admin/books SANS slash final (books.service.ts).
 * Régression : la règle permitAll ne couvrait que "/admin/books/" (avec slash),
 * l'URL canonique renvoyait donc un 401 injustifié.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PublicBooksCatalogSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowAnonymousAccessToBooksListWithoutTrailingSlash() throws Exception {
        mockMvc.perform(get("/admin/books"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAnonymousAccessToBooksListWithTrailingSlash() throws Exception {
        mockMvc.perform(get("/admin/books/"))
                .andExpect(status().isOk());
    }
}
