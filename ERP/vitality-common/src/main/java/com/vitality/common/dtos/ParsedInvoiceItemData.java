package com.vitality.common.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

@Data
public class ParsedInvoiceItemData {
    private String itemDescription;
    private BigInteger receivedQuantity;
    private BigInteger damagedQuantity;
    private BigInteger freeQuantity;
    private BigDecimal itemPrice;
    private String hsnCode;
    private LocalDate expiryDate;
    private LocalDate manufacturedDate;
    private String batchNumber;
    private BigDecimal taxPercentage;
    private BigDecimal itemTotalPrice;
    private BigDecimal mrp;
}
