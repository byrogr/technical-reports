package com.scontrol.technicalreports.dto;

/**
 * Datos de un equipo devueltos por la API, con el cliente al que pertenece.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public record EquipmentResponse(
        Long id,
        Long clientId,
        String clientName,
        String model,
        String serialNumber) {
}
