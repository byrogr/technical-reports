package com.scontrol.technicalreports.config;

import java.util.Locale;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.scontrol.technicalreports.model.User;
import com.scontrol.technicalreports.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Crea el usuario inicial al arrancar la aplicación si la tabla de usuarios está vacía.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }
        if (!StringUtils.hasText(adminProperties.email()) || !StringUtils.hasText(adminProperties.password())) {
            log.warn("No hay usuarios y ADMIN_EMAIL / ADMIN_PASSWORD no están definidos: nadie podrá hacer login");
            return;
        }
        User user = new User();
        user.setEmail(adminProperties.email().trim().toLowerCase(Locale.ROOT));
        user.setPasswordHash(passwordEncoder.encode(adminProperties.password()));
        userRepository.save(user);
        log.info("Usuario inicial creado: {}", user.getEmail());
    }
}
