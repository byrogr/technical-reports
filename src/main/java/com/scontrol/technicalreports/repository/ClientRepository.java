package com.scontrol.technicalreports.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.scontrol.technicalreports.model.Client;

/**
 * Repositorio de acceso a los clientes.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findAllByOrderByNameAsc();

    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByDocumentNumberAndIdNot(String documentNumber, Long id);
}
