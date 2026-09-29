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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints CRUD de equipos: la colección cuelga del cliente y el detalle es plano.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping("/clients/{clientId}/equipment")
    public List<EquipmentResponse> findByClient(@PathVariable Long clientId) {
        return equipmentService.findByClient(clientId);
    }

    @PostMapping("/clients/{clientId}/equipment")
    public ResponseEntity<EquipmentResponse> create(@PathVariable Long clientId,
                                                    @Valid @RequestBody EquipmentRequest request) {
        EquipmentResponse created = equipmentService.create(clientId, request);
        return ResponseEntity.created(URI.create("/api/equipment/" + created.id())).body(created);
    }

    @GetMapping("/equipment/{id}")
    public EquipmentResponse findById(@PathVariable Long id) {
        return equipmentService.findById(id);
    }

    @PutMapping("/equipment/{id}")
    public EquipmentResponse update(@PathVariable Long id, @Valid @RequestBody EquipmentRequest request) {
        return equipmentService.update(id, request);
    }

    @DeleteMapping("/equipment/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        equipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
