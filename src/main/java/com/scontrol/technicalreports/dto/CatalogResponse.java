package com.scontrol.technicalreports.dto;

import com.scontrol.technicalreports.model.CatalogType;

/**
 * Datos de una opción de catálogo devueltos por la API.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public record CatalogResponse(
        Long id,
        CatalogType type,
        String value,
        boolean active) {
}
