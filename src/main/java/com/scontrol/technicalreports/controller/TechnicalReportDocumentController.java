package com.scontrol.technicalreports.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.scontrol.technicalreports.document.ReportDocument;
import com.scontrol.technicalreports.service.TechnicalReportDocumentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

/**
 * Endpoint de descarga del informe técnico en PDF.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@RestController
@Tag(name = "Technical Reports", description = "Informes técnicos de mantenimiento")
@RequiredArgsConstructor
public class TechnicalReportDocumentController {

    private final TechnicalReportDocumentService documentService;

    @Operation(summary = "Descargar el informe en PDF")
    @GetMapping(value = "/api/technical-reports/{id}/document", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        ReportDocument document = documentService.generatePdf(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(document.fileName()).build().toString())
                .body(document.content());
    }
}
