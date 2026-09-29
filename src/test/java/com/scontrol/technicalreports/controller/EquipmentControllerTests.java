package com.scontrol.technicalreports.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;
import com.scontrol.technicalreports.TestcontainersConfiguration;

/**
 * Tests de integración del CRUD de equipos (colección anidada bajo el cliente).
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class EquipmentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private long clientId;

    @BeforeEach
    void createClient() {
        clientId = jdbcTemplate.queryForObject(
                "INSERT INTO clients (name) VALUES ('FERREYROS') RETURNING id", Long.class);
    }

    @Test
    void createsEquipmentForClient() throws Exception {
        long id = idOf(createEquipment(clientId, """
                {"model": "777 SPECTRUM2", "serialNumber": "777 - 19 00160 / 777 - 19 00161"}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value(clientId))
                .andExpect(jsonPath("$.clientName").value("FERREYROS"))
                .andExpect(jsonPath("$.serialNumber").value("777 - 19 00160 / 777 - 19 00161")));

        mockMvc.perform(get("/api/equipment/{id}", id).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("777 SPECTRUM2"));
    }

    @Test
    void locationPointsToFlatEquipmentRoute() throws Exception {
        long id = idOf(createEquipment(clientId, """
                {"model": "777 SPECTRUM2"}
                """)
                .andExpect(header().string("Location", startsWith("/api/equipment/"))));
        mockMvc.perform(get("/api/equipment/{id}", id).with(jwt()))
                .andExpect(status().isOk());
    }

    @Test
    void listsOnlyEquipmentOfTheClient() throws Exception {
        long otherClientId = jdbcTemplate.queryForObject(
                "INSERT INTO clients (name) VALUES ('ANTAMINA') RETURNING id", Long.class);
        createEquipment(clientId, """
                {"model": "777 SPECTRUM2"}
                """);
        createEquipment(clientId, """
                {"model": "785"}
                """);
        createEquipment(otherClientId, """
                {"model": "930E"}
                """);

        mockMvc.perform(get("/api/clients/{clientId}/equipment", clientId).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].model").value("777 SPECTRUM2"))
                .andExpect(jsonPath("$[1].model").value("785"));
    }

    @Test
    void returns404WhenClientDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/clients/{clientId}/equipment", 999999).with(jwt()))
                .andExpect(status().isNotFound());
        createEquipment(999999, """
                {"model": "777 SPECTRUM2"}
                """)
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsBlankModel() throws Exception {
        createEquipment(clientId, """
                {"model": ""}
                """)
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatesAndDeletesEquipment() throws Exception {
        long id = idOf(createEquipment(clientId, """
                {"model": "777 SPECTRUM2"}
                """));

        mockMvc.perform(put("/api/equipment/{id}", id).with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"model": "777G", "serialNumber": "ABC-123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("777G"))
                .andExpect(jsonPath("$.serialNumber").value("ABC-123"));

        mockMvc.perform(delete("/api/equipment/{id}", id).with(jwt()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/equipment/{id}", id).with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void cannotDeleteEquipmentWithReports() throws Exception {
        long id = idOf(createEquipment(clientId, """
                {"model": "777 SPECTRUM2"}
                """));
        insertReportFor(id);

        mockMvc.perform(delete("/api/equipment/{id}", id).with(jwt()))
                .andExpect(status().isConflict());
    }

    private void insertReportFor(long equipmentId) {
        // Informe mínimo directo en BD: el CRUD de informes llega en la Fase 3
        jdbcTemplate.update("""
                INSERT INTO technical_reports (report_number, equipment_id, end_datetime, initial_status_id,
                    final_status_id, event_failure_id, action_id, details, personnel_id, supervisor_id, created_by)
                SELECT 'TEST-0001', ?, NOW(), s.id, s.id, e.id, a.id, 'detalle', p.id, r.id, u.id
                FROM (SELECT id FROM catalogs WHERE type = 'STATUS' LIMIT 1) s,
                     (SELECT id FROM catalogs WHERE type = 'EVENT_FAILURE' LIMIT 1) e,
                     (SELECT id FROM catalogs WHERE type = 'ACTION' LIMIT 1) a,
                     (SELECT id FROM catalogs WHERE type = 'PERSONNEL' LIMIT 1) p,
                     (SELECT id FROM catalogs WHERE type = 'SUPERVISOR' LIMIT 1) r,
                     (SELECT id FROM users LIMIT 1) u
                """, equipmentId);
    }

    private ResultActions createEquipment(long clientId, String json) throws Exception {
        return mockMvc.perform(post("/api/clients/{clientId}/equipment", clientId).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private static long idOf(ResultActions result) throws Exception {
        Number id = JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }
}
