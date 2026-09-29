package com.scontrol.technicalreports.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.scontrol.technicalreports.dto.EquipmentRequest;
import com.scontrol.technicalreports.dto.EquipmentResponse;
import com.scontrol.technicalreports.model.Equipment;

/**
 * Conversión entre la entidad Equipment y sus DTOs. El cliente lo asigna el servicio.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@Mapper(uses = StringMapper.class)
public interface EquipmentMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", source = "client.name")
    EquipmentResponse toResponse(Equipment equipment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    Equipment toEntity(EquipmentRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    void updateEntity(EquipmentRequest request, @MappingTarget Equipment equipment);
}
