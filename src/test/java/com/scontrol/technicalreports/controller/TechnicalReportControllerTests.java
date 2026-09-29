package com.scontrol.technicalreports.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;
import com.scontrol.technicalreports.TestcontainersConfiguration;

/**
 * Tests de integración del CRUD de informes técnicos y de su numeración correlativa.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class TechnicalReportControllerTests {

    private static final RequestPostProcessor ADMIN = jwt().jwt(token -> token.subject("admin@scontrol.test"));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private long clientId;
    private long equipmentId;
    private long operativo;
    private long inoperativo;
    private long radiocontrol;
    private long correctivo;
    private long technician;
    private long supervisor;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("UPDATE report_counters SET last_number = 0");
        clientId = jdbcTemplate.queryForObject(
                "INSERT INTO clients (name) VALUES ('FERREYROS') RETURNING id", Long.class);
        equipmentId = insertEquipment(clientId, "777 SPECTRUM2");
        operativo = catalog("STATUS", "Operativo");
        inoperativo = catalog("STATUS", "Inoperativo");
        radiocontrol = catalog("EVENT_FAILURE", "Sistema Radiocontrol");
        correctivo = catalog("ACTION", "Mantenimiento Correctivo");
        technician = catalog("PERSONNEL", "Tec. Jorge Antonio Huaman Andia");
        supervisor = catalog("SUPERVISOR", "Ing. Edson Mejia Cielo.");
    }

    @Test
    void createsReportWithFirstNumberAndResolvedData() throws Exception {
        createReport(validReport(equipmentId))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/technical-reports/")))
                .andExpect(jsonPath("$.reportNumber").value("008-0001"))
                .andExpect(jsonPath("$.clientName").value("FERREYROS"))
                .andExpect(jsonPath("$.equipmentModel").value("777 SPECTRUM2"))
                .andExpect(jsonPath("$.initialStatus.value").value("Inoperativo"))
                .andExpect(jsonPath("$.finalStatus.value").value("Operativo"))
                .andExpect(jsonPath("$.eventFailure.value").value("Sistema Radiocontrol"))
                .andExpect(jsonPath("$.affectedComponent").value("Transmisor Spectrum 2 / receptor FSE777"))
                .andExpect(jsonPath("$.personnel.value").value("Tec. Jorge Antonio Huaman Andia"))
                .andExpect(jsonPath("$.createdBy").value("admin@scontrol.test"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void numbersAreConsecutive() throws Exception {
        createReport(validReport(equipmentId)).andExpect(jsonPath("$.reportNumber").value("008-0001"));
        createReport(validReport(equipmentId)).andExpect(jsonPath("$.reportNumber").value("008-0002"));
        createReport(validReport(equipmentId)).andExpect(jsonPath("$.reportNumber").value("008-0003"));
    }

    @Test
    void numberKeepsGrowingAfter9999() throws Exception {
        jdbcTemplate.update("UPDATE report_counters SET last_number = 9999");
        createReport(validReport(equipmentId)).andExpect(jsonPath("$.reportNumber").value("008-10000"));
    }

    @Test
    void rejectedReportDoesNotConsumeNumber() throws Exception {
        createReport(reportWith(equipmentId, correctivo /* ACTION en lugar de STATUS */, operativo))
                .andExpect(status().isBadRequest());
        createReport(validReport(equipmentId)).andExpect(jsonPath("$.reportNumber").value("008-0001"));
    }

    @Test
    void rejectsCatalogOptionOfWrongType() throws Exception {
        createReport(reportWith(equipmentId, correctivo, operativo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(startsWith("La opción " + correctivo + " es de tipo ACTION")));
    }

    @Test
    void rejectsInactiveCatalogOption() throws Exception {
        jdbcTemplate.update("UPDATE catalogs SET active = FALSE WHERE id = ?", inoperativo);
        createReport(validReport(equipmentId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("La opción 'Inoperativo' está desactivada"));
    }

    @Test
    void rejectsUnknownEquipmentAndCatalog() throws Exception {
        createReport(validReport(999999)).andExpect(status().isBadRequest());
        createReport(reportWith(equipmentId, 999999, operativo)).andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMissingFieldsAndInvertedDates() throws Exception {
        createReport("""
                {"equipmentId": %d, "details": " "}
                """.formatted(equipmentId))
                .andExpect(status().isBadRequest());
        createReport(validReport(equipmentId).replace("2026-03-02T08:00:00", "2026-03-03T08:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void acceptsReportWithoutStartDatetime() throws Exception {
        createReport(validReport(equipmentId).replace("\"2026-03-02T08:00:00\"", "null"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startDatetime").isEmpty());
    }

    @Test
    void updatesReportKeepingNumberAndAuthor() throws Exception {
        long id = idOf(createReport(validReport(equipmentId)));
        String updated = validReport(equipmentId).replace("Se revisó el sistema", "Se cambió el receptor");

        mockMvc.perform(put("/api/technical-reports/{id}", id).with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updated))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportNumber").value("008-0001"))
                .andExpect(jsonPath("$.details").value("Se cambió el receptor"))
                .andExpect(jsonPath("$.createdBy").value("admin@scontrol.test"));
    }

    @Test
    void updateKeepsOptionDeactivatedAfterCreation() throws Exception {
        long id = idOf(createReport(validReport(equipmentId)));
        jdbcTemplate.update("UPDATE catalogs SET active = FALSE WHERE id = ?", inoperativo);

        mockMvc.perform(put("/api/technical-reports/{id}", id).with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validReport(equipmentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.initialStatus.value").value("Inoperativo"));
    }

    @Test
    void filtersByClientEquipmentAndDates() throws Exception {
        long otherClient = jdbcTemplate.queryForObject(
                "INSERT INTO clients (name) VALUES ('ANTAMINA') RETURNING id", Long.class);
        long otherEquipment = insertEquipment(otherClient, "930E");
        createReport(validReport(equipmentId));                                              // 008-0001, 2026-03-02
        createReport(validReport(equipmentId).replace("2026-03-02T18:00:00", "2026-04-10T18:00:00")); // 008-0002
        createReport(validReport(otherEquipment));                                           // 008-0003

        mockMvc.perform(get("/api/technical-reports").param("clientId", String.valueOf(clientId)).with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].reportNumber", contains("008-0002", "008-0001")));

        mockMvc.perform(get("/api/technical-reports").param("equipmentId", String.valueOf(otherEquipment)).with(ADMIN))
                .andExpect(jsonPath("$[*].reportNumber", contains("008-0003")));

        mockMvc.perform(get("/api/technical-reports")
                        .param("clientId", String.valueOf(clientId))
                        .param("from", "2026-03-02")
                        .param("to", "2026-03-02")
                        .with(ADMIN))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].reportNumber").value("008-0001"));
    }

    @Test
    void returns404ForUnknownReport() throws Exception {
        mockMvc.perform(get("/api/technical-reports/{id}", 999999).with(ADMIN))
                .andExpect(status().isNotFound());
    }

    private String validReport(long equipment) {
        return reportWith(equipment, inoperativo, operativo);
    }

    private String reportWith(long equipment, long initialStatus, long finalStatus) {
        return """
                {"equipmentId": %d,
                 "startDatetime": "2026-03-02T08:00:00", "endDatetime": "2026-03-02T18:00:00",
                 "initialStatusId": %d, "finalStatusId": %d, "eventFailureId": %d,
                 "affectedComponent": "Transmisor Spectrum 2 / receptor FSE777",
                 "actionId": %d, "details": "Se revisó el sistema", "personnelId": %d, "supervisorId": %d}
                """.formatted(equipment, initialStatus, finalStatus, radiocontrol, correctivo, technician, supervisor);
    }

    private ResultActions createReport(String json) throws Exception {
        return mockMvc.perform(post("/api/technical-reports").with(ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private long insertEquipment(long client, String model) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO equipment (client_id, model) VALUES (?, ?) RETURNING id", Long.class, client, model);
    }

    private long catalog(String type, String value) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM catalogs WHERE type = ? AND value = ?", Long.class, type, value);
    }

    private static long idOf(ResultActions result) throws Exception {
        Number id = JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }
}
