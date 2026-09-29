package com.scontrol.technicalreports.config;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.scontrol.technicalreports.model.User;
import com.scontrol.technicalreports.repository.UserRepository;

/**
 * Crea el usuario inicial al arrancar la aplicación si la tabla de usuarios está vacía.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@Component
public class AdminUserInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    public AdminUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                AdminProperties adminProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminProperties = adminProperties;
    }

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
        String email = adminProperties.email().trim().toLowerCase(Locale.ROOT);
        userRepository.save(new User(email, passwordEncoder.encode(adminProperties.password())));
        log.info("Usuario inicial creado: {}", email);
    }
}
