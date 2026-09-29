package com.scontrol.technicalreports.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scontrol.technicalreports.dto.ClientRequest;
import com.scontrol.technicalreports.dto.ClientResponse;
import com.scontrol.technicalreports.exception.ConflictException;
import com.scontrol.technicalreports.exception.ResourceNotFoundException;
import com.scontrol.technicalreports.mapper.ClientMapper;
import com.scontrol.technicalreports.model.Client;
import com.scontrol.technicalreports.repository.ClientRepository;
import com.scontrol.technicalreports.repository.EquipmentRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio encargado de la gestión de clientes.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final EquipmentRepository equipmentRepository;
    private final ClientMapper clientMapper;

    public List<ClientResponse> findAll() {
        return clientRepository.findAllByOrderByNameAsc().stream()
                .map(clientMapper::toResponse)
                .toList();
    }

    public ClientResponse findById(Long id) {
        return clientMapper.toResponse(getClient(id));
    }

    @Transactional
    public ClientResponse create(ClientRequest request) {
        if (request.documentNumber() != null && clientRepository.existsByDocumentNumber(request.documentNumber())) {
            throw duplicateDocument(request.documentNumber());
        }
        Client client = clientMapper.toEntity(request);
        return clientMapper.toResponse(clientRepository.save(client));
    }

    @Transactional
    public ClientResponse update(Long id, ClientRequest request) {
        Client client = getClient(id);
        if (request.documentNumber() != null
                && clientRepository.existsByDocumentNumberAndIdNot(request.documentNumber(), id)) {
            throw duplicateDocument(request.documentNumber());
        }
        clientMapper.updateEntity(request, client);
        return clientMapper.toResponse(client);
    }

    @Transactional
    public void delete(Long id) {
        Client client = getClient(id);
        // Los informes cuelgan de los equipos: si no hay equipos, tampoco hay informes
        if (equipmentRepository.existsByClientId(id)) {
            throw new ConflictException("El cliente tiene equipos asociados y no se puede eliminar");
        }
        clientRepository.delete(client);
    }

    Client getClient(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    private static ConflictException duplicateDocument(String documentNumber) {
        return new ConflictException("Ya existe un cliente con el documento " + documentNumber);
    }
}
