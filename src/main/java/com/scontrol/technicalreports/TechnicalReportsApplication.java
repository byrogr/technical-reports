package com.scontrol.technicalreports;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Punto de entrada de la API de informes técnicos de Scontrol Ingeniería.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class TechnicalReportsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechnicalReportsApplication.class, args);
    }

}
