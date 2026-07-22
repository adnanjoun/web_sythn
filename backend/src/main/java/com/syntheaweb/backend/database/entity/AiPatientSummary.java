package com.syntheaweb.backend.database.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_patient_summaries")
public class AiPatientSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Patient patient;

    @Column(columnDefinition = "TEXT")
    private String summaryText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SummaryStatus status = SummaryStatus.PENDING;

    @Column(nullable = false)
    private String modelName;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public AiPatientSummary() {}

    public AiPatientSummary(Patient patient, String modelName) {
        this.patient = patient;
        this.modelName = modelName;
        this.createdAt = LocalDateTime.now();
        this.status = SummaryStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
    }

    public SummaryStatus getStatus() {
        return status;
    }

    public void setStatus(SummaryStatus status) {
        this.status = status;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
