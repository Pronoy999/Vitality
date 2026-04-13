package com.vitality.api.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_roles_master")
@EqualsAndHashCode(callSuper = false)
public class Roles extends BaseEntity {
    @Column(name = "role_name", nullable = false)
    private String roleName;
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    @CreationTimestamp
    private LocalDateTime created_timestamp;
    @UpdateTimestamp
    private LocalDateTime updated_timestamp;
}
