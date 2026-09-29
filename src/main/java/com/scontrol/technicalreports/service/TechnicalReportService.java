package com.scontrol.technicalreports.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scontrol.technicalreports.dto.TechnicalReportRequest;
import com.scontrol.technicalreports.dto.TechnicalReportResponse;
import com.scontrol.technicalreports.exception.InvalidRequestException;
import com.scontrol.technicalreports.exception.ResourceNotFoundException;
import com.scontrol.technicalreports.mapper.TechnicalReportMapper;
import com.scontrol.technicalreports.model.Catalog;
import com.scontrol.technicalreports.model.CatalogType;
import com.scontrol.technicalreports.model.ReportCounter;
import com.scontrol.technicalreports.model.TechnicalReport;
import com.scontrol.technicalreports.model.User;
import com.scontrol.technicalreports.repository.CatalogRepository;
import com.scontrol.technicalreports.repository.EquipmentRepository;
import com.scontrol.technicalreports.repository.ReportCounterRepository;
import com.scontrol.technicalreports.repository.TechnicalReportRepository;
import com.scontrol.technicalreports.repository.TechnicalReportSpecifications;
import com.scontrol.technicalreports.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio encargado de la gestión de informes técnicos y de su numeración correlativa.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TechnicalReportService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "id");

    private final TechnicalReportRepository technicalReportRepository;
    private final ReportCounterRepository reportCounterRepository;
    private final EquipmentRepository equipmentRepository;
    private final CatalogRepository catalogRepository;
    private final UserRepository userRepository;
    private final TechnicalReportMapper technicalReportMapper;

    public List<TechnicalReportResponse> findAll(Long clientId, Long equipmentId, LocalDate from, LocalDate to) {
        List<Specification<TechnicalReport>> filters = new ArrayList<>();
        if (clientId != null) {
            filters.add(TechnicalReportSpecifications.clientId(clientId));
        }
        if (equipmentId != null) {
            filters.add(TechnicalReportSpecifications.equipmentId(equipmentId));
        }
        if (from != null) {
            filters.add(TechnicalReportSpecifications.endDateFrom(from));
        }
        if (to != null) {
            filters.add(TechnicalReportSpecifications.endDateTo(to));
        }
        return technicalReportRepository.findAll(Specification.allOf(filters), NEWEST_FIRST).stream()
                .map(technicalReportMapper::toResponse)
                .toList();
    }

    public TechnicalReportResponse findById(Long id) {
        return technicalReportMapper.toResponse(getReport(id));
    }

    @Transactional
    public TechnicalReportResponse create(TechnicalReportRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new InsufficientAuthenticationException("Usuario no encontrado"));

        // Primero se valida todo; el contador se bloquea al final para no retener el lock de más
        TechnicalReport report = new TechnicalReport();
        applyRequest(request, report);
        report.setCreatedBy(user);
        report.setReportNumber(nextReportNumber());

        return technicalReportMapper.toResponse(technicalReportRepository.save(report));
    }

    @Transactional
    public TechnicalReportResponse update(Long id, TechnicalReportRequest request) {
        TechnicalReport report = getReport(id);
        applyRequest(request, report);
        return technicalReportMapper.toResponse(report);
    }

    TechnicalReport getReport(Long id) {
        return technicalReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Informe técnico", id));
    }

    private void applyRequest(TechnicalReportRequest request, TechnicalReport report) {
        technicalReportMapper.updateEntity(request, report);
        report.setEquipment(equipmentRepository.findById(request.equipmentId())
                .orElseThrow(() -> new InvalidRequestException("El equipo " + request.equipmentId() + " no existe")));
        report.setInitialStatus(resolveCatalog(request.initialStatusId(), CatalogType.STATUS, report.getInitialStatus()));
        report.setFinalStatus(resolveCatalog(request.finalStatusId(), CatalogType.STATUS, report.getFinalStatus()));
        report.setEventFailure(resolveCatalog(request.eventFailureId(), CatalogType.EVENT_FAILURE, report.getEventFailure()));
        report.setAction(resolveCatalog(request.actionId(), CatalogType.ACTION, report.getAction()));
        report.setPersonnel(resolveCatalog(request.personnelId(), CatalogType.PERSONNEL, report.getPersonnel()));
        report.setSupervisor(resolveCatalog(request.supervisorId(), CatalogType.SUPERVISOR, report.getSupervisor()));
    }

    /**
     * La opción debe existir, ser del tipo esperado y estar activa. Se acepta una opción inactiva
     * solo si es la que el informe ya tenía, para poder editar informes antiguos.
     */
    private Catalog resolveCatalog(Long id, CatalogType expectedType, Catalog current) {
        if (current != null && current.getId().equals(id)) {
            return current;
        }
        Catalog catalog = catalogRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException("La opción de catálogo " + id + " no existe"));
        if (catalog.getType() != expectedType) {
            throw new InvalidRequestException(
                    "La opción " + id + " es de tipo " + catalog.getType() + ", se esperaba " + expectedType);
        }
        if (!catalog.isActive()) {
            throw new InvalidRequestException("La opción '" + catalog.getValue() + "' está desactivada");
        }
        return catalog;
    }

    // Formato <serie>-<correlativo>, con mínimo 4 dígitos: 008-0001 ... 008-9999, 008-10000
    private String nextReportNumber() {
        ReportCounter counter = reportCounterRepository.findFirstByOrderBySeriesAsc()
                .orElseThrow(() -> new IllegalStateException("No hay serie configurada en report_counters"));
        counter.setLastNumber(counter.getLastNumber() + 1);
        return "%s-%04d".formatted(counter.getSeries(), counter.getLastNumber());
    }
}
