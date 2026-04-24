package com.vitality.common.dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class GetInventoryRequest {
    private String itemDesc;
    private String batchNumber;
    private LocalDate expiryDate;
}
