package com.scontrol.technicalreports.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scontrol.technicalreports.dto.EquipmentRequest;
import com.scontrol.technicalreports.dto.EquipmentResponse;
import com.scontrol.technicalreports.exception.ConflictException;
import com.scontrol.technicalreports.exception.ResourceNotFoundException;
import com.scontrol.technicalreports.mapper.EquipmentMapper;
import com.scontrol.technicalreports.model.Equipment;
import com.scontrol.technicalreports.repository.EquipmentRepository;
import com.scontrol.technicalreports.repository.TechnicalReportRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio encargado de la gestión de los equipos de cada cliente.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final TechnicalReportRepository technicalReportRepository;
    private final ClientService clientService;
    private final EquipmentMapper equipmentMapper;

    public List<EquipmentResponse> findByClient(Long clientId) {
        // Valida que el cliente exista para devolver 404 en vez de una lista vacía
        clientService.getClient(clientId);
        return equipmentRepository.findByClientIdOrderByModelAsc(clientId).stream()
                .map(equipmentMapper::toResponse)
                .toList();
    }

    public EquipmentResponse findById(Long id) {
        return equipmentMapper.toResponse(getEquipment(id));
    }

    @Transactional
    public EquipmentResponse create(Long clientId, EquipmentRequest request) {
        Equipment equipment = equipmentMapper.toEntity(request);
        equipment.setClient(clientService.getClient(clientId));
        return equipmentMapper.toResponse(equipmentRepository.save(equipment));
    }

    @Transactional
    public EquipmentResponse update(Long id, EquipmentRequest request) {
        Equipment equipment = getEquipment(id);
        equipmentMapper.updateEntity(request, equipment);
        return equipmentMapper.toResponse(equipment);
    }

    @Transactional
    public void delete(Long id) {
        Equipment equipment = getEquipment(id);
        if (technicalReportRepository.existsByEquipmentId(id)) {
            throw new ConflictException("El equipo tiene informes técnicos asociados y no se puede eliminar");
        }
        equipmentRepository.delete(equipment);
    }

    private Equipment getEquipment(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo", id));
    }
}
