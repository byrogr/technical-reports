package com.scontrol.technicalreports.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.scontrol.technicalreports.model.Catalog;
import com.scontrol.technicalreports.model.CatalogType;

/**
 * Repositorio de acceso a las opciones de catálogo.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public interface CatalogRepository extends JpaRepository<Catalog, Long> {

    List<Catalog> findByTypeOrderByValueAsc(CatalogType type);

    boolean existsByTypeAndValue(CatalogType type, String value);

    boolean existsByTypeAndValueAndIdNot(CatalogType type, String value, Long id);
}
