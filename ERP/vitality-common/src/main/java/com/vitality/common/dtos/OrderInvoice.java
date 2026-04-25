package com.vitality.common.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class OrderInvoice {
    private Long orderId;
    private LocalDate orderDate;
    private String patientName;
    private BigDecimal totalItemPrice;
    private BigDecimal totalDiscount;
    private BigDecimal totalTaxAmount;
    private BigDecimal platformFee;
    private BigDecimal deliveryFee;
    private BigDecimal roundOffAmount;
    private BigDecimal totalPrice;
    private List<OrderItemInvoice> items;
}