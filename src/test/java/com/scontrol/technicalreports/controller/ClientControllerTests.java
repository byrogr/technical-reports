package com.scontrol.technicalreports.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
 * Tests de integración del CRUD de clientes.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class ClientControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsClientWithRuc() throws Exception {
        createClient("""
                {"name": "FERREYROS", "documentType": "RUC", "documentNumber": "20100028698",
                 "contactEmail": "contacto@ferreyros.com.pe"}
                """)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/clients/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("FERREYROS"))
                .andExpect(jsonPath("$.documentType").value("RUC"))
                .andExpect(jsonPath("$.contactEmail").value("contacto@ferreyros.com.pe"));
    }

    @Test
    void createsClientWithoutDocument() throws Exception {
        createClient("""
                {"name": "Juan Pérez"}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentType").isEmpty());
    }

    @Test
    void rejectsDniWithWrongLength() throws Exception {
        createClient("""
                {"name": "Juan Pérez", "documentType": "DNI", "documentNumber": "123"}
                """)
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsDocumentNumberWithoutType() throws Exception {
        createClient("""
                {"name": "Juan Pérez", "documentNumber": "12345678"}
                """)
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsInvalidEmailAndBlankName() throws Exception {
        createClient("""
                {"name": " ", "contactEmail": "no-es-email"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("La petición tiene campos inválidos"))
                .andExpect(jsonPath("$.errors[*].field", contains("contactEmail", "name")));
    }

    @Test
    void rejectsUnknownDocumentType() throws Exception {
        createClient("""
                {"name": "Juan Pérez", "documentType": "PASAPORTE", "documentNumber": "12345678"}
                """)
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsDuplicateDocumentNumber() throws Exception {
        String body = """
                {"name": "FERREYROS", "documentType": "RUC", "documentNumber": "20100028698"}
                """;
        createClient(body).andExpect(status().isCreated());
        createClient(body).andExpect(status().isConflict());
    }

    @Test
    void listsFindsUpdatesAndDeletesClient() throws Exception {
        long id = idOf(createClient("""
                {"name": "ANTAMINA"}
                """));

        mockMvc.perform(get("/api/clients").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("ANTAMINA")));

        mockMvc.perform(put("/api/clients/{id}", id).with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "COMPAÑÍA MINERA ANTAMINA", "documentType": "RUC", "documentNumber": "20330262428"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("COMPAÑÍA MINERA ANTAMINA"));

        mockMvc.perform(get("/api/clients/{id}", id).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentNumber").value("20330262428"));

        mockMvc.perform(delete("/api/clients/{id}", id).with(jwt()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/clients/{id}", id).with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateKeepsOwnDocumentNumber() throws Exception {
        String body = """
                {"name": "FERREYROS", "documentType": "RUC", "documentNumber": "20100028698"}
                """;
        long id = idOf(createClient(body));

        mockMvc.perform(put("/api/clients/{id}", id).with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void cannotDeleteClientWithEquipment() throws Exception {
        long id = idOf(createClient("""
                {"name": "FERREYROS"}
                """));
        mockMvc.perform(post("/api/clients/{id}/equipment", id).with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"model": "777 SPECTRUM2"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/clients/{id}", id).with(jwt()))
                .andExpect(status().isConflict());
    }

    @Test
    void returns404ForUnknownClient() throws Exception {
        mockMvc.perform(get("/api/clients/{id}", 999999).with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions createClient(String json) throws Exception {
        return mockMvc.perform(post("/api/clients").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private static long idOf(ResultActions result) throws Exception {
        Number id = JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }
}
