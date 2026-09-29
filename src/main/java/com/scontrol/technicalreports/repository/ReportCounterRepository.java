package com.scontrol.technicalreports.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.scontrol.technicalreports.model.ReportCounter;

import jakarta.persistence.LockModeType;

/**
 * Repositorio del contador de numeración de informes.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public interface ReportCounterRepository extends JpaRepository<ReportCounter, String> {

    // SELECT ... FOR UPDATE: bloquea la fila hasta que termine la transacción que crea el informe
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ReportCounter> findFirstByOrderBySeriesAsc();
}
