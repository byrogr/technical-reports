package com.scontrol.technicalreports.service;

import org.springframework.stereotype.Service;

import com.scontrol.technicalreports.document.ReportDocument;
import com.scontrol.technicalreports.document.TechnicalReportPdfGenerator;
import com.scontrol.technicalreports.dto.TechnicalReportResponse;

import lombok.RequiredArgsConstructor;

/**
 * Servicio encargado de generar el documento PDF de un informe técnico.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@Service
@RequiredArgsConstructor
public class TechnicalReportDocumentService {

    private final TechnicalReportService technicalReportService;
    private final TechnicalReportPdfGenerator pdfGenerator;

    public ReportDocument generatePdf(Long reportId) {
        TechnicalReportResponse report = technicalReportService.findById(reportId);
        String fileName = "informe-tecnico-" + report.reportNumber() + ".pdf";
        return new ReportDocument(fileName, pdfGenerator.generate(report));
    }
}
