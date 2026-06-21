package com.vitality.api.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_order_items")
@Data
@EqualsAndHashCode(callSuper = false)
public class OrderItems extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "quantity", nullable = false)
    private BigInteger quantity;

    @Column(name = "item_price", nullable = false)
    private BigDecimal itemPrice;

    @Column(name = "item_discount", nullable = false)
    private BigDecimal itemDiscount;

    @Column(name = "cgst_percentage")
    private BigDecimal cgstPercentage;

    @Column(name = "cgst_amount")
    private BigDecimal cgstAmount;

    @Column(name = "sgst_percentage")
    private BigDecimal sgstPercentage;

    @Column(name = "sgst_amount")
    private BigDecimal sgstAmount;

    @Column(name = "igst_percentage")
    private BigDecimal igstPercentage;

    @Column(name = "igst_amount")
    private BigDecimal igstAmount;

    @Column(name = "total_tax_amount")
    private BigDecimal totalTaxAmount;

    @Column(name = "item_total_price", nullable = false)
    private BigDecimal itemTotalPrice;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

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