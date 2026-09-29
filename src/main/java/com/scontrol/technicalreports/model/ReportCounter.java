package com.scontrol.technicalreports.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Contador de la numeración correlativa de informes técnicos por serie.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@Entity
@Table(name = "report_counters")
public class ReportCounter {

    @Id
    @Column(length = 10)
    private String series;

    @Column(name = "last_number", nullable = false)
    private int lastNumber;

    protected ReportCounter() {
    }

    public String getSeries() {
        return series;
    }

    public int getLastNumber() {
        return lastNumber;
    }

    public void setLastNumber(int lastNumber) {
        this.lastNumber = lastNumber;
    }
}
