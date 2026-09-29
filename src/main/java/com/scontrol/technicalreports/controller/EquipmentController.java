package com.scontrol.technicalreports.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scontrol.technicalreports.dto.EquipmentRequest;
import com.scontrol.technicalreports.dto.EquipmentResponse;
import com.scontrol.technicalreports.service.EquipmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints CRUD de equipos: la colección cuelga del cliente y el detalle es plano.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@RestController
@Tag(name = "Equipment", description = "Equipos de cada cliente")
@RequestMapping("/api")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @Operation(summary = "Listar los equipos de un cliente")
    @GetMapping("/clients/{clientId}/equipment")
    public List<EquipmentResponse> findByClient(@PathVariable Long clientId) {
        return equipmentService.findByClient(clientId);
    }

    @Operation(summary = "Crear un equipo para un cliente")
    @ApiResponse(responseCode = "201", description = "Creado")
    @PostMapping("/clients/{clientId}/equipment")
    public ResponseEntity<EquipmentResponse> create(@PathVariable Long clientId,
                                                    @Valid @RequestBody EquipmentRequest request) {
        EquipmentResponse created = equipmentService.create(clientId, request);
        return ResponseEntity.created(URI.create("/api/equipment/" + created.id())).body(created);
    }

    @Operation(summary = "Obtener un equipo")
    @GetMapping("/equipment/{id}")
    public EquipmentResponse findById(@PathVariable Long id) {
        return equipmentService.findById(id);
    }

    @Operation(summary = "Editar un equipo")
    @PutMapping("/equipment/{id}")
    public EquipmentResponse update(@PathVariable Long id, @Valid @RequestBody EquipmentRequest request) {
        return equipmentService.update(id, request);
    }

    @Operation(summary = "Eliminar un equipo", description = "409 si el equipo tiene informes")
    @ApiResponse(responseCode = "204", description = "Eliminado")
    @DeleteMapping("/equipment/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        equipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
