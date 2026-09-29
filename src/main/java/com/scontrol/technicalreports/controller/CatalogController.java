package com.scontrol.technicalreports.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scontrol.technicalreports.dto.CatalogCreateRequest;
import com.scontrol.technicalreports.dto.CatalogResponse;
import com.scontrol.technicalreports.dto.CatalogUpdateRequest;
import com.scontrol.technicalreports.model.CatalogType;
import com.scontrol.technicalreports.service.CatalogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints de catálogos configurables. No hay DELETE: las opciones se desactivan.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@RestController
@Tag(name = "Catalogs", description = "Opciones configurables (acción, estado, evento/falla, personal, responsable)")
@RequestMapping("/api/catalogs")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @Operation(summary = "Listar opciones por tipo", description = "Incluye activas e inactivas")
    @GetMapping
    public List<CatalogResponse> findByType(@RequestParam CatalogType type) {
        return catalogService.findByType(type);
    }

    @Operation(summary = "Crear una opción")
    @ApiResponse(responseCode = "201", description = "Creado")
    @PostMapping
    public ResponseEntity<CatalogResponse> create(@Valid @RequestBody CatalogCreateRequest request) {
        // Sin cabecera Location: no existe GET /api/catalogs/{id}
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.create(request));
    }

    @Operation(summary = "Editar o desactivar una opción", description = "El tipo no se puede cambiar")
    @PutMapping("/{id}")
    public CatalogResponse update(@PathVariable Long id, @Valid @RequestBody CatalogUpdateRequest request) {
        return catalogService.update(id, request);
    }
}
