package com.scontrol.technicalreports.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.scontrol.technicalreports.TestcontainersConfiguration;
import com.scontrol.technicalreports.dto.TechnicalReportRequest;

/**
 * Verifica que la numeración no genera duplicados ni saltos con creaciones simultáneas.
 * No es transaccional: cada informe se confirma en su propia transacción, como en producción.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class ReportNumberConcurrencyTests {

    private static final int REPORTS = 10;

    @Autowired
    private TechnicalReportService technicalReportService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private int initialCounter;
    private long clientId;
    private long equipmentId;

    @BeforeEach
    void setUp() {
        initialCounter = jdbcTemplate.queryForObject("SELECT last_number FROM report_counters", Integer.class);
        clientId = jdbcTemplate.queryForObject(
                "INSERT INTO clients (name) VALUES ('CONCURRENCIA') RETURNING id", Long.class);
        equipmentId = jdbcTemplate.queryForObject(
                "INSERT INTO equipment (client_id, model) VALUES (?, 'TEST') RETURNING id", Long.class, clientId);
    }

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM technical_reports WHERE equipment_id = ?", equipmentId);
        jdbcTemplate.update("DELETE FROM equipment WHERE id = ?", equipmentId);
        jdbcTemplate.update("DELETE FROM clients WHERE id = ?", clientId);
        jdbcTemplate.update("UPDATE report_counters SET last_number = ?", initialCounter);
    }

    @Test
    void concurrentCreationsGetConsecutiveUniqueNumbers() throws Exception {
        TechnicalReportRequest request = request();
        List<Callable<String>> tasks = new ArrayList<>();
        for (int i = 0; i < REPORTS; i++) {
            tasks.add(() -> technicalReportService.create(request, "admin@scontrol.test").reportNumber());
        }

        List<String> numbers = new ArrayList<>();
        try (ExecutorService executor = Executors.newFixedThreadPool(REPORTS)) {
            for (Future<String> future : executor.invokeAll(tasks)) {
                numbers.add(future.get());
            }
        }

        List<String> expected = IntStream.rangeClosed(initialCounter + 1, initialCounter + REPORTS)
                .mapToObj(n -> "008-%04d".formatted(n))
                .toList();
        assertThat(numbers).containsExactlyInAnyOrderElementsOf(expected);
    }

    private TechnicalReportRequest request() {
        return new TechnicalReportRequest(equipmentId, null, LocalDateTime.of(2026, 3, 2, 18, 0),
                catalog("STATUS", "Inoperativo"), catalog("STATUS", "Operativo"),
                catalog("EVENT_FAILURE", "Sistema Radiocontrol"), null,
                catalog("ACTION", "Mantenimiento Correctivo"), "Prueba de concurrencia",
                catalog("PERSONNEL", "Tec. Jorge Antonio Huaman Andia"),
                catalog("SUPERVISOR", "Ing. Edson Mejia Cielo."));
    }

    private long catalog(String type, String value) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM catalogs WHERE type = ? AND value = ?", Long.class, type, value);
    }
}
