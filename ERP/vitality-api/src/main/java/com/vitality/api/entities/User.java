package com.vitality.api.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "tbl_users")
@Data
public class User {
    @Id
    @Column(name = "guid", nullable = false)
    private String guid;

    @Column(name = "first_name", nullable = false)
    private String firstName;
    @Column(name = "last_name", nullable = false)
    private String lastName;
    @Column(name = "date_of_birth")
    private Date dateOfBirth;
    @Column(name = "gender")
    private String gender;
    @Column(name = "age")
    private int age;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    @CreationTimestamp
    private LocalDateTime created_timestamp;
    @UpdateTimestamp
    private LocalDateTime updated_timestamp;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Roles userRole;

    @PreUpdate
    public void preUpdate() {
        this.updated_timestamp = LocalDateTime.now();
    }
}
