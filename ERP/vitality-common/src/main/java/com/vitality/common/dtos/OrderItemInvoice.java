package com.vitality.common.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class OrderItemInvoice {
    private String itemDescription;
    private BigInteger quantity;
    private BigDecimal itemPrice;
    private BigDecimal itemDiscount;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal itemTotalPrice;
}