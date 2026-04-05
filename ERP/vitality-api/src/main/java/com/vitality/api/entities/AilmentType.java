package com.vitality.api.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_ailment_type_master")
public class AilmentType extends BaseEntity {
    @Column(name = "code")
    private String ailmentCode;
    @Column(name = "type", nullable = false)
    private String ailmentType;
    @CreationTimestamp
    private LocalDateTime created;
    @UpdateTimestamp
    private LocalDateTime updated;

}
