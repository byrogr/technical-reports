package com.scontrol.technicalreports.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Informe técnico de mantenimiento realizado sobre un equipo.
 *
 * @author Roger Rojas Effio - roger.rojas@rmsolutions.pe
 */
@Entity
@Table(name = "technical_reports")
public class TechnicalReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_number", nullable = false, unique = true, length = 20)
    private String reportNumber;

    // El cliente del informe se obtiene a través del equipo
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(name = "start_datetime")
    private LocalDateTime startDatetime;

    @Column(name = "end_datetime", nullable = false)
    private LocalDateTime endDatetime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initial_status_id", nullable = false)
    private Catalog initialStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "final_status_id", nullable = false)
    private Catalog finalStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_failure_id", nullable = false)
    private Catalog eventFailure;

    @Column(name = "affected_component")
    private String affectedComponent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "action_id", nullable = false)
    private Catalog action;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String details;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personnel_id", nullable = false)
    private Catalog personnel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supervisor_id", nullable = false)
    private Catalog supervisor;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private User createdBy;

    protected TechnicalReport() {
    }

    public TechnicalReport(String reportNumber, User createdBy) {
        this.reportNumber = reportNumber;
        this.createdBy = createdBy;
    }

    public Long getId() {
        return id;
    }

    public String getReportNumber() {
        return reportNumber;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public LocalDateTime getStartDatetime() {
        return startDatetime;
    }

    public void setStartDatetime(LocalDateTime startDatetime) {
        this.startDatetime = startDatetime;
    }

    public LocalDateTime getEndDatetime() {
        return endDatetime;
    }

    public void setEndDatetime(LocalDateTime endDatetime) {
        this.endDatetime = endDatetime;
    }

    public Catalog getInitialStatus() {
        return initialStatus;
    }

    public void setInitialStatus(Catalog initialStatus) {
        this.initialStatus = initialStatus;
    }

    public Catalog getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(Catalog finalStatus) {
        this.finalStatus = finalStatus;
    }

    public Catalog getEventFailure() {
        return eventFailure;
    }

    public void setEventFailure(Catalog eventFailure) {
        this.eventFailure = eventFailure;
    }

    public String getAffectedComponent() {
        return affectedComponent;
    }

    public void setAffectedComponent(String affectedComponent) {
        this.affectedComponent = affectedComponent;
    }

    public Catalog getAction() {
        return action;
    }

    public void setAction(Catalog action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Catalog getPersonnel() {
        return personnel;
    }

    public void setPersonnel(Catalog personnel) {
        this.personnel = personnel;
    }

    public Catalog getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(Catalog supervisor) {
        this.supervisor = supervisor;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }
}
