package com.vitality.common.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateInvoiceRequest {
    private Long purchaseOrderId;
    private String invoiceNumber;
    private String supplierName;
    private Long supplierId;
    private LocalDate invoiceDate;
    private LocalDate receivedDate;
    private boolean areItemsDelivered;
    private BigDecimal itemTotalPrice;
    private BigDecimal discountAmount;
    private BigDecimal logisticsAmount;
    private BigDecimal insuranceAmount;
    private BigDecimal roundOffAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalPrice;
    private List<InvoiceItemsRequest> invoiceItems;
}
