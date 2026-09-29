package com.scontrol.technicalreports.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scontrol.technicalreports.dto.TechnicalReportRequest;
import com.scontrol.technicalreports.dto.TechnicalReportResponse;
import com.scontrol.technicalreports.service.TechnicalReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints de informes técnicos. No hay DELETE: los informes emitidos se conservan.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@RestController
@Tag(name = "Technical Reports", description = "Informes técnicos de mantenimiento")
@RequestMapping("/api/technical-reports")
@RequiredArgsConstructor
public class TechnicalReportController {

    private final TechnicalReportService technicalReportService;

    @Operation(summary = "Listar informes", description = "Más recientes primero; filtros opcionales")
    @GetMapping
    public List<TechnicalReportResponse> findAll(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return technicalReportService.findAll(clientId, equipmentId, from, to);
    }

    @Operation(summary = "Obtener un informe")
    @GetMapping("/{id}")
    public TechnicalReportResponse findById(@PathVariable Long id) {
        return technicalReportService.findById(id);
    }

    @Operation(summary = "Crear un informe", description = "Asigna el número correlativo automáticamente")
    @ApiResponse(responseCode = "201", description = "Creado")
    @PostMapping
    public ResponseEntity<TechnicalReportResponse> create(@Valid @RequestBody TechnicalReportRequest request,
                                                          @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        // El subject del JWT es el email del usuario autenticado
        TechnicalReportResponse created = technicalReportService.create(request, jwt.getSubject());
        return ResponseEntity.created(URI.create("/api/technical-reports/" + created.id())).body(created);
    }

    @Operation(summary = "Editar un informe", description = "No cambian el número ni el autor")
    @PutMapping("/{id}")
    public TechnicalReportResponse update(@PathVariable Long id, @Valid @RequestBody TechnicalReportRequest request) {
        return technicalReportService.update(id, request);
    }
}
