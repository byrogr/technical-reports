package com.scontrol.technicalreports.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear o editar un informe técnico. Los catálogos se referencian por id.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public record TechnicalReportRequest(
        @NotNull Long equipmentId,
        LocalDateTime startDatetime,
        @NotNull LocalDateTime endDatetime,
        @NotNull Long initialStatusId,
        @NotNull Long finalStatusId,
        @NotNull Long eventFailureId,
        @Size(max = 255) String affectedComponent,
        @NotNull Long actionId,
        @NotBlank String details,
        @NotNull Long personnelId,
        @NotNull Long supervisorId) {

    @AssertTrue(message = "endDatetime no puede ser anterior a startDatetime")
    public boolean isDateRangeValid() {
        return startDatetime == null || endDatetime == null || !endDatetime.isBefore(startDatetime);
    }
}
