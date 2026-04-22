package com.vitality.common.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InvoiceResponse {

    private Long id;
    private String invoiceId;

    private LocalDate invoiceDate;
    private LocalDate receivedDate;
    private String status;

    private BigDecimal itemTotalPrice;
    private BigDecimal totalDiscount;
    private BigDecimal logisticAmount;
    private BigDecimal insuranceAmount;
    private BigDecimal roundOffAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalPrice;

    private Boolean isActive;

    private LocalDateTime createdTimestamp;
    private LocalDateTime updatedTimestamp;

    // Flattened supplier (avoid nested proxy issues)
    private Long supplierId;
    private String supplierName;

    // Optional: PO reference
    private Long purchaseOrderId;

    private List<InvoiceItemResponse> items;
}