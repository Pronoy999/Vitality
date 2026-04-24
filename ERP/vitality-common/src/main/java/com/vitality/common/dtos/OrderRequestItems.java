package com.vitality.common.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class OrderRequestItems {
    private Long inventoryId;
    private BigInteger quantity;
    private BigDecimal markupPercentage;
}
