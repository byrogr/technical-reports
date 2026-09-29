package com.scontrol.technicalreports.dto;

import com.scontrol.technicalreports.model.DocumentType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear o editar un cliente.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public record ClientRequest(
        @NotBlank @Size(max = 200) String name,
        DocumentType documentType,
        @Size(max = 11) String documentNumber,
        @Email @Size(max = 254) String contactEmail) {

    // Tipo y número van juntos; el número debe tener 11 dígitos si es RUC y 8 si es DNI
    // Solo validación: no es un campo del JSON
    @Schema(hidden = true)
    @AssertTrue(message = "documentType y documentNumber van juntos: RUC de 11 dígitos o DNI de 8 dígitos")
    public boolean isDocumentValid() {
        if (documentType == null || documentNumber == null) {
            return documentType == null && documentNumber == null;
        }
        return switch (documentType) {
            case RUC -> documentNumber.matches("\\d{11}");
            case DNI -> documentNumber.matches("\\d{8}");
        };
    }
}
