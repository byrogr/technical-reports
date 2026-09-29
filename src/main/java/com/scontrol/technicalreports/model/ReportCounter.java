package com.scontrol.technicalreports.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Contador de la numeración correlativa de informes técnicos por serie.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@Entity
@Table(name = "report_counters")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportCounter {

    // Las series se crean por migración, no desde la aplicación
    @Id
    @Column(length = 10)
    private String series;

    @Setter
    @Column(name = "last_number", nullable = false)
    private int lastNumber;
}
