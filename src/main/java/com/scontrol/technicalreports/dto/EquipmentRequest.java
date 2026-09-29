package com.scontrol.technicalreports.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear o editar un equipo. El cliente se toma de la ruta al crear.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public record EquipmentRequest(
        @NotBlank @Size(max = 100) String model,
        @Size(max = 255) String serialNumber) {
}
