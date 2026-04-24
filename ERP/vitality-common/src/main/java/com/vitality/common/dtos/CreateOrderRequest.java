package com.vitality.common.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderRequest {
    private List<OrderRequestItems> orderRequestItems;
    private Long patientId;
    private String patientFirstName;
    private String patientLastName;
    private BigDecimal deliveryFee;
    private BigDecimal platformFee;
}
