package com.scontrol.technicalreports.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.scontrol.technicalreports.TestcontainersConfiguration;

/**
 * Verifica las restricciones de BD definidas en la migración inicial (DESIGN.md, sección 5).
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class SchemaMigrationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void reportCounterIsSeededWithSeries008() {
        Integer lastNumber = jdbcTemplate.queryForObject(
                "SELECT last_number FROM report_counters WHERE series = '008'", Integer.class);
        assertThat(lastNumber).isZero();
    }

    @Test
    void clientAcceptsValidRucDniOrNoDocument() {
        insertClient("EMPRESA", "RUC", "20100047218");
        insertClient("PERSONA", "DNI", "12345678");
        insertClient("SIN DOCUMENTO", null, null);
        insertClient("OTRO SIN DOCUMENTO", null, null);
    }

    @Test
    void clientRejectsRucWithWrongLength() {
        assertThatThrownBy(() -> insertClient("EMPRESA", "RUC", "12345678"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void clientRejectsDocumentNumberWithoutType() {
        assertThatThrownBy(() -> insertClient("EMPRESA", null, "12345678"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void catalogRejectsUnknownType() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO catalogs (type, value) VALUES ('UNKNOWN', 'x')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void insertClient(String name, String documentType, String documentNumber) {
        jdbcTemplate.update("INSERT INTO clients (name, document_type, document_number) VALUES (?, ?, ?)",
                name, documentType, documentNumber);
    }
}
