package com.vitality.api.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_invoice_items", schema = "vitality")
@EqualsAndHashCode(callSuper = false)
@Data
public class InvoiceItem extends BaseEntity {

    @Column(name = "item_desc", nullable = false)
    private String itemDesc;

    @Column(name = "received_item_qty", nullable = false)
    private BigDecimal receivedItemQty = BigDecimal.ZERO;

    @Column(name = "damaged_item_qty")
    private BigDecimal damagedItemQty = BigDecimal.ZERO;

    @Column(name = "item_price", nullable = false)
    private BigDecimal itemPrice;

    @Column(name = "hsn_code", nullable = false)
    private String hsnCode;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "manufactured_date")
    private LocalDate manufacturedDate;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "mrp")
    private BigDecimal mrp;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_timestamp", nullable = false, updatable = false)
    private LocalDateTime createdTimestamp;

    @Column(name = "updated_timestamp", nullable = false)
    private LocalDateTime updatedTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @PrePersist
    protected void onCreate() {
        this.createdTimestamp = LocalDateTime.now();
        this.updatedTimestamp = LocalDateTime.now();

        if (this.receivedItemQty == null) {
            this.receivedItemQty = BigDecimal.ZERO;
        }

        if (this.damagedItemQty == null) {
            this.damagedItemQty = BigDecimal.ZERO;
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