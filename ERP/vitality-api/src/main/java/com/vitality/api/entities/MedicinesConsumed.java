package com.vitality.api.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "tbl_medicines_consumed")
public class MedicinesConsumed extends BaseEntity {
    @Column(name = "medicine_name", nullable = false, columnDefinition = "TEXT")
    private String medicineName;
    @Column(name = "dosage", columnDefinition = "TEXT")
    private String dosage;
    @Column(name = "frequency", columnDefinition = "TEXT")
    private String frequency;
    @Column(name = "start_date", columnDefinition = "DATE")
    private Date startDate;
    @Column(name = "end_date", columnDefinition = "DATE")
    private Date endDate;
    @Column(name = "status")
    private String status;
    @Column(name = "prescribed_by", columnDefinition = "TEXT")
    private String prescribedBy;
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    @CreationTimestamp
    private LocalDateTime created;
    @UpdateTimestamp
    private LocalDateTime updated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patients patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_ailment_id", nullable = false)
    private AilmentType ailmentType;
}
