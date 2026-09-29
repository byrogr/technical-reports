package com.scontrol.technicalreports.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.scontrol.technicalreports.model.TechnicalReport;

/**
 * Repositorio de acceso a los informes técnicos. Las consultas de lectura cargan en un solo
 * SELECT el equipo, el cliente, los catálogos y el usuario, para evitar consultas N+1.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public interface TechnicalReportRepository extends JpaRepository<TechnicalReport, Long>,
        JpaSpecificationExecutor<TechnicalReport> {

    @Override
    @EntityGraph(attributePaths = {"equipment.client", "initialStatus", "finalStatus", "eventFailure", "action",
            "personnel", "supervisor", "createdBy"})
    Optional<TechnicalReport> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"equipment.client", "initialStatus", "finalStatus", "eventFailure", "action",
            "personnel", "supervisor", "createdBy"})
    List<TechnicalReport> findAll(Specification<TechnicalReport> spec, Sort sort);

    boolean existsByEquipmentId(Long equipmentId);
}
