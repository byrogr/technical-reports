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

import com.scontrol.technicalreports.dto.ClientRequest;
import com.scontrol.technicalreports.dto.ClientResponse;
import com.scontrol.technicalreports.service.ClientService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints CRUD de clientes.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@RestController
@Tag(name = "Clients", description = "Clientes de la empresa")
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @Operation(summary = "Listar clientes", description = "Ordenados por nombre")
    @GetMapping
    public List<ClientResponse> findAll() {
        return clientService.findAll();
    }

    @Operation(summary = "Obtener un cliente")
    @GetMapping("/{id}")
    public ClientResponse findById(@PathVariable Long id) {
        return clientService.findById(id);
    }

    @Operation(summary = "Crear un cliente")
    @ApiResponse(responseCode = "201", description = "Creado")
    @PostMapping
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest request) {
        ClientResponse created = clientService.create(request);
        return ResponseEntity.created(URI.create("/api/clients/" + created.id())).body(created);
    }

    @Operation(summary = "Editar un cliente")
    @PutMapping("/{id}")
    public ClientResponse update(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return clientService.update(id, request);
    }

    @Operation(summary = "Eliminar un cliente", description = "409 si el cliente tiene equipos")
    @ApiResponse(responseCode = "204", description = "Eliminado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
