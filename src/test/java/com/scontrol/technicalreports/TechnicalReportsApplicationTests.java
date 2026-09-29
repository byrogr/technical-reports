package com.scontrol.technicalreports;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifica que el contexto arranca con las migraciones aplicadas y las entidades validadas.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class TechnicalReportsApplicationTests {

    @Test
    void contextLoads() {
    }

}
