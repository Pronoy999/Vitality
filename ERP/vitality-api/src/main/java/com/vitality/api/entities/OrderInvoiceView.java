package com.vitality.api.entities;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

@Data
public class OrderInvoiceView {
    private Long orderId;
    private String firstName;
    private String lastName;
    private String patientPhoneNumber;
    private LocalDate orderDate;
    private BigInteger totalItems;
    private BigDecimal totalItemPrice;
    private BigDecimal totalDiscount;
    private BigDecimal totalTaxAmount;
    private BigDecimal platformFee;
    private BigDecimal deliveryFee;
    private BigDecimal roundOffAmount;
    private BigDecimal totalPrice;
    private BigInteger quantity;
    private String itemDescription;
    private BigDecimal itemPrice;
    private BigDecimal itemDiscount;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal itemTotalPrice;
    private LocalDate expiryDate;
    private String batchNumber;
    private BigDecimal mrp;

    public OrderInvoiceView(Long orderId, String firstName, String lastName, String patientPhoneNumber, LocalDate orderDate, BigInteger totalItems,
                            BigDecimal totalItemPrice, BigDecimal totalDiscount, BigDecimal totalTaxAmount,
                            BigDecimal platformFee, BigDecimal deliveryFee, BigDecimal roundOffAmount, BigDecimal totalPrice,
                            BigInteger quantity, String itemDescription, BigDecimal itemPrice, BigDecimal itemDiscount,
                            BigDecimal cgstAmount, BigDecimal sgstAmount, BigDecimal itemTotalPrice, LocalDate expiryDate, String batchNumber, BigDecimal mrp) {
        this.orderId = orderId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.patientPhoneNumber = patientPhoneNumber;
        this.orderDate = orderDate;
        this.totalItems = totalItems;
        this.totalItemPrice = totalItemPrice;
        this.totalDiscount = totalDiscount;
        this.totalTaxAmount = totalTaxAmount;
        this.platformFee = platformFee;
        this.deliveryFee = deliveryFee;
        this.roundOffAmount = roundOffAmount;
        this.totalPrice = totalPrice;
        this.quantity = quantity;
        this.itemDescription = itemDescription;
        this.itemPrice = itemPrice;
        this.itemDiscount = itemDiscount;
        this.cgstAmount = cgstAmount;
        this.sgstAmount = sgstAmount;
        this.itemTotalPrice = itemTotalPrice;
        this.expiryDate = expiryDate;
        this.batchNumber = batchNumber;
        this.mrp = mrp;
    }
}
