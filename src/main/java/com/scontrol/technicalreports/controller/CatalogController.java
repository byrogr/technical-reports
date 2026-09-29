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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints de catálogos configurables. No hay DELETE: las opciones se desactivan.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@RestController
@RequestMapping("/api/catalogs")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping
    public List<CatalogResponse> findByType(@RequestParam CatalogType type) {
        return catalogService.findByType(type);
    }

    @PostMapping
    public ResponseEntity<CatalogResponse> create(@Valid @RequestBody CatalogCreateRequest request) {
        // Sin cabecera Location: no existe GET /api/catalogs/{id}
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.create(request));
    }

    @PutMapping("/{id}")
    public CatalogResponse update(@PathVariable Long id, @Valid @RequestBody CatalogUpdateRequest request) {
        return catalogService.update(id, request);
    }
}
