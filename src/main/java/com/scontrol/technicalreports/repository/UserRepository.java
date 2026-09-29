package com.scontrol.technicalreports.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.scontrol.technicalreports.model.User;

/**
 * Repositorio de acceso a los usuarios del sistema.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
