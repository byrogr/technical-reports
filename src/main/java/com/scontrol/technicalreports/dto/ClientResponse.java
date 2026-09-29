package com.scontrol.technicalreports.dto;

import com.scontrol.technicalreports.model.DocumentType;

/**
 * Datos de un cliente devueltos por la API.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public record ClientResponse(
        Long id,
        String name,
        DocumentType documentType,
        String documentNumber,
        String contactEmail) {
}
