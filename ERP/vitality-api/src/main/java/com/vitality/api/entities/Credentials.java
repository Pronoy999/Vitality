package com.vitality.api.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_credentials")
@Data
@EqualsAndHashCode(callSuper = false)
public class Credentials extends BaseEntity {
    @Column(name = "email_id", nullable = false)
    private String emailId;
    @Column(name = "phone_number")
    private String phoneNumber;
    @Column(name = "password")
    private String password;
    @Column(name = "google_token")
    private String googleToken;
    @CreationTimestamp
    private LocalDateTime created_timestamp;
    @UpdateTimestamp
    private LocalDateTime updated_timestamp;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "guid")
    private User user;
}
