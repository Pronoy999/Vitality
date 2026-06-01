package com.vitality.common.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

@Data
public class OrderItemInvoice {
    private String itemDescription;
    private BigInteger quantity;
    private BigDecimal itemPrice;
    private BigDecimal itemDiscount;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal itemTotalPrice;
    private LocalDate expiryDate;
    private String batchNumber;
    private BigDecimal mrp;
}