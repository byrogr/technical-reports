package com.scontrol.technicalreports.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.scontrol.technicalreports.dto.TechnicalReportRequest;
import com.scontrol.technicalreports.dto.TechnicalReportResponse;
import com.scontrol.technicalreports.model.TechnicalReport;

/**
 * Conversión entre la entidad TechnicalReport y sus DTOs. Las relaciones (equipo, catálogos,
 * usuario) y el número de informe los asigna el servicio.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@Mapper(uses = {StringMapper.class, CatalogMapper.class})
public interface TechnicalReportMapper {

    @Mapping(target = "clientId", source = "equipment.client.id")
    @Mapping(target = "clientName", source = "equipment.client.name")
    @Mapping(target = "equipmentId", source = "equipment.id")
    @Mapping(target = "equipmentModel", source = "equipment.model")
    @Mapping(target = "equipmentSerialNumber", source = "equipment.serialNumber")
    @Mapping(target = "createdBy", source = "createdBy.email")
    TechnicalReportResponse toResponse(TechnicalReport report);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reportNumber", ignore = true)
    @Mapping(target = "equipment", ignore = true)
    @Mapping(target = "initialStatus", ignore = true)
    @Mapping(target = "finalStatus", ignore = true)
    @Mapping(target = "eventFailure", ignore = true)
    @Mapping(target = "action", ignore = true)
    @Mapping(target = "personnel", ignore = true)
    @Mapping(target = "supervisor", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntity(TechnicalReportRequest request, @MappingTarget TechnicalReport report);
}
