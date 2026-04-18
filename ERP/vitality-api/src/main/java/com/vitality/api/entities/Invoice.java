package com.vitality.api.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_invoice", schema = "vitality")
@EqualsAndHashCode(callSuper = false)
@Data
public class Invoice extends BaseEntity {

    @Column(name = "invoice_id")
    private String invoiceId;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Column(name = "status")
    private String status = "INVOICE_RAISED";

    @Column(name = "tax_amt")
    private BigDecimal taxAmount;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_timestamp", nullable = false, updatable = false)
    private LocalDateTime createdTimestamp;

    @Column(name = "updated_timestamp", nullable = false)
    private LocalDateTime updatedTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_id", nullable = false)
    private PurchaseOrder purchaseOrder;


    @PrePersist
    protected void onCreate() {
        this.createdTimestamp = LocalDateTime.now();
        this.updatedTimestamp = LocalDateTime.now();

        if (this.invoiceDate == null) {
            this.invoiceDate = LocalDate.now();
        }

        if (this.status == null) {
            this.status = "INVOICE_RAISED";
        }

        if (this.taxAmount == null) {
            this.taxAmount = BigDecimal.ZERO;
        }

        if (this.totalPrice == null) {
            this.totalPrice = BigDecimal.ZERO;
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