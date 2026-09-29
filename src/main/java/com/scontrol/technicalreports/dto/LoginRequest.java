package com.scontrol.technicalreports.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Credenciales enviadas al endpoint de login.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public record LoginRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 72) String password) {
}
