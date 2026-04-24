package com.vitality.common.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

@Data
public class InvoiceItemResponse {

    private Long id;

    private String itemDesc;
    private BigInteger receivedItemQty;
    private BigInteger damagedItemQty;
    private BigInteger freeItemQty;

    private BigDecimal itemPrice;
    private String hsnCode;

    private LocalDate expiryDate;
    private LocalDate manufacturedDate;
    private String batchNumber;

    private BigDecimal taxPercentage;
    private BigDecimal itemTotalPrice;
    private BigDecimal mrp;
}