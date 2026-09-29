package com.scontrol.technicalreports.dto;

/**
 * Respuesta del login con el JWT de acceso y su duración en segundos.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}
