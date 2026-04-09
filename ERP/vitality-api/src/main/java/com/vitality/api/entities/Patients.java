package com.vitality.api.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tbl_patients")
public class Patients {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "credentials_seq")
    @SequenceGenerator(
            name = "credentials_seq",
            sequenceName = "global_sid_seq",
            allocationSize = 1
    )
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

    @Column(name = "health_parameters")
    private String healthParameters;

    @Column(name = "has_heath_insurance")
    private boolean hasHealthInsurance;

    @Column(name = "additional_diagnosis")
    private String additionalDiagnosis;

    @Column(name = "medicines_consumed")
    private String medicinesConsumed;

    @Column(name = "additional_services_required")
    private String additionalServicesRequired;

    @CreationTimestamp
    private LocalDateTime created_timestamp;
    @UpdateTimestamp
    private LocalDateTime updated_timestamp;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "guid", insertable = false, updatable = false)
    private User user;
}
