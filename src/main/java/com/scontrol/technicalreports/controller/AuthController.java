package com.scontrol.technicalreports.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scontrol.technicalreports.dto.LoginRequest;
import com.scontrol.technicalreports.dto.LoginResponse;
import com.scontrol.technicalreports.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints de autenticación.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@RestController
@Tag(name = "Auth", description = "Autenticación con JWT")
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Iniciar sesión", description = "Devuelve un JWT válido por 10 horas")
    @SecurityRequirements
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
