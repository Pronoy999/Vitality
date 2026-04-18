package com.vitality.api.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_purchase_order", schema = "vitality")
public class PurchaseOrder extends BaseEntity {

    @Column(name = "po_number")
    private String poNumber;

    @Column(name = "po_generation_date")
    private LocalDate poGenerationDate;

    @Column(name = "po_delivery_date")
    private LocalDate poDeliveryDate;

    @Column(name = "status")
    private String status = "PO_GENERATED";

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_on")
    private LocalDate approvedOn;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_timestamp", nullable = false, updatable = false)
    private LocalDateTime createdTimestamp;

    @Column(name = "updated_timestamp", nullable = false)
    private LocalDateTime updatedTimestamp;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;


    @PrePersist
    protected void onCreate() {
        this.createdTimestamp = LocalDateTime.now();
        this.updatedTimestamp = LocalDateTime.now();

        if (this.poGenerationDate == null) {
            this.poGenerationDate = LocalDate.now();
        }

        if (this.status == null) {
            this.status = "PO_GENERATED";
        }

        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedTimestamp = LocalDateTime.now();
    }
}