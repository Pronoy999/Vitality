package com.vitality.common.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemPrice {
    private BigDecimal totalItemPrice;
    private BigDecimal totalDiscount;
    private BigDecimal cgstPercentage;
    private BigDecimal cgstAmount;
    private BigDecimal sgstPercentage;
    private BigDecimal sgstAmount;
    private BigDecimal totalTaxAmount;
    private BigDecimal totalPrice;
}
