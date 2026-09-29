package com.scontrol.technicalreports.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.scontrol.technicalreports.dto.CatalogCreateRequest;
import com.scontrol.technicalreports.dto.CatalogResponse;
import com.scontrol.technicalreports.dto.CatalogUpdateRequest;
import com.scontrol.technicalreports.model.Catalog;

/**
 * Conversión entre la entidad Catalog y sus DTOs. El tipo de una opción no se modifica al editar.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@Mapper(uses = StringMapper.class)
public interface CatalogMapper {

    CatalogResponse toResponse(Catalog catalog);

    // Una opción nueva siempre nace activa (valor por defecto de la entidad)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    Catalog toEntity(CatalogCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    void updateEntity(CatalogUpdateRequest request, @MappingTarget Catalog catalog);
}
