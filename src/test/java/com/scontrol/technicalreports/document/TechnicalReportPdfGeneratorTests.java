package com.scontrol.technicalreports.document;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

import com.scontrol.technicalreports.dto.CatalogResponse;
import com.scontrol.technicalreports.dto.TechnicalReportResponse;
import com.scontrol.technicalreports.model.CatalogType;

/**
 * Tests unitarios del generador de PDF: contenido, campos opcionales vacíos y detalles largos.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
class TechnicalReportPdfGeneratorTests {

    private final TechnicalReportPdfGenerator generator = new TechnicalReportPdfGenerator();

    @Test
    void rendersReportData() throws Exception {
        byte[] pdf = generator.generate(report("Se cambió el receptor FSE777.", LocalDateTime.of(2026, 3, 2, 8, 0),
                "777 - 19 00160 / 777 - 19 00161", "Transmisor Spectrum 2"));

        String text = text(pdf, 1);
        assertThat(text).contains("008-0001", "FERREYROS", "777 SPECTRUM2", "777 - 19 00160 / 777 - 19 00161",
                "02/03/2026 08:00", "02/03/2026 18:00", "Inoperativo", "Operativo", "Sistema Radiocontrol",
                "Transmisor Spectrum 2", "Mantenimiento Correctivo", "Se cambió el receptor FSE777.",
                "Tec. Jorge Antonio Huaman Andia", "Ing. Edson Mejia Cielo.");
    }

    @Test
    void rendersWithoutOptionalFields() throws Exception {
        byte[] pdf = generator.generate(report("Detalle", null, null, null));

        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
        assertThat(text(pdf, 1)).contains("008-0001", "Detalle");
    }

    @Test
    void longDetailsSpanSeveralPages() throws Exception {
        String longDetails = "Línea de detalle del trabajo realizado en el equipo.\n".repeat(200);

        byte[] pdf = generator.generate(report(longDetails, null, null, null));

        try (PdfReader reader = new PdfReader(pdf)) {
            assertThat(reader.getNumberOfPages()).isGreaterThan(1);
        }
    }

    private static TechnicalReportResponse report(String details, LocalDateTime start, String serial,
                                                  String component) {
        return new TechnicalReportResponse(1L, "008-0001", 1L, "FERREYROS", 1L, "777 SPECTRUM2", serial,
                start, LocalDateTime.of(2026, 3, 2, 18, 0),
                option(CatalogType.STATUS, "Inoperativo"), option(CatalogType.STATUS, "Operativo"),
                option(CatalogType.EVENT_FAILURE, "Sistema Radiocontrol"), component,
                option(CatalogType.ACTION, "Mantenimiento Correctivo"), details,
                option(CatalogType.PERSONNEL, "Tec. Jorge Antonio Huaman Andia"),
                option(CatalogType.SUPERVISOR, "Ing. Edson Mejia Cielo."),
                LocalDateTime.now(), "admin@scontrol.test");
    }

    private static CatalogResponse option(CatalogType type, String value) {
        return new CatalogResponse(1L, type, value, true);
    }

    private static String text(byte[] pdf, int page) throws Exception {
        try (PdfReader reader = new PdfReader(pdf)) {
            return new PdfTextExtractor(reader).getTextFromPage(page);
        }
    }
}
