package com.scontrol.technicalreports.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.scontrol.technicalreports.model.TechnicalReport;

/**
 * Repositorio de acceso a los informes técnicos.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public interface TechnicalReportRepository extends JpaRepository<TechnicalReport, Long> {

    boolean existsByEquipmentId(Long equipmentId);
}
