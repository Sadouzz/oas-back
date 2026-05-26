package sn.oas.facturation.piecedetache.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PieceDetacheControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createPdp_returnsOk() throws Exception {
        mockMvc.perform(post("/api/pieces-detachees/create")
                        .with(user("agent@test.sn").roles("AGENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "PDP",
                                  "numeroDeSerie": "SN-PDP-TEST-001",
                                  "reference": "REF-PDP-001",
                                  "categorie": "Freinage",
                                  "pourcentage": 12.5,
                                  "statut": "ACTIF",
                                  "stockMagasin": 50,
                                  "prix": 2500.0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroDeSerie").value("SN-PDP-TEST-001"))
                .andExpect(jsonPath("$.qteReelle").value(50))
                .andExpect(jsonPath("$.stockAtelier").value(0))
                .andExpect(jsonPath("$.type").value("PDP"));
    }

    @Test
    void listFilterByType_returnsOnlyMatchingType() throws Exception {
        createPdgViaApi("SN-PDG-FILTER", "REF-F1", "Moteur");
        createPdgViaApi("SN-PDS-FILTER", "REF-F2", "Carrosserie");

        mockMvc.perform(get("/api/pieces-detachees")
                        .with(user("agent@test.sn").roles("AGENT"))
                        .param("type", "PDG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].type", everyItem(is("PDG"))));
    }

    @Test
    void searchByKeyword_findsByCategorie() throws Exception {
        createPdgViaApi("SN-SEARCH-001", "REF-S1", "Transmission");

        mockMvc.perform(get("/api/pieces-detachees")
                        .with(user("agent@test.sn").roles("AGENT"))
                        .param("keyword", "transmis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].categorie", hasItem(containsStringIgnoringCase("transmis"))));
    }

    @Test
    void createWithoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/pieces-detachees/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "PDG",
                                  "numeroDeSerie": "SN-NO-AUTH",
                                  "reference": "REF-NA",
                                  "categorie": "Test",
                                  "pourcentage": 1.0,
                                  "statut": "ACTIF"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    private void createPdgViaApi(String numeroSerie, String reference, String categorie) throws Exception {
        mockMvc.perform(post("/api/pieces-detachees/create")
                        .with(user("agent@test.sn").roles("AGENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "PDG",
                                  "numeroDeSerie": "%s",
                                  "reference": "%s",
                                  "categorie": "%s",
                                  "pourcentage": 8.0,
                                  "statut": "ACTIF"
                                }
                                """.formatted(numeroSerie, reference, categorie)))
                .andExpect(status().isOk());
    }
}
