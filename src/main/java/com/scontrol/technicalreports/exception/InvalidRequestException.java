package com.scontrol.technicalreports.exception;

/**
 * Se lanza cuando la petición referencia datos inválidos (ej. una opción de catálogo de otro
 * tipo o inactiva); se traduce a HTTP 400.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
