package com.vitality.api.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_purchase_order_items", schema = "vitality")
@EqualsAndHashCode(callSuper = false)
@Data
public class PurchaseOrderItem extends BaseEntity {

    @Column(name = "item_desc", nullable = false)
    private String itemDesc;

    @Column(name = "item_qty", nullable = false)
    private BigDecimal itemQty;

    @Column(name = "estimated_price")
    private BigDecimal estimatedPrice;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_timestamp", nullable = false, updatable = false)
    private LocalDateTime createdTimestamp;

    @Column(name = "updated_timestamp", nullable = false)
    private LocalDateTime updatedTimestamp;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "po_id", nullable = false)
    private PurchaseOrder purchaseOrder;


    @PrePersist
    protected void onCreate() {
        this.createdTimestamp = LocalDateTime.now();
        this.updatedTimestamp = LocalDateTime.now();

        if (this.itemQty == null) {
            this.itemQty = BigDecimal.ONE;
        }

        if (this.estimatedPrice == null) {
            this.estimatedPrice = BigDecimal.ZERO;
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