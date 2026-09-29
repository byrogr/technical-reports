package com.scontrol.technicalreports.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scontrol.technicalreports.dto.CatalogCreateRequest;
import com.scontrol.technicalreports.dto.CatalogResponse;
import com.scontrol.technicalreports.dto.CatalogUpdateRequest;
import com.scontrol.technicalreports.exception.ConflictException;
import com.scontrol.technicalreports.exception.ResourceNotFoundException;
import com.scontrol.technicalreports.mapper.CatalogMapper;
import com.scontrol.technicalreports.model.Catalog;
import com.scontrol.technicalreports.model.CatalogType;
import com.scontrol.technicalreports.repository.CatalogRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio encargado de la gestión de las opciones de catálogo.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CatalogService {

    private final CatalogRepository catalogRepository;
    private final CatalogMapper catalogMapper;

    // Devuelve activas e inactivas: el cliente de la API decide cuáles mostrar
    public List<CatalogResponse> findByType(CatalogType type) {
        return catalogRepository.findByTypeOrderByValueAsc(type).stream()
                .map(catalogMapper::toResponse)
                .toList();
    }

    @Transactional
    public CatalogResponse create(CatalogCreateRequest request) {
        Catalog catalog = catalogMapper.toEntity(request);
        if (catalogRepository.existsByTypeAndValue(catalog.getType(), catalog.getValue())) {
            throw duplicateValue(catalog.getType(), catalog.getValue());
        }
        return catalogMapper.toResponse(catalogRepository.save(catalog));
    }

    @Transactional
    public CatalogResponse update(Long id, CatalogUpdateRequest request) {
        Catalog catalog = catalogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opción de catálogo", id));
        String value = request.value().trim();
        if (catalogRepository.existsByTypeAndValueAndIdNot(catalog.getType(), value, id)) {
            throw duplicateValue(catalog.getType(), value);
        }
        catalogMapper.updateEntity(request, catalog);
        return catalogMapper.toResponse(catalog);
    }

    private static ConflictException duplicateValue(CatalogType type, String value) {
        return new ConflictException("Ya existe la opción '" + value + "' en el catálogo " + type);
    }
}
