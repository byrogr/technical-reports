package com.scontrol.technicalreports.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.scontrol.technicalreports.model.TechnicalReport;

/**
 * Filtros opcionales para el listado de informes técnicos.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public final class TechnicalReportSpecifications {

    private TechnicalReportSpecifications() {
    }

    public static Specification<TechnicalReport> clientId(Long clientId) {
        return (root, query, cb) -> cb.equal(root.get("equipment").get("client").get("id"), clientId);
    }

    public static Specification<TechnicalReport> equipmentId(Long equipmentId) {
        return (root, query, cb) -> cb.equal(root.get("equipment").get("id"), equipmentId);
    }

    // Rango de fechas sobre la fecha de término, ambos extremos inclusivos
    public static Specification<TechnicalReport> endDateFrom(LocalDate from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("endDatetime"), from.atStartOfDay());
    }

    public static Specification<TechnicalReport> endDateTo(LocalDate to) {
        return (root, query, cb) -> cb.lessThan(root.get("endDatetime"), to.plusDays(1).atStartOfDay());
    }
}
