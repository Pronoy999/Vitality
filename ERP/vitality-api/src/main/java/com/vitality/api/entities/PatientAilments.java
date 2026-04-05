package com.vitality.api.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_patient_ailments")
public class PatientAilments extends BaseEntity {
    @Column(name = "ailment_desc", nullable = false)
    private String ailmentDesc;
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
    @JoinColumn(name = "ailment_type_id", nullable = false)
    private AilmentType ailmentType;

}
