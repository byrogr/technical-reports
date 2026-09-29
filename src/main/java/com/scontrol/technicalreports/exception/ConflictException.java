package com.scontrol.technicalreports.exception;

/**
 * Se lanza cuando la operación choca con el estado actual de los datos (duplicados,
 * registros con dependencias); se traduce a HTTP 409.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
