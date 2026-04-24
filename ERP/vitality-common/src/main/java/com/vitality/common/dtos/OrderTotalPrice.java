package com.vitality.common.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderTotalPrice {
    private BigDecimal itemTotalPrice;
    private BigDecimal totalDiscount;
    private BigDecimal totalTaxAmount;
    private BigDecimal roundOffAmount;
    private BigDecimal totalPrice;
}
