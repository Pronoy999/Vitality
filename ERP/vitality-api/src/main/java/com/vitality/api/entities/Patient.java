package com.vitality.api.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "tbl_patients")
public class Patient extends BaseEntity {
    @Column(name = "abha_id")
    private String abhaId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "age")
    private Integer age;

    @Column(name = "gender")
    private String gender;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "email_id", unique = true)
    private String emailId;

    @Column(name = "height_in_cms")
    private BigDecimal heightInCms;

    @Column(name = "weight_in_kgs")
    private BigDecimal weightInKgs;

    @Column(name = "blood_pressure")
    private String bloodPressure;

    @Column(name = "ailment_history")
    private String ailmentHistory;

    @Column(name = "health_parameters")
    private String healthParameters;

    @Column(name = "has_heath_insurance")
    private Boolean hasHealthInsurance;

    @Column(name = "additional_diagnosis")
    private String additionalDiagnosis;

    @Column(name = "medicines_consumed")
    private String medicinesConsumed;

    @Column(name = "additional_services_required")
    private String additionalServicesRequired;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "guid")
    private User user;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @CreationTimestamp
    private LocalDateTime created_timestamp;
    @UpdateTimestamp
    private LocalDateTime updated_timestamp;


    @PreUpdate
    public void preUpdate() {
        this.updated_timestamp = LocalDateTime.now();
    }
}
