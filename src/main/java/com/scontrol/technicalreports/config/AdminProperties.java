package com.scontrol.technicalreports.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Credenciales del usuario inicial, leídas de las variables de entorno ADMIN_EMAIL y ADMIN_PASSWORD.
 *
 * @author Roger Rojas - roger.rojas@rmsolutions.pe
 */
@ConfigurationProperties("app.admin")
public record AdminProperties(String email, String password) {
}
