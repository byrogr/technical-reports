package com.scontrol.technicalreports.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.scontrol.technicalreports.model.Equipment;

/**
 * Repositorio de acceso a los equipos.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findByClientIdOrderByModelAsc(Long clientId);

    boolean existsByClientId(Long clientId);
}
