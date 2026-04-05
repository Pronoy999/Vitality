package com.vitality.api.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tbl_patients")
public class Patients {
    @Id
    @Column(name = "guid", nullable = false)
    private String guid;

    @Column(name = "abha_id")
    private String abhaId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "age", nullable = false)
    private int age;

    @Column(name = "gender", nullable = false)
    private String gender;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "email_id", unique = true)
    private String emailId;

    @Column(name = "height_in_cms")
    private int heightInCms;

    @Column(name = "weight_in_kgs")
    private int weightInKgs;

    @Column(name = "blood_pressure")
    private String bloodPressure;

    @Column(name = "ailment_history")
    private String ailmentHistory;

    @Column(name = "has_heath_insurance")
    private boolean hasHealthInsurance;

    @Column(name = "additional_diagnosis")
    private String additionalDiagnosis;

    @Column(name = "medicines_consumed")
    private String medicinesConsumed;

    @Column(name = "additional_services_required")
    private String additionalServicesRequired;

    @CreationTimestamp
    private LocalDateTime created;
    @UpdateTimestamp
    private LocalDateTime updated;
}
