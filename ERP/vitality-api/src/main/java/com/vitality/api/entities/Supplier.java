package com.vitality.api.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_supplier", schema = "vitality")
@Data
@EqualsAndHashCode(callSuper = false)
public class Supplier extends BaseEntity {

    @Column(name = "supplier_name", nullable = false)
    private String supplierName;

    @Column(name = "poc_name", nullable = false)
    private String pocName;

    @Column(name = "poc_contact", nullable = false)
    private String pocContact;

    @Column(name = "estimate_delivery_in_days")
    private BigDecimal estimateDeliveryInDays;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_timestamp", nullable = false, updatable = false)
    private LocalDateTime createdTimestamp;

    @Column(name = "updated_timestamp", nullable = false)
    private LocalDateTime updatedTimestamp;

    @PrePersist
    protected void onCreate() {
        this.createdTimestamp = LocalDateTime.now();
        this.updatedTimestamp = LocalDateTime.now();

        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedTimestamp = LocalDateTime.now();
    }
}