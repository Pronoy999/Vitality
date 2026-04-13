package com.vitality.api.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_prescription_diagnosis")
@EqualsAndHashCode(callSuper = false)
@Data
public class PrescriptionDiagnosis extends BaseEntity {
    @Column(name = "diagnosis")
    private String diagnosis;
    @Column(name = "medicine_name")
    private String medicineName;
    @Column(name = "dosage")
    private String dosage;
    @Column(name = "unit")
    private BigDecimal unit;
    @Column(name = "unit_measure")
    private String unitMeasure;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    @Column(name = "frequency")
    private String frequency;
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime created_timestamp;
    @UpdateTimestamp
    private LocalDateTime updated_timestamp;

    @ManyToOne
    @JoinColumn(name = "prescription_id", referencedColumnName = "id")
    private Prescription prescription;


    @PreUpdate
    public void preUpdate() {
        this.updated_timestamp = LocalDateTime.now();
    }
}
