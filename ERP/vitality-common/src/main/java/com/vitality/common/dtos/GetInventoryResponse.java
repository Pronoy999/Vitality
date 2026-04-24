package com.vitality.common.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

@Data
public class GetInventoryResponse {
    private Long inventoryId;
    private String itemDescription;
    private BigInteger quantityAvailable;
    private BigInteger quantityReserved;
    private String batchNumber;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private BigDecimal purchasePrice;
    private BigDecimal taxPercentage;
    private BigDecimal mrp;
    private String supplierName;
}
