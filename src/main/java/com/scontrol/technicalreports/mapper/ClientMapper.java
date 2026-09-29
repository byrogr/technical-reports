package com.scontrol.technicalreports.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.scontrol.technicalreports.dto.ClientRequest;
import com.scontrol.technicalreports.dto.ClientResponse;
import com.scontrol.technicalreports.model.Client;

/**
 * Conversión entre la entidad Client y sus DTOs.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@Mapper(uses = StringMapper.class)
public interface ClientMapper {

    ClientResponse toResponse(Client client);

    @Mapping(target = "id", ignore = true)
    Client toEntity(ClientRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(ClientRequest request, @MappingTarget Client client);
}
