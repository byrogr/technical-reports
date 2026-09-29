package com.scontrol.technicalreports.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración del JWT: clave secreta HS256 y tiempo de expiración del token.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@ConfigurationProperties("app.jwt")
public record JwtProperties(String secret, Duration expiration) {

    // HS256 exige una clave de al menos 256 bits
    private static final int MIN_SECRET_BYTES = 32;

    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "app.jwt.secret (JWT_SECRET) debe tener al menos " + MIN_SECRET_BYTES + " bytes");
        }
        if (expiration == null) {
            expiration = Duration.ofHours(10);
        }
    }
}
