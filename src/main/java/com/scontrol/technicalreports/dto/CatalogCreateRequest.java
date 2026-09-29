package com.scontrol.technicalreports.dto;

import com.scontrol.technicalreports.model.CatalogType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear una opción de catálogo.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public record CatalogCreateRequest(
        @NotNull CatalogType type,
        @NotBlank @Size(max = 150) String value) {
}
