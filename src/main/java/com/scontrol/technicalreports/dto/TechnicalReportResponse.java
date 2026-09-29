package com.scontrol.technicalreports.dto;

import java.time.LocalDateTime;

/**
 * Informe técnico devuelto por la API, con cliente, equipo y catálogos ya resueltos.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public record TechnicalReportResponse(
        Long id,
        String reportNumber,
        Long clientId,
        String clientName,
        Long equipmentId,
        String equipmentModel,
        String equipmentSerialNumber,
        LocalDateTime startDatetime,
        LocalDateTime endDatetime,
        CatalogResponse initialStatus,
        CatalogResponse finalStatus,
        CatalogResponse eventFailure,
        String affectedComponent,
        CatalogResponse action,
        String details,
        CatalogResponse personnel,
        CatalogResponse supervisor,
        LocalDateTime createdAt,
        String createdBy) {
}
