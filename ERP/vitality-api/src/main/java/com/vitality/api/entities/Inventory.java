package com.vitality.api.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "tbl_inventory")
@Data
public class Inventory extends BaseEntity {

    @Column(name = "item_desc", nullable = false)
    private String itemDescription;

    @Column(name = "quantity_available", nullable = false)
    private BigInteger quantityAvailable;

    @Column(name = "quantity_reserved")
    private BigInteger quantityReserved;

    @Column(name = "batch_number", length = 1000)
    private String batchNumber;

    @Column(name = "manufacturing_date")
    private LocalDate manufacturingDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "purchase_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "tax_percentage", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxPercentage;

    @Column(name = "mrp", nullable = false, precision = 10, scale = 2)
    private BigDecimal mrp;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_timestamp", nullable = false, updatable = false)
    private LocalDateTime createdTimestamp;

    @Column(name = "updated_timestamp", nullable = false)
    private LocalDateTime updatedTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;


    @PrePersist
    protected void onCreate() {
        this.createdTimestamp = LocalDateTime.now();
        this.updatedTimestamp = LocalDateTime.now();
        if (this.quantityReserved == null) {
            this.quantityReserved = BigInteger.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedTimestamp = LocalDateTime.now();
    }

    /**
     * Method to get the unique key for the inventory item based on item description, batch number, manufacturing date and expiry date.
     *
     * @return the unique Key.
     */
    public String getKey() {
        if (itemDescription != null && batchNumber != null && expiryDate != null) {
            return itemDescription + "_" + batchNumber + "_" + manufacturingDate.toString() + "_" + expiryDate;
        } else {
            return null;
        }
    }
}
