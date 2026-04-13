package com.vitality.api.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_otp")
public class Otp extends BaseEntity {
    @Column(name = "phone_number", length = 15, nullable = false)
    private String phoneNumber;

    @Column(name = "otp", nullable = false)
    private Integer otp;
    @CreationTimestamp
    private LocalDateTime created_timestamp;
    @UpdateTimestamp
    private LocalDateTime updated_timestamp;
}