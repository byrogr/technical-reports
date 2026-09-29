package com.scontrol.technicalreports.exception;

/**
 * Se lanza cuando el recurso solicitado no existe; se traduce a HTTP 404.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " con id " + id + " no existe");
    }
}
