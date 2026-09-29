package com.scontrol.technicalreports.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para editar o desactivar una opción de catálogo. El tipo no se puede cambiar.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public record CatalogUpdateRequest(
        @NotBlank @Size(max = 150) String value,
        @NotNull Boolean active) {
}
