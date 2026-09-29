package com.scontrol.technicalreports.mapper;

import org.springframework.stereotype.Component;

/**
 * Normalización de textos que MapStruct aplica a todas las propiedades String: quita espacios
 * al inicio y al final.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@Component
public class StringMapper {

    public String trim(String value) {
        return value == null ? null : value.trim();
    }
}
