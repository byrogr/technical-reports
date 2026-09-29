package com.scontrol.technicalreports.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;
import com.scontrol.technicalreports.TestcontainersConfiguration;

/**
 * Tests de integración de los catálogos configurables.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class CatalogControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsSeededOptionsByType() throws Exception {
        mockMvc.perform(get("/api/catalogs").param("type", "STATUS").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].value", contains("En Obs.", "Inoperativo", "Operativo")));
    }

    @Test
    void requiresValidType() throws Exception {
        mockMvc.perform(get("/api/catalogs").with(jwt()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/catalogs").param("type", "UNKNOWN").with(jwt()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createsOption() throws Exception {
        createOption("""
                {"type": "ACTION", "value": "Calibración"}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("ACTION"))
                .andExpect(jsonPath("$.value").value("Calibración"))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(get("/api/catalogs").param("type", "ACTION").with(jwt()))
                .andExpect(jsonPath("$[*].value", hasItem("Calibración")));
    }

    @Test
    void rejectsDuplicateOptionInSameType() throws Exception {
        createOption("""
                {"type": "STATUS", "value": "Operativo"}
                """)
                .andExpect(status().isConflict());
    }

    @Test
    void allowsSameValueInDifferentType() throws Exception {
        createOption("""
                {"type": "SUPERVISOR", "value": "Tec. Jorge Antonio Huaman Andia"}
                """)
                .andExpect(status().isCreated());
    }

    @Test
    void rejectsMissingType() throws Exception {
        createOption("""
                {"value": "Calibración"}
                """)
                .andExpect(status().isBadRequest());
    }

    @Test
    void editsAndDeactivatesOption() throws Exception {
        String body = createOption("""
                {"type": "ACTION", "value": "Calibracion"}
                """).andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(body, "$.id")).longValue();

        mockMvc.perform(put("/api/catalogs/{id}", id).with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"value": "Calibración", "active": false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("Calibración"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void rejectsEditToDuplicateValue() throws Exception {
        String body = createOption("""
                {"type": "STATUS", "value": "Detenido"}
                """).andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(body, "$.id")).longValue();

        mockMvc.perform(put("/api/catalogs/{id}", id).with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"value": "Operativo", "active": true}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void returns404WhenEditingUnknownOption() throws Exception {
        mockMvc.perform(put("/api/catalogs/{id}", 999999).with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"value": "x", "active": true}
                                """))
                .andExpect(status().isNotFound());
    }

    private ResultActions createOption(String json) throws Exception {
        return mockMvc.perform(post("/api/catalogs").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }
}
