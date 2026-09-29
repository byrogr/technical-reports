package com.scontrol.technicalreports.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;
import com.scontrol.technicalreports.TestcontainersConfiguration;

/**
 * Tests de integración de la descarga del informe técnico en PDF.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class TechnicalReportDocumentControllerTests {

    private static final RequestPostProcessor ADMIN = jwt().jwt(token -> token.subject("admin@scontrol.test"));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void downloadsReportAsPdf() throws Exception {
        long reportId = createReport();

        byte[] pdf = mockMvc.perform(get("/api/technical-reports/{id}/document", reportId).with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"informe-tecnico-008-0001.pdf\""))
                .andReturn().getResponse().getContentAsByteArray();

        try (PdfReader reader = new PdfReader(pdf)) {
            String text = new PdfTextExtractor(reader).getTextFromPage(1);
            assertThat(text).contains("008-0001", "FERREYROS", "777 SPECTRUM2", "Sistema Radiocontrol",
                    "Se revisó el sistema");
        }
    }

    @Test
    void returns404ForUnknownReport() throws Exception {
        mockMvc.perform(get("/api/technical-reports/{id}/document", 999999).with(ADMIN))
                .andExpect(status().isNotFound());
    }

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/technical-reports/{id}/document", 1))
                .andExpect(status().isUnauthorized());
    }

    private long createReport() throws Exception {
        jdbcTemplate.update("UPDATE report_counters SET last_number = 0");
        long clientId = jdbcTemplate.queryForObject(
                "INSERT INTO clients (name) VALUES ('FERREYROS') RETURNING id", Long.class);
        long equipmentId = jdbcTemplate.queryForObject(
                "INSERT INTO equipment (client_id, model) VALUES (?, '777 SPECTRUM2') RETURNING id",
                Long.class, clientId);
        String body = """
                {"equipmentId": %d, "endDatetime": "2026-03-02T18:00:00",
                 "initialStatusId": %d, "finalStatusId": %d, "eventFailureId": %d, "actionId": %d,
                 "details": "Se revisó el sistema", "personnelId": %d, "supervisorId": %d}
                """.formatted(equipmentId, catalog("STATUS", "Inoperativo"), catalog("STATUS", "Operativo"),
                catalog("EVENT_FAILURE", "Sistema Radiocontrol"), catalog("ACTION", "Mantenimiento Correctivo"),
                catalog("PERSONNEL", "Tec. Jorge Antonio Huaman Andia"),
                catalog("SUPERVISOR", "Ing. Edson Mejia Cielo."));

        String response = mockMvc.perform(post("/api/technical-reports").with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private long catalog(String type, String value) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM catalogs WHERE type = ? AND value = ?", Long.class, type, value);
    }
}
